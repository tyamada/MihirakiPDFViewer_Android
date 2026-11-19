package com.github.tyamada.mihirakipdfviewer_android.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface PurchaseState {
    data object Idle : PurchaseState
    data object Cancelled : PurchaseState
    data class Error(val message: String) : PurchaseState
    data class Success(val tier: TipTier) : PurchaseState
}

class BillingManager(context: Context) : PurchasesUpdatedListener, AutoCloseable {
    private val productCatalog = ProductCatalogState<ProductDetails>()
    val catalog = productCatalog.state
    private val _purchase = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchase = _purchase.asStateFlow()
    private val _purchasedTiers = MutableStateFlow<Set<TipTier>>(emptySet())
    val purchasedTiers = _purchasedTiers.asStateFlow()
    private val client = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .enableAutoServiceReconnection()
        .build()

    fun connect() {
        Log.d("Billing", "Starting connection...")
        client.startConnection(
            object : BillingClientStateListener {
                override fun onBillingServiceDisconnected() {
                    productCatalog.invalidate()
                    Log.d("Billing", "Service disconnected")
                }
                override fun onBillingSetupFinished(result: BillingResult) {
                    Log.d("Billing", "Setup finished: ${result.responseCode} - ${result.debugMessage}")
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        query()
                        queryPurchases()
                    } else {
                        productCatalog.invalidate()
                        _purchase.value = PurchaseState.Error("Setup Error: ${result.debugMessage} (Code ${result.responseCode})")
                    }
                }
            },
        )
    }

    fun queryPurchases() {
        client.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val tiers = purchases
                    .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                    .mapNotNull { purchase -> purchase.products.firstNotNullOfOrNull(TipTier::fromProductId) }
                    .toSet()
                if (tiers.isNotEmpty()) {
                    _purchasedTiers.value = tiers
                }
            }
        }
    }

    fun refreshProducts() = query()

    private fun query() {
        val request = productCatalog.begin()
        Log.d("Billing", "Querying products...")
        val productList = TipTier.purchasableTiers.map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it.productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()
        client.queryProductDetailsAsync(params) { billingResult, queryResult ->
            Log.d("Billing", "Query result code: ${billingResult.responseCode}")
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                productCatalog.complete(request, queryResult.productDetailsList)
            } else {
                if (productCatalog.complete(request, emptyList())) {
                    _purchase.value = PurchaseState.Error("Query Error: ${billingResult.debugMessage} (Code ${billingResult.responseCode})")
                }
            }
        }
    }

    fun purchase(activity: Activity, details: ProductDetails, offerToken: String?) {
        if (details.productId == TipTier.KOYOMI.productId) return
        if (!productCatalog.isCurrent(details)) return
        val params = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .apply { offerToken?.let { setOfferToken(it) } }
            .build()
        val result = client.launchBillingFlow(activity, BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(params)).build())
        if (result.responseCode != BillingClient.BillingResponseCode.OK) _purchase.value = PurchaseState.Error(result.debugMessage)
    }

    fun simulateSuccess(tier: TipTier) {
        if (tier == TipTier.KOYOMI) return
        _purchasedTiers.value = _purchasedTiers.value + tier
        _purchase.value = PurchaseState.Success(tier)
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) { _purchase.value = PurchaseState.Cancelled; return }
        if (result.responseCode != BillingClient.BillingResponseCode.OK) { _purchase.value = PurchaseState.Error(result.debugMessage); return }
        purchases.orEmpty().filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }.forEach { purchase ->
            purchase.products.firstNotNullOfOrNull(TipTier::fromProductId)?.let { tier ->
                _purchasedTiers.value = _purchasedTiers.value + tier
                if (!purchase.isAcknowledged) {
                    val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    client.acknowledgePurchase(acknowledgeParams) { ackResult ->
                        _purchase.value = if (ackResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            PurchaseState.Success(tier)
                        } else {
                            PurchaseState.Error(ackResult.debugMessage)
                        }
                    }
                } else {
                    _purchase.value = PurchaseState.Success(tier)
                }
            }
        }
    }

    override fun close() {
        productCatalog.invalidate()
        client.endConnection()
    }
}

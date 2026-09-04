package com.mihiraki.pdfviewer.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface PurchaseState { data object Idle : PurchaseState; data object Cancelled : PurchaseState; data class Error(val message: String) : PurchaseState; data class Success(val tier: TipTier) : PurchaseState }

class BillingManager(context: Context) : PurchasesUpdatedListener, AutoCloseable {
    private val _products = MutableStateFlow<List<ProductDetails>>(emptyList()); val products = _products.asStateFlow()
    private val _purchase = MutableStateFlow<PurchaseState>(PurchaseState.Idle); val purchase = _purchase.asStateFlow()
    private val client = BillingClient.newBuilder(context).setListener(this).enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()).enableAutoServiceReconnection().build()

    fun connect() = client.startConnection(
        object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() = Unit
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) query() else _purchase.value =
                    PurchaseState.Error(result.debugMessage)
            }
        },
    )
    private fun query() {
        val entries = TipTier.entries.map { QueryProductDetailsParams.Product.newBuilder().setProductId(it.productId).setProductType(BillingClient.ProductType.INAPP).build() }
        client.queryProductDetailsAsync(QueryProductDetailsParams.newBuilder().setProductList(entries).build()) { result, details ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) _products.value = details.productDetailsList else _purchase.value = PurchaseState.Error(result.debugMessage)
        }
    }
    fun purchase(activity: Activity, details: ProductDetails) {
        val offer = details.oneTimePurchaseOfferDetailsList?.firstOrNull()?.offerToken
        val params = BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(details).apply {
            offer?.let { setOfferToken(it) }
        }.build()
        val result = client.launchBillingFlow(activity, BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(params)).build())
        if (result.responseCode != BillingClient.BillingResponseCode.OK) _purchase.value = PurchaseState.Error(result.debugMessage)
    }

    /** Simulates a successful purchase for debugging purposes. */
    fun simulateSuccess(tier: TipTier) {
        _purchase.value = PurchaseState.Success(tier)
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) { _purchase.value = PurchaseState.Cancelled; return }
        if (result.responseCode != BillingClient.BillingResponseCode.OK) { _purchase.value = PurchaseState.Error(result.debugMessage); return }
        purchases.orEmpty().filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }.forEach { purchase ->
            purchase.products.firstNotNullOfOrNull(TipTier::fromProductId)?.let { tier ->
                client.consumeAsync(ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()) { consumed, _ ->
                    _purchase.value = if (consumed.responseCode == BillingClient.BillingResponseCode.OK) PurchaseState.Success(tier) else PurchaseState.Error(consumed.debugMessage)
                }
            }
        }
    }
    override fun close() = client.endConnection()
}

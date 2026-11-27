package com.github.tyamada.mihirakipdfviewer_android.ui.screens

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.github.tyamada.mihirakipdfviewer_android.BuildConfig
import com.github.tyamada.mihirakipdfviewer_android.R
import com.github.tyamada.mihirakipdfviewer_android.billing.*
import com.github.tyamada.mihirakipdfviewer_android.util.UpdateManager
import com.github.tyamada.mihirakipdfviewer_android.viewmodel.ViewerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun TipScreen(vm: ViewerViewModel, back: () -> Unit) {
    val context = LocalContext.current; val manager = vm.billing
    val catalog by manager.catalog.collectAsState(); val purchase by manager.purchase.collectAsState()
    val products = catalog.products
    val purchasedTiers by manager.purchasedTiers.collectAsState()
    val viewerState by vm.state.collectAsState()
    val badgeDetails = rememberBadgeDetailsState()
    val isDebug = BuildConfig.DEBUG

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        manager.refreshProducts()
        manager.queryPurchases()
    }

    LaunchedEffect(purchasedTiers) {
        if (purchasedTiers.isNotEmpty()) {
            vm.updateSettings { s -> s.copy(purchasedTiers = s.purchasedTiers + purchasedTiers.map { it.name }) }
        }
    }

    LaunchedEffect(purchase) {
        if (purchase is PurchaseState.Success) {
            val tierName = (purchase as PurchaseState.Success).tier.name
            vm.updateSettings { s -> s.copy(purchasedTiers = s.purchasedTiers + tierName, purchasedTier = tierName) }
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.support)) }, navigationIcon = { IconButton(back) { Icon(Icons.Default.ArrowBack, stringResource(R.string.back)) } }) }) { p ->
        Column(
            modifier = Modifier.fillMaxSize().padding(p)
                .verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.tip_message))
            Text(stringResource(R.string.tip_free_notice))
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.koyomi_badge_title), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.koyomi_free_notice))
                    if ("KOYOMI" in viewerState.settings.ownedBadgeIds || TipTier.KOYOMI in purchasedTiers) {
                        OwnedBadgeButton("KOYOMI", badgeDetails, BadgeDetailsLocation.Support)
                    } else {
                        BadgeDetailsTextButton("KOYOMI", badgeDetails, stringResource(R.string.badge_view_details), R.id.badge_support_koyomi)
                    }
                }
            }
            if (BuildConfig.IS_SIDELOAD) {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(stringResource(R.string.sideload_support_notice))
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { UpdateManager.openPlayStore(context) }
                        ) {
                            Text(stringResource(R.string.sideload_open_play_store))
                        }
                    }
                }
            } else {
                TipTier.purchasableTiers.forEach { tier ->
                val product = products.firstOrNull { it.productId == tier.productId }
                val offer = product?.oneTimePurchaseOfferDetailsList?.firstOrNull()
                val priceText = offer?.formattedPrice
                    ?: if (isDebug) {
                        when (tier) {
                            TipTier.BRONZE -> "¥100 (Mock)"
                            TipTier.SILVER -> "¥500 (Mock)"
                            TipTier.GOLD -> "¥1000 (Mock)"
                            else -> stringResource(R.string.tip_price_unavailable)
                        }
                    } else stringResource(R.string.tip_price_unavailable)

                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val iconRes = when (tier) {
                                TipTier.BRONZE -> R.drawable.ic_tip_bronze
                                TipTier.SILVER -> R.drawable.ic_tip_silver
                                TipTier.GOLD -> R.drawable.ic_tip_gold
                                TipTier.KOYOMI -> R.drawable.ic_tip_koyomi
                            }
                            Image(
                                painter = painterResource(iconRes),
                                contentDescription = null,
                                modifier = Modifier.size(48.dp).align(Alignment.CenterVertically)
                            )
                            Column(Modifier.weight(1f)) {
                                val tierTitle = when (tier) {
                                    TipTier.KOYOMI -> stringResource(R.string.koyomi_badge_title)
                                    else -> tier.name
                                }
                                Text(tierTitle)
                                if (catalog.isLoading) {
                                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Text(priceText)
                                }
                            }
                        }
                        val isPurchased = purchasedTiers.contains(tier) || tier.name in viewerState.settings.allPurchasedTiers
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                if (isDebug && product == null) {
                                    manager.simulateSuccess(tier)
                                } else if (product != null && offer != null) {
                                    manager.purchase(context as Activity, product, offer.offerToken)
                                }
                            },
                            enabled = !catalog.isLoading && !isPurchased && (offer != null || (isDebug && product == null))
                        ) {
                            Text(if (isPurchased) stringResource(R.string.purchased) else stringResource(R.string.purchase))
                        }
                        if (isPurchased) OwnedBadgeButton(tier.name, badgeDetails, BadgeDetailsLocation.Support)
                    }
                }
            }
            }
            when (val s = purchase) {
                is PurchaseState.Success -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(stringResource(R.string.purchase_thanks, s.tier.name))
                        Text(stringResource(R.string.purchase_badge_notice), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
                is PurchaseState.Cancelled -> Text(stringResource(R.string.purchase_cancelled))
                is PurchaseState.Error -> Text(stringResource(R.string.purchase_failed), color = MaterialTheme.colorScheme.error)
                else -> Unit
            }
        }
    }
    BadgeDetailsHost(badgeDetails)

}

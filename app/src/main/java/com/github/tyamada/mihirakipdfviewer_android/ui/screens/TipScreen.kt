package com.github.tyamada.mihirakipdfviewer_android.ui.screens

import android.app.Activity
import androidx.compose.foundation.Image
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
import com.github.tyamada.mihirakipdfviewer_android.BuildConfig
import com.github.tyamada.mihirakipdfviewer_android.R
import com.github.tyamada.mihirakipdfviewer_android.billing.*
import com.github.tyamada.mihirakipdfviewer_android.viewmodel.ViewerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun TipScreen(vm: ViewerViewModel, back: () -> Unit) {
    val context = LocalContext.current; val manager = vm.billing
    val products by manager.products.collectAsState(); val purchase by manager.purchase.collectAsState()
    val purchasedTiers by manager.purchasedTiers.collectAsState()
    val isDebug = BuildConfig.DEBUG

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
        Column(Modifier.padding(p).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.tip_message))
            Text(stringResource(R.string.tip_free_notice))
            TipTier.entries.forEach { tier ->
                val product = products.firstOrNull { it.productId == tier.productId }
                val priceText = product?.oneTimePurchaseOfferDetailsList?.firstOrNull()?.formattedPrice
                    ?: if (isDebug) {
                        when (tier) {
                            TipTier.BRONZE -> "¥100 (Mock)"
                            TipTier.SILVER -> "¥500 (Mock)"
                            TipTier.GOLD -> "¥1000 (Mock)"
                        }
                    } else tier.productId

                ElevatedCard(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        val iconRes = when (tier) {
                            TipTier.BRONZE -> R.drawable.ic_tip_bronze
                            TipTier.SILVER -> R.drawable.ic_tip_silver
                            TipTier.GOLD -> R.drawable.ic_tip_gold
                        }
                        Image(
                            painter = painterResource(iconRes),
                            contentDescription = tier.name,
                            modifier = Modifier.size(48.dp).align(Alignment.CenterVertically)
                        )
                        Column(Modifier.weight(1f)) {
                            Text(tier.name)
                            Text(priceText)
                        }
                        Button(
                            onClick = {
                                if (isDebug && product == null) {
                                    manager.simulateSuccess(tier)
                                } else {
                                    product?.let { manager.purchase(context as Activity, it) }
                                }
                            },
                            enabled = isDebug || product != null
                        ) {
                            Text(stringResource(R.string.purchase))
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
}

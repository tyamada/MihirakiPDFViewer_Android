package com.mihiraki.pdfviewer.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mihiraki.pdfviewer.R
import com.mihiraki.pdfviewer.billing.*
import com.mihiraki.pdfviewer.viewmodel.ViewerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun TipScreen(vm: ViewerViewModel, back: () -> Unit) {
    val context = LocalContext.current; val manager = remember { BillingManager(context) }
    val products by manager.products.collectAsState(); val purchase by manager.purchase.collectAsState()
    val isDebug = com.mihiraki.pdfviewer.BuildConfig.DEBUG

    LaunchedEffect(purchase) {
        if (purchase is PurchaseState.Success) {
            vm.updateSettings { it.copy(purchasedTier = (purchase as PurchaseState.Success).tier.name) }
        }
    }

    DisposableEffect(manager) { manager.connect(); onDispose(manager::close) }
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.support)) }, navigationIcon = { IconButton(back) { Icon(Icons.Default.ArrowBack, stringResource(R.string.back)) } }) }) { p ->
        Column(Modifier.padding(p).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.tip_message)); TipTier.entries.forEach { tier ->
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
                        Text(tier.badge, style = MaterialTheme.typography.displaySmall)
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
            when (val s = purchase) { is PurchaseState.Success -> Text(stringResource(R.string.purchase_thanks, s.tier.name)); is PurchaseState.Cancelled -> Text(stringResource(R.string.purchase_cancelled)); is PurchaseState.Error -> Text(stringResource(R.string.purchase_failed), color = MaterialTheme.colorScheme.error); else -> Unit }
        }
    }
}

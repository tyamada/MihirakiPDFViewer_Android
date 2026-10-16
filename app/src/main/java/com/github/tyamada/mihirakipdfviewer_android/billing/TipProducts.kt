package com.github.tyamada.mihirakipdfviewer_android.billing

enum class TipTier(val productId: String, val badge: String) {
    BRONZE("tip_100", "🥉"),
    SILVER("tip_500", "🥈"),
    GOLD("tip_1000", "🥇"),
    KOYOMI("tip_v20_koyomi", "🌸");
    companion object { fun fromProductId(id: String) = entries.firstOrNull { it.productId == id } }
}

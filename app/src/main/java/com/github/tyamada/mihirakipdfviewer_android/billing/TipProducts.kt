package com.github.tyamada.mihirakipdfviewer_android.billing

enum class TipTier(val productId: String, val badge: String, val isFree: Boolean = false) {
    BRONZE("tip_100", "🥉"),
    SILVER("tip_500", "🥈"),
    GOLD("tip_1000", "🥇"),
    KOYOMI("tip_v20_koyomi", "🌸", true);
    companion object { fun fromProductId(id: String) = entries.firstOrNull { it.productId == id } }
}

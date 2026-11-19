package com.github.tyamada.mihirakipdfviewer_android.billing

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ProductCatalogSnapshot<T>(val products: List<T> = emptyList(), val isLoading: Boolean = false)

/** ProductDetails are short-lived: never display or buy from a superseded request. */
internal class ProductCatalogState<T> {
    private var generation = 0L
    private val mutable = MutableStateFlow(ProductCatalogSnapshot<T>())
    val state = mutable.asStateFlow()

    @Synchronized fun begin(): Long {
        generation++
        mutable.value = ProductCatalogSnapshot(isLoading = true)
        return generation
    }

    @Synchronized fun complete(request: Long, products: List<T>): Boolean {
        if (request != generation) return false
        mutable.value = ProductCatalogSnapshot(products.toList())
        return true
    }

    @Synchronized fun isCurrent(product: T): Boolean =
        !mutable.value.isLoading && mutable.value.products.any { it === product }

    @Synchronized fun invalidate() {
        generation++
        mutable.value = ProductCatalogSnapshot()
    }
}

package com.github.tyamada.mihirakipdfviewer_android

import com.github.tyamada.mihirakipdfviewer_android.billing.ProductCatalogState
import org.junit.Assert.*
import org.junit.Test

class ProductCatalogStateTest {
    private data class Offer(val currency: String, val price: Int)

    @Test fun returningFromAnotherBillingContextHidesOldPricesUntilFreshResponse() {
        val catalog = ProductCatalogState<Offer>()
        val taiwan = Offer("TWD", 60)
        catalog.complete(catalog.begin(), listOf(taiwan))
        assertTrue(catalog.isCurrent(taiwan))
        val request = catalog.begin()
        assertTrue(catalog.state.value.isLoading)
        assertTrue(catalog.state.value.products.isEmpty())
        assertFalse(catalog.isCurrent(taiwan))
        val macao = Offer("MOP", 16)
        catalog.complete(request, listOf(macao))
        assertEquals(listOf(macao), catalog.state.value.products)
        assertTrue(catalog.isCurrent(macao))
        assertFalse(catalog.isCurrent(taiwan))
    }

    @Test fun delayedOldCountryResponseCannotReplaceNewCountry() {
        val catalog = ProductCatalogState<Offer>()
        val old = catalog.begin()
        val current = catalog.begin()
        val macao = Offer("MOP", 16)
        assertTrue(catalog.complete(current, listOf(macao)))
        assertFalse(catalog.complete(old, listOf(Offer("JPY", 200))))
        assertEquals(listOf(macao), catalog.state.value.products)
    }

    @Test fun oldFailureCannotClearACompletedNewQuery() {
        val catalog = ProductCatalogState<Offer>()
        val old = catalog.begin()
        val current = catalog.begin()
        val offer = Offer("CAD", 279)
        catalog.complete(current, listOf(offer))
        assertFalse(catalog.complete(old, emptyList()))
        assertTrue(catalog.isCurrent(offer))
    }

    @Test fun failedFreshRequestCannotFallBackToAnOldPurchasableOffer() {
        val catalog = ProductCatalogState<Offer>()
        val offer = Offer("TWD", 60)
        catalog.complete(catalog.begin(), listOf(offer))
        catalog.complete(catalog.begin(), emptyList())
        assertFalse(catalog.state.value.isLoading)
        assertTrue(catalog.state.value.products.isEmpty())
        assertFalse(catalog.isCurrent(offer))
    }

    @Test fun disconnectInvalidatesInFlightResults() {
        val catalog = ProductCatalogState<Offer>()
        val request = catalog.begin()
        catalog.invalidate()
        assertFalse(catalog.complete(request, listOf(Offer("JPY", 200))))
        assertTrue(catalog.state.value.products.isEmpty())
        assertFalse(catalog.state.value.isLoading)
    }

    @Test fun equalPriceDoesNotAuthorizeTheSupersededProductDetailsObject() {
        val catalog = ProductCatalogState<Offer>()
        val old = Offer("TWD", 60)
        catalog.complete(catalog.begin(), listOf(old))
        val current = old.copy()
        val input = mutableListOf(current)
        catalog.complete(catalog.begin(), input)
        input.clear()
        assertTrue(catalog.isCurrent(current))
        assertFalse(catalog.isCurrent(old))
    }
}

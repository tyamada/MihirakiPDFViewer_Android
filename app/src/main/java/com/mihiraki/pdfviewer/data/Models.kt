package com.mihiraki.pdfviewer.data

enum class ReadingDirection { L2R, R2L }
enum class CoverMode { STANDARD, COMPATIBILITY }
enum class ViewerLayout { SINGLE, SPREAD }

data class ViewerSettings(
    val direction: ReadingDirection = ReadingDirection.L2R,
    val coverMode: CoverMode = CoverMode.STANDARD,
    val layout: ViewerLayout = ViewerLayout.SINGLE,
    val showCover: Boolean = false,
    val highQuality: Boolean = false,
    val sharpness: Float = 0f,
    val purchasedTier: String? = null,
)

data class DocumentInfo(
    val title: String = "", val author: String = "", val subject: String = "",
    val keywords: String = "", val version: String = "",
)

/** null means a deliberate blank; integers are zero-based PDF page indices. */
data class PageSpread(val left: Int?, val right: Int?)

data class SearchRect(val left: Float, val top: Float, val right: Float, val bottom: Float)
data class SearchHit(val pageIndex: Int, val rects: List<SearchRect>)

object SpreadPlanner {
    fun plan(pageCount: Int, direction: ReadingDirection, showCover: Boolean, mode: CoverMode): List<PageSpread> {
        if (pageCount <= 0) return emptyList()
        val result = mutableListOf<PageSpread>()
        var page = 0
        
        // Use showCover if in Standard mode, or if requested by other logic.
        val effectiveShowCover = if (mode == CoverMode.STANDARD) showCover else false

        if (effectiveShowCover) {
            // Standalone cover is always on the right side of the spread [null, page0]
            result += PageSpread(null, 0)
            page = 1
        }
        while (page < pageCount) {
            val first = page
            val second = (page + 1).takeIf { it < pageCount }
            result += if (direction == ReadingDirection.L2R) PageSpread(first, second) else PageSpread(second, first)
            page += 2
        }
        return result
    }
}

object DirectionDetector {
    fun fromMetadata(pageLayout: String?, direction: String?): ReadingDirection? {
        val normalizedDir = direction?.lowercase().orEmpty()
        if (normalizedDir.contains("r2l") || normalizedDir.contains("righttoleft")) return ReadingDirection.R2L
        if (normalizedDir.contains("l2r") || normalizedDir.contains("lefttoright")) return ReadingDirection.L2R
        val normalizedLayout = pageLayout?.lowercase().orEmpty()
        // TwoPageRight (odd on right) is standard for L2R documents.
        // TwoPageLeft (odd on left) is standard for R2L documents.
        if (normalizedLayout.contains("right")) return ReadingDirection.L2R
        if (normalizedLayout.contains("left")) return ReadingDirection.R2L
        return null
    }
}

object LayoutDetector {
    fun fromMetadata(pageLayout: String?): ViewerLayout? {
        val normalized = pageLayout?.lowercase().orEmpty()
        if (normalized.contains("two")) return ViewerLayout.SPREAD
        if (normalized.contains("single")) return ViewerLayout.SINGLE
        if (normalized.contains("one")) return ViewerLayout.SINGLE
        return null
    }

    fun shouldShowCover(pageLayout: String?): Boolean? {
        val normalized = pageLayout?.lowercase().orEmpty()
        if (!normalized.contains("two") && !normalized.contains("column")) return null
        // TwoPageRight/TwoColumnRight means page 1 is on the right. 
        // In L2R, this is a standalone cover. In R2L, this is a normal spread [2, 1].
        // Wait, let's re-verify R2L. If R2L and TwoPageRight, page 1 is on the right. 
        // If it's a spread [2, 1], page 1 is on the right. So No Cover.
        // If R2L and TwoPageLeft, page 1 is on the left.
        // If it's a spread [1, 2], page 1 is on the left. But this is L2R reading order.
        // If it's a standalone cover [1, blank], page 1 is on the left. This is R2L cover.
        
        // Actually, the most reliable mapping for many PDF tools is:
        // Right -> Show Cover (Page 1 alone on the right)
        // Left -> No Cover (Page 1 and 2 together)
        // This holds for both directions if we consider the "first" page's position.
        return if (normalized.contains("right")) true
        else if (normalized.contains("left")) false
        else null
    }
}

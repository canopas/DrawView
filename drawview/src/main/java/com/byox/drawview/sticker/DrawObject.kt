package com.byox.drawview.sticker

import android.graphics.Paint
import android.graphics.Path

internal data class DrawObject(
    val pathAndPaint: PathAndPaint?,
    val sticker: Sticker?,
    val drawType: DrawType
)

internal enum class DrawType {
    PATH,
    STICKER
}

internal data class PathAndPaint(
        val path: Path,
        val paint: Paint
)
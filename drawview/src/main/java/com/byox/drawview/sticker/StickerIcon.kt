package com.byox.drawview.sticker

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.view.MotionEvent

val DEFAULT_ICON_RADIUS = 14f * Resources.getSystem().displayMetrics.density

const val LEFT_TOP = 0
const val RIGHT_TOP = 1
const val LEFT_BOTTOM = 2
const val RIGHT_BOTTOM = 3

internal class StickerIcon(drawable: Drawable?, gravity: Int) : DrawableSticker(drawable!!), StickerIconListener {
    var iconRadius = DEFAULT_ICON_RADIUS
    var x = 0f
    var y = 0f

    var position = LEFT_TOP
    var iconListener: StickerIconListener? = null

    init {
        position = gravity
    }

    fun draw(canvas: Canvas, paint: Paint?) {
        canvas.drawCircle(x, y, iconRadius, paint!!)
        super.draw(canvas)
    }

    override fun onActionDown(stickerView: StickerView?, event: MotionEvent?) {
        iconListener?.onActionDown(stickerView, event)
    }

    override fun onActionMove(stickerView: StickerView, event: MotionEvent) {
        iconListener?.onActionMove(stickerView, event)
    }

    override fun onActionUp(stickerView: StickerView, event: MotionEvent?) {
        iconListener?.onActionUp(stickerView, event)
    }
}
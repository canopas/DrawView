package com.byox.drawview.sticker

import android.view.MotionEvent

internal interface StickerViewListener {
    fun onRemove()
    fun onDone(obj: DrawObject)
    fun onZoomAndRotate()
    fun onFlip()
    fun onClickStickerOutside(x: Float, y: Float)
    fun onTouchEvent(event: MotionEvent)
}
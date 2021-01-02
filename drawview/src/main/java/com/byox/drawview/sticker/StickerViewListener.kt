package com.byox.drawview.sticker

import android.view.MotionEvent

internal interface StickerViewListener {
    fun onRemove(currentSticker: Sticker?)
    fun onDone(obj: DrawObject)
    fun onZoomAndRotate()
    fun onFlip()
    fun onClickStickerOutside(x: Float, y: Float)
    fun onTouchEvent(event: MotionEvent)
    fun onTouchEvent(x: Float, y: Float)

}
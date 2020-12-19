package com.byox.drawview.sticker.events

import android.view.MotionEvent
import com.byox.drawview.sticker.StickerIconListener
import com.byox.drawview.sticker.StickerView

internal class ZoomIconEvent: StickerIconListener {
    override fun onActionDown(stickerView: StickerView?, event: MotionEvent?) {}
    override fun onActionMove(stickerView: StickerView, event: MotionEvent) {
        stickerView.zoomAndRotate(event)
    }
    override fun onActionUp(stickerView: StickerView, event: MotionEvent?) {}
}
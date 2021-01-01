package com.byox.drawview.sticker.events

import android.view.MotionEvent
import com.byox.drawview.sticker.StickerIconListener
import com.byox.drawview.sticker.StickerView

internal class DoneIconEvent : StickerIconListener {
    override fun onActionDown(stickerView: StickerView?, event: MotionEvent?) {}
    override fun onActionMove(stickerView: StickerView, event: MotionEvent) {}
    override fun onActionUp(stickerView: StickerView, event: MotionEvent?) {
        stickerView.done()
    }
}
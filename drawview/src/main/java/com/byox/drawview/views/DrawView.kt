package com.byox.drawview.views

import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.graphics.Paint.Cap
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.View.OnTouchListener
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.cardview.widget.CardView
import com.byox.drawview.R
import com.byox.drawview.dictionaries.DrawMove
import com.byox.drawview.enums.*
import com.byox.drawview.sticker.*
import com.byox.drawview.utils.BitmapUtils
import com.byox.drawview.utils.MatrixUtils
import com.byox.drawview.utils.SerializablePaint
import com.byox.drawview.utils.SerializablePath
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

/**
 * Created by Ing. Oscar G. Medina Cruz on 06/11/2016.
 *
 *
 * This view was created for draw or paint anything you want.
 *
 *
 *
 *
 * This view can be configurated for change draw color, width size, can use tools like pen, line, circle, square.
 *
 *
 * @author Ing. Oscar G. Medina Cruz
 */
class DrawView : FrameLayout, OnTouchListener {
    // CONSTANTS
    val TAG = "DrawView"

    // LISTENER
    private var onDrawViewListener: OnDrawViewListener? = null
    private var mScaleGestureDetector: ScaleGestureDetector? = null
    private var mGestureDetector: GestureDetector? = null

    // VARS
    var isForCamera = false
        private set
    var drawColor = 0
        private set
    var drawWidth = 0
        private set

    // GETTERS
    var drawAlpha = 0
        private set
    var isAntiAlias = false
        private set
    var isDither = false
        private set

    //    public Object getBackgroundImage() {
    //        return mBackgroundImage;
    //    }
    var paintStyle: Paint.Style? = null
        private set
    var lineCap: Cap? = null
        private set
    var fontFamily: Typeface? = null
        private set
    var fontSize = 0f
        private set
    private var backgroundDrawColor = -1

    private val mBackgroundImageBitmap: Bitmap? = null
    private var mCanvasClipBounds: Rect? = null
    private var mContentBitmap: Bitmap? = null
    private var mContentCanvas: Canvas? = null
    var isZoomEnabled = false
        private set
    private var mZoomFactor = 1.0f
    private var mZoomCenterX = -1.0f
    private var mZoomCenterY = -1.0f
    var maxZoomFactor = 8f
        private set
    var zoomRegionScale = 4f
        private set
    var zoomRegionScaleMin = 2f
        private set
    var zoomRegionScaleMax = 5f
        private set
    private var mFromZoomRegion = false
    private var mLastTouchEvent = -1
    var drawingMode: DrawingMode? = null
        private set
    var drawingTool: DrawingTool? = null
        private set
    private var mInitialDrawingOrientation: DrawingOrientation? = null
    private var mDrawMoveHistory: MutableList<DrawMove> = ArrayList()
    private var mDrawMoveHistoryIndex = -1 // 历史路径index
    private var mDrawMoveBackgroundIndex = -1 // background index
    private var mAuxRect: RectF? = null
    private var mEraserXefferMode: PorterDuffXfermode? = null
    private var mBackgroundPaint: SerializablePaint? = null
    private var mInvalidateRect: Rect? = null

    // VIEWS
    private var mZoomRegionCardView: CardView? = null
    private var mZoomRegionView: ZoomRegionView? = null
    /**
     * 返回绘制历史记录状态
     *
     * @return
     */
    /**
     * 绘制记录历史开关
     *
     * @param historySwitch true
     */
    var historySwitch = true // true 开启绘制历史；false 关闭

    /**
     * Default constructor
     *
     * @param context
     */
    constructor(context: Context?) : super(context!!) {
        initVars()
    }

    /**
     * Default constructor
     *
     * @param context
     * @param attrs
     */
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initVars()
        initAttributes(context, attrs)
    }

    /**
     * Default constructor
     *
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initVars()
        initAttributes(context, attrs)
    }

    /**
     * Default constructor
     *
     * @param context
     * @param attrs
     * @param defStyleAttr
     * @param defStyleRes
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        initVars()
        initAttributes(context, attrs)
    }

    /**
     * Draw custom content in the view
     *
     * @param canvas
     */
    override fun onDraw(canvas: Canvas) {
        val mContentCanvas = mContentCanvas ?: return
        mContentBitmap!!.eraseColor(Color.TRANSPARENT)
        if (isZoomEnabled) {
            canvas.save()
            canvas.scale(mZoomFactor, mZoomFactor, mZoomCenterX, mZoomCenterY)
        }

        // Draw canvas background
        mContentCanvas.drawRect(0f, 0f, mContentBitmap!!.width.toFloat(), mContentBitmap!!.height.toFloat(), mBackgroundPaint!!)
        if (mDrawMoveBackgroundIndex != -1 && mDrawMoveHistory.size > 0) {
            val drawMove = mDrawMoveHistory[mDrawMoveBackgroundIndex]
            drawBackgroundImage(drawMove, canvas)
        }
        for (i in 0 until mDrawMoveHistoryIndex + 1) {
            val drawMove = mDrawMoveHistory[i]
            when (drawMove.drawingMode) {
                DrawingMode.DRAW ->
                    when (drawMove.drawingTool) {
                        DrawingTool.PEN -> if (drawMove.drawingPath != null) mContentCanvas.drawPath(drawMove.drawingPath, drawMove.paint)
                        DrawingTool.LINE -> mContentCanvas.drawLine(drawMove.startX, drawMove.startY,
                                drawMove.endX, drawMove.endY, drawMove.paint)
                        DrawingTool.ARROW -> {
                            mContentCanvas.drawLine(drawMove.startX, drawMove.startY,
                                    drawMove.endX, drawMove.endY, drawMove.paint)
                            var angle = Math.toDegrees(Math.atan2((drawMove.endY - drawMove.startY).toDouble(), (
                                    drawMove.endX - drawMove.startX).toDouble())).toFloat() - 90
                            angle = if (angle < 0) angle + 360 else angle
                            val middleWidth = 8f + drawMove.paint.strokeWidth
                            val arrowHeadLarge = 30f + drawMove.paint.strokeWidth
                            mContentCanvas.save()
                            mContentCanvas.translate(drawMove.endX, drawMove.endY)
                            mContentCanvas.rotate(angle)
                            mContentCanvas.drawLine(0f, 0f, middleWidth, 0f, drawMove.paint)
                            mContentCanvas.drawLine(middleWidth, 0f, 0f, arrowHeadLarge, drawMove.paint)
                            mContentCanvas.drawLine(0f, arrowHeadLarge, -middleWidth, 0f, drawMove.paint)
                            mContentCanvas.drawLine(-middleWidth, 0f, 0f, 0f, drawMove.paint)
                            mContentCanvas.restore()
                        }
                        DrawingTool.RECTANGLE -> mContentCanvas.drawRect(drawMove.startX, drawMove.startY,
                                drawMove.endX, drawMove.endY, drawMove.paint)
                        DrawingTool.CIRCLE -> if (drawMove.endX > drawMove.startX) {
                            mContentCanvas.drawCircle(drawMove.startX, drawMove.startY,
                                    drawMove.endX - drawMove.startX, drawMove.paint)
                        } else {
                            mContentCanvas.drawCircle(drawMove.startX, drawMove.startY,
                                    drawMove.startX - drawMove.endX, drawMove.paint)
                        }
                        DrawingTool.ELLIPSE -> {
                            mAuxRect!![drawMove.endX - abs(drawMove.endX - drawMove.startX), drawMove.endY - abs(drawMove.endY - drawMove.startY), drawMove.endX + Math.abs(drawMove.endX - drawMove.startX)] = drawMove.endY + Math.abs(drawMove.endY - drawMove.startY)
                            mContentCanvas.drawOval(mAuxRect!!, drawMove.paint)
                        }
                    }
                DrawingMode.TEXT -> {
                    Log.e("DrawView ", "OnDraw : Text")
                    if (!drawMove.text.isNullOrEmpty()) {
                        if (!drawMove.isTextDone) {
                            val textSticker = TextSticker(context, null)
                            textSticker.setText(drawMove.text)
                            textSticker.setAlpha(255)
                            textSticker.setTextColor(drawMove.paint.color)
                            drawMove.setSticker(textSticker)
                            textSticker.resizeText()
                            mStickerView.visibility = VISIBLE
                            mStickerView.addSticker(textSticker)
                        } else {
                            val textSticker: TextSticker? = drawMove.textSticker
                            textSticker?.setTextColor(drawMove.paint.color)
                            textSticker?.draw(mContentCanvas)
                        }
                    }
                }
                DrawingMode.ERASER -> if (drawMove.drawingPath != null) {
                    drawMove.paint.xfermode = mEraserXefferMode
                    mContentCanvas.drawPath(drawMove.drawingPath, drawMove.paint)
                    drawMove.paint.xfermode = null
                }
            }

            if (i == mDrawMoveHistory.size - 1) onDrawViewListener?.onAllMovesPainted()
        }
        canvas.getClipBounds(mCanvasClipBounds)
        canvas.drawBitmap(mContentBitmap!!, 0f, 0f, null)
        if (isZoomEnabled) {
            canvas.restore()
            if (!mFromZoomRegion) {
                mZoomRegionView?.drawZoomRegion(mContentBitmap, mCanvasClipBounds, 4f)
            }
        }
        super.onDraw(canvas)
    }

    /**
     * Handle touch events in the view
     *
     * @param view
     * @param motionEvent
     * @return
     */
    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        if (!historySwitch) {
            return false
        }
        if (isZoomEnabled) {
            mScaleGestureDetector?.onTouchEvent(motionEvent)
            mGestureDetector?.onTouchEvent(motionEvent)
        }
        val touchX = motionEvent.x / mZoomFactor + mCanvasClipBounds!!.left
        val touchY = motionEvent.y / mZoomFactor + mCanvasClipBounds!!.top
        var lastMoveIndex = 0
        if (motionEvent.pointerCount == 1) {
            when (motionEvent.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    mLastTouchEvent = MotionEvent.ACTION_DOWN
                    if (onDrawViewListener != null) onDrawViewListener!!.onStartDrawing()
                    if (mDrawMoveHistoryIndex >= -1 &&
                            mDrawMoveHistoryIndex < mDrawMoveHistory.size - 1)
                        mDrawMoveHistory = mDrawMoveHistory.subList(0, mDrawMoveHistoryIndex + 1)
                    mDrawMoveHistory.add(DrawMove.newInstance()
                            .setPaint(newPaintParams)
                            .setStartX(touchX).setStartY(touchY)
                            .setEndX(touchX).setEndY(touchY)
                            .setDrawingMode(drawingMode).setDrawingTool(drawingTool))
                    Log.e("Draw view : ", "ACTION_DOWN")
                    lastMoveIndex = mDrawMoveHistory.size - 1

//                    Paint currentPaint = mDrawMoveHistory.get(mDrawMoveHistory.size() - 1).getPaint();
//                    currentPaint.setStrokeWidth(currentPaint.getStrokeWidth() / mZoomFactor);
//                    mDrawMoveHistory.get(mDrawMoveHistory.size() - 1).setPaint(currentPaint);
                    mDrawMoveHistoryIndex++
                    if (drawingTool == DrawingTool.PEN || drawingMode == DrawingMode.ERASER) {
                        val path = SerializablePath()
                        path.moveTo(touchX, touchY)
                        path.lineTo(touchX, touchY)
                        mDrawMoveHistory[lastMoveIndex].setDrawingPathList(path)
                    }
                }
                MotionEvent.ACTION_MOVE -> if (mLastTouchEvent == MotionEvent.ACTION_DOWN ||
                        mLastTouchEvent == MotionEvent.ACTION_MOVE) {
                    mLastTouchEvent = MotionEvent.ACTION_MOVE
                    lastMoveIndex = mDrawMoveHistory.size - 1
                    if (mDrawMoveHistory.size > 0) {
                        mDrawMoveHistory[lastMoveIndex].setEndX(touchX).endY = touchY
                        if (drawingTool == DrawingTool.PEN || drawingMode == DrawingMode.ERASER) {
                            var i = 0
                            while (i < motionEvent.historySize) {
                                val historicalX = motionEvent.getHistoricalX(i) / mZoomFactor + mCanvasClipBounds!!.left
                                val historicalY = motionEvent.getHistoricalY(i) / mZoomFactor + mCanvasClipBounds!!.top
                                mDrawMoveHistory[lastMoveIndex].drawingPath.lineTo(historicalX, historicalY)
                                i++
                            }
                            mDrawMoveHistory[lastMoveIndex].drawingPath.lineTo(touchX, touchY)
                        }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    lastMoveIndex = mDrawMoveHistory.size - 1
                    if (mLastTouchEvent == MotionEvent.ACTION_DOWN) {
                    } else if (mLastTouchEvent == MotionEvent.ACTION_MOVE) {
                        mLastTouchEvent = -1
                        if (mDrawMoveHistory.size > 0) {
                            mDrawMoveHistory[lastMoveIndex].setEndX(touchX).endY = touchY
                            if (drawingTool == DrawingTool.PEN || drawingMode == DrawingMode.ERASER) {
                                var i = 0
                                while (i < motionEvent.historySize) {
                                    val historicalX = motionEvent.getHistoricalX(i) / mZoomFactor + mCanvasClipBounds!!.left
                                    val historicalY = motionEvent.getHistoricalY(i) / mZoomFactor + mCanvasClipBounds!!.top
                                    mDrawMoveHistory[lastMoveIndex].drawingPath.lineTo(historicalX, historicalY)
                                    i++
                                }
                                mDrawMoveHistory[lastMoveIndex].drawingPath.lineTo(touchX, touchY)
                            }
                        }
                    }
                    if (drawingMode == DrawingMode.TEXT) {
                        if (!mStickerView.hasText()) {
                            onDrawViewListener?.onRequestText()
                        }
                    }
                    onDrawViewListener?.onEndDrawing()
                }
                else -> return false
            }
        } else {
            mLastTouchEvent = -1
        }
        if (mDrawMoveHistory.size > 0) {
            mInvalidateRect = Rect(
                    (touchX - mDrawMoveHistory[lastMoveIndex].paint.strokeWidth * 2).toInt(),
                    (touchY - mDrawMoveHistory[lastMoveIndex].paint.strokeWidth * 2).toInt(),
                    (touchX + mDrawMoveHistory[lastMoveIndex].paint.strokeWidth * 2).toInt(),
                    (touchY + mDrawMoveHistory[lastMoveIndex].paint.strokeWidth * 2).toInt())
        }
        this.invalidate(mInvalidateRect!!.left, mInvalidateRect!!.top, mInvalidateRect!!.right, mInvalidateRect!!.bottom)
        return true
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable("superState", super.onSaveInstanceState())
        bundle.putInt("drawMoveHistorySize", mDrawMoveHistory.size)
        for (i in mDrawMoveHistory.indices) {
            bundle.putSerializable("mDrawMoveHistory$i", mDrawMoveHistory[i])
        }
        bundle.putInt("mDrawMoveHistoryIndex", mDrawMoveHistoryIndex)
        bundle.putInt("mDrawMoveBackgroundIndex", mDrawMoveBackgroundIndex)
        bundle.putSerializable("mDrawingMode", drawingMode)
        bundle.putSerializable("mDrawingTool", drawingTool)
        bundle.putSerializable("mInitialDrawingOrientation", mInitialDrawingOrientation)
        bundle.putInt("mDrawColor", drawColor)
        bundle.putInt("mDrawWidth", drawWidth)
        bundle.putInt("mDrawAlpha", drawAlpha)
        bundle.putInt("mBackgroundColor", backgroundDrawColor)
        bundle.putBoolean("mAntiAlias", isAntiAlias)
        bundle.putBoolean("mDither", isDither)
        bundle.putFloat("mFontSize", fontSize)
        bundle.putSerializable("mPaintStyle", paintStyle)
        bundle.putSerializable("mLineCap", lineCap)
        bundle.putInt("mFontFamily",
                if (fontFamily === Typeface.DEFAULT) 0 else if (fontFamily === Typeface.MONOSPACE) 1 else if (fontFamily === Typeface.SANS_SERIF) 2 else if (fontFamily === Typeface.SERIF) 3 else 0)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        var state: Parcelable? = state
        if (state is Bundle) {
            val bundle = state
            for (i in 0 until bundle.getInt("drawMoveHistorySize")) {
                mDrawMoveHistory.add(bundle.getSerializable("mDrawMoveHistory$i") as DrawMove)
            }
            mDrawMoveHistoryIndex = bundle.getInt("mDrawMoveHistoryIndex")
            mDrawMoveBackgroundIndex = bundle.getInt("mDrawMoveBackgroundIndex")
            drawingMode = bundle.getSerializable("mDrawingMode") as DrawingMode?
            drawingTool = bundle.getSerializable("mDrawingTool") as DrawingTool?
            mInitialDrawingOrientation = bundle.getSerializable("mInitialDrawingOrientation") as DrawingOrientation?
            drawColor = bundle.getInt("mDrawColor")
            drawWidth = bundle.getInt("mDrawWidth")
            drawAlpha = bundle.getInt("mDrawAlpha")
            backgroundDrawColor = bundle.getInt("mBackgroundColor")
            isAntiAlias = bundle.getBoolean("mAntiAlias")
            isDither = bundle.getBoolean("mDither")
            fontSize = bundle.getFloat("mFontSize")
            paintStyle = bundle.getSerializable("mPaintStyle") as Paint.Style?
            lineCap = bundle.getSerializable("mLineCap") as Cap?
            fontFamily = if (bundle.getInt("mFontFamily") == 0) Typeface.DEFAULT else if (bundle.getInt("mFontFamily") == 1) Typeface.MONOSPACE else if (bundle.getInt("mFontFamily") == 2) Typeface.SANS_SERIF else if (bundle.getInt("mFontFamily") == 3) Typeface.SERIF else Typeface.DEFAULT
            state = bundle.getParcelable("superState")
        }
        super.onRestoreInstanceState(state)
    }
    // PRIVATE METHODS
    /**
     * Initialize general vars for the view
     */
    private fun initVars() {
        mDrawMoveHistory = ArrayList()
        mScaleGestureDetector = ScaleGestureDetector(context, ScaleGestureListener())
        mGestureDetector = GestureDetector(context, GestureListener())
        mCanvasClipBounds = Rect()
        mAuxRect = RectF()
        mEraserXefferMode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        setOnTouchListener(this)
        viewTreeObserver.addOnGlobalLayoutListener(
                object : OnGlobalLayoutListener {
                    @SuppressLint("NewApi")
                    override fun onGlobalLayout() {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            viewTreeObserver
                                    .removeGlobalOnLayoutListener(this)
                        } else {
                            viewTreeObserver
                                    .removeOnGlobalLayoutListener(this)
                        }
                        initZoomRegionView()
                    }
                })
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        addView(mStickerView, params)
        mStickerView.visibility = GONE
    }

    private val mStickerView = StickerView(context, object : StickerViewListener {
        override fun onRemove(currentSticker: Sticker?) {
            undo()
        }

        override fun onDone(obj: DrawObject) {
            findLastText()?.isTextDone = true
            invalidate()
        }

        override fun onZoomAndRotate() {
        }

        override fun onFlip() {
        }

        override fun onTouchEvent(x: Float, y: Float) {
        }

        override fun onClickStickerOutside(x: Float, y: Float) {
        }

        override fun onTouchEvent(motionEvent: MotionEvent) {
        }
    })

    private fun findLastText(): DrawMove? {
        return mDrawMoveHistory.findLast { it.drawingMode == DrawingMode.TEXT }
    }

    /**
     * Init the ZoomRegionView for navigate into image when user zoom in
     */
    private fun initZoomRegionView() {
        if (mZoomRegionView == null) {
            val init = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            mContentBitmap = init.copy(Bitmap.Config.ARGB_8888, true)
            init.recycle()
            mContentCanvas = Canvas(mContentBitmap!!)
            val layoutParams = LayoutParams(width / 4, height / 4,
                    Gravity.TOP or Gravity.END)
            layoutParams.setMargins(12, 12, 12, 12)
            mZoomRegionCardView = CardView(context)
            mZoomRegionCardView!!.layoutParams = layoutParams
            mZoomRegionCardView!!.preventCornerOverlap = true
            mZoomRegionCardView!!.radius = 0f
            mZoomRegionCardView!!.useCompatPadding = true
            mZoomRegionCardView!!.visibility = INVISIBLE
            val childLayoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)
            mZoomRegionView = ZoomRegionView(context)
            mZoomRegionView!!.layoutParams = childLayoutParams
            mZoomRegionView!!.scaleType = ImageView.ScaleType.CENTER_CROP
            mZoomRegionView!!.setOnZoomRegionListener { newRect ->
                mFromZoomRegion = true
                mZoomCenterX = (newRect.centerX() * 4).toFloat()
                mZoomCenterY = (newRect.centerY() * 4).toFloat()
                invalidate()
            }
            mZoomRegionCardView!!.addView(mZoomRegionView)
            addView(mZoomRegionCardView)
        }
    }

    /**
     * Initialize view attributes
     *
     * @param context
     * @param attrs
     */
    private fun initAttributes(context: Context, attrs: AttributeSet?) {
        val typedArray = context.theme.obtainStyledAttributes(
                attrs, R.styleable.DrawView, 0, 0)
        try {
            drawColor = typedArray.getColor(R.styleable.DrawView_dv_draw_color, Color.BLACK)
            drawWidth = typedArray.getInteger(R.styleable.DrawView_dv_draw_width, 3)
            drawAlpha = typedArray.getInteger(R.styleable.DrawView_dv_draw_alpha, 255)
            isAntiAlias = typedArray.getBoolean(R.styleable.DrawView_dv_draw_anti_alias, true)
            isDither = typedArray.getBoolean(R.styleable.DrawView_dv_draw_dither, true)

            when (typedArray.getInteger(R.styleable.DrawView_dv_draw_style, 2)) {
                0 -> this.paintStyle = Paint.Style.FILL
                1 -> this.paintStyle = Paint.Style.FILL_AND_STROKE
                2 -> this.paintStyle = Paint.Style.STROKE
            }

            when (typedArray.getInteger(R.styleable.DrawView_dv_draw_corners, 2)) {
                0 -> lineCap = Paint.Cap.BUTT
                1 -> lineCap = Cap.ROUND
                2 -> lineCap = Cap.SQUARE
            }
            when (typedArray.getInteger(R.styleable.DrawView_dv_draw_font_family, 0)) {
                0 -> fontFamily = Typeface.DEFAULT
                1 -> fontFamily = Typeface.MONOSPACE
                2 -> fontFamily = Typeface.SANS_SERIF
                3 -> fontFamily = Typeface.SERIF
            }

            fontSize = typedArray.getInteger(R.styleable.DrawView_dv_draw_font_size, 12).toFloat()
            isForCamera = typedArray.getBoolean(R.styleable.DrawView_dv_draw_is_camera, false)

            val orientation = typedArray.getInteger(R.styleable.DrawView_dv_draw_orientation,
                    if (width > height) 1 else 0)

            mInitialDrawingOrientation = DrawingOrientation.values()[orientation]

            if (background != null && !isForCamera) try {
                backgroundDrawColor = (background as ColorDrawable).color
                setBackgroundColor(Color.TRANSPARENT)
            } catch (e: Exception) {
                e.printStackTrace()
                setBackgroundColor(Color.TRANSPARENT)
                backgroundDrawColor = (background as ColorDrawable).color
                setBackgroundResource(R.drawable.drawable_transparent_pattern)
            } else {
                setBackgroundColor(Color.TRANSPARENT)
                backgroundDrawColor = (background as ColorDrawable).color
                if (!isForCamera) setBackgroundResource(R.drawable.drawable_transparent_pattern)
            }

            mBackgroundPaint = SerializablePaint()
            mBackgroundPaint?.style = Paint.Style.FILL
            mBackgroundPaint?.color = if (backgroundDrawColor != -1) backgroundDrawColor else Color.TRANSPARENT
            drawingTool = DrawingTool.values()[typedArray.getInteger(R.styleable.DrawView_dv_draw_tool, 0)]
            drawingMode = DrawingMode.values()[typedArray.getInteger(R.styleable.DrawView_dv_draw_mode, 0)]
            isZoomEnabled = typedArray.getBoolean(R.styleable.DrawView_dv_draw_enable_zoom, false)
            zoomRegionScale = typedArray.getFloat(R.styleable.DrawView_dv_draw_zoomregion_scale, zoomRegionScale)
            zoomRegionScaleMin = typedArray.getFloat(R.styleable.DrawView_dv_draw_zoomregion_minscale, zoomRegionScaleMin)
            zoomRegionScaleMax = typedArray.getFloat(R.styleable.DrawView_dv_draw_zoomregion_maxscale, zoomRegionScaleMax)
        } finally {
            typedArray.recycle()
        }
    }

    /**
     * New paint parameters
     *
     * @return new paint parameters for initialize drawing
     */
    private val newPaintParams: SerializablePaint
        private get() {
            val paint = SerializablePaint()
            if (drawingMode == DrawingMode.ERASER) {
                if (drawingTool != DrawingTool.PEN) {
                    Log.i(TAG, "For use eraser drawing mode is necessary to use pen tool")
                    drawingTool = DrawingTool.PEN
                }
                paint.color = backgroundDrawColor
            } else {
                paint.color = drawColor
            }
            paint.style = paintStyle
            paint.isDither = isDither
            paint.strokeWidth = drawWidth.toFloat()
            paint.alpha = drawAlpha
            paint.isAntiAlias = isAntiAlias
            paint.strokeCap = lineCap
            paint.typeface = fontFamily
            paint.textSize = fontSize
            return paint
        }
    // PUBLIC METHODS
    /**
     * Current paint parameters
     *
     * @return current paint parameters
     */
    val currentPaintParams: SerializablePaint
        get() {
            val currentPaint: SerializablePaint
            if (mDrawMoveHistory.size > 0 && mDrawMoveHistoryIndex >= 0) {
                currentPaint = SerializablePaint()
                currentPaint.color = mDrawMoveHistory[mDrawMoveHistoryIndex].paint.color
                currentPaint.style = mDrawMoveHistory[mDrawMoveHistoryIndex].paint.style
                currentPaint.isDither = mDrawMoveHistory[mDrawMoveHistoryIndex].paint.isDither
                currentPaint.strokeWidth = mDrawMoveHistory[mDrawMoveHistoryIndex].paint.strokeWidth
                currentPaint.alpha = mDrawMoveHistory[mDrawMoveHistoryIndex].paint.alpha
                currentPaint.isAntiAlias = mDrawMoveHistory[mDrawMoveHistoryIndex].paint.isAntiAlias
                currentPaint.strokeCap = mDrawMoveHistory[mDrawMoveHistoryIndex].paint.strokeCap
                currentPaint.typeface = mDrawMoveHistory[mDrawMoveHistoryIndex].paint.typeface
                currentPaint.textSize = fontSize
            } else {
                currentPaint = SerializablePaint()
                currentPaint.color = drawColor
                currentPaint.style = paintStyle
                currentPaint.isDither = isDither
                currentPaint.strokeWidth = drawWidth.toFloat()
                currentPaint.alpha = drawAlpha
                currentPaint.isAntiAlias = isAntiAlias
                currentPaint.strokeCap = lineCap
                currentPaint.typeface = fontFamily
                currentPaint.textSize = 24f
            }
            return currentPaint
        }

    /**
     * Restart all the parameters and drawing history
     *
     * @return if the draw view can be restarted
     */
    fun restartDrawing(): Boolean {
        if (mDrawMoveHistory != null) {
            mStickerView.remove()
            mDrawMoveHistory.clear()
            mDrawMoveHistoryIndex = -1
            mDrawMoveBackgroundIndex = -1
            invalidate()
            onDrawViewListener?.onClearDrawing()
            return true
        }
        invalidate()
        return false
    }

    /**
     * 清空绘制记录，不清空背景图片
     */
    fun clearHistory(): Boolean {
        if (mDrawMoveHistory != null) {
            if (mDrawMoveBackgroundIndex != -1) {
                val drawMove = mDrawMoveHistory[mDrawMoveBackgroundIndex]
                mDrawMoveHistory.clear()
                mDrawMoveHistory.add(drawMove)
                mDrawMoveHistoryIndex = 0
                mDrawMoveBackgroundIndex = 0
                invalidate()
            } else {
                mDrawMoveHistory.clear()
                mDrawMoveHistoryIndex = -1
                mDrawMoveBackgroundIndex = -1
                invalidate()
            }
            //            if (onDrawViewListener != null)
//                onDrawViewListener.onClearDrawing();
            return true
        }
        invalidate()
        return false
    }

    /**
     * Undo last drawing action
     *
     * @return if the view can do the undo action
     */
    fun undo(): Boolean {
        if (mDrawMoveHistoryIndex > -1 &&
                mDrawMoveHistory.size > 0) {
            mDrawMoveHistoryIndex--
            mDrawMoveBackgroundIndex = -1
            val lastText = findLastText()
            if (lastText != null && !lastText.isTextDone) {
                mStickerView.remove()
            }
            for (i in 0 until mDrawMoveHistoryIndex + 1) {
                if (mDrawMoveHistory[i].backgroundImage != null) {
                    mDrawMoveBackgroundIndex = i
                }
            }
            invalidate()
            return true
        }
        invalidate()
        return false
    }

    /**
     * Check if the draw view can do undo action
     *
     * @return if the view can do the undo action
     */
    fun canUndo(): Boolean {
        return mDrawMoveHistoryIndex > -1 &&
                mDrawMoveHistory.size > 0
    }

    /**
     * Redo preview action
     *
     * @return if the view can do the redo action
     */
    fun redo(): Boolean {
        if (mDrawMoveHistoryIndex <= mDrawMoveHistory.size - 1) {
            mDrawMoveHistoryIndex++
            mDrawMoveBackgroundIndex = -1
            for (i in 0 until mDrawMoveHistoryIndex + 1) {
                if (mDrawMoveHistory[i].backgroundImage != null) {
                    mDrawMoveBackgroundIndex = i
                }
            }
            invalidate()
            return true
        }
        invalidate()
        return false
    }

    /**
     * Check if the view can do the redo action
     *
     * @return if the view can do the redo action
     */
    fun canRedo(): Boolean {
        return mDrawMoveHistoryIndex < mDrawMoveHistory.size - 1
    }

    /**
     * Create capture of the drawing view as bitmap or as byte array
     *
     * @param drawingCapture
     * @return Object in form of bitmap or byte array
     */
    fun createCapture(drawingCapture: DrawingCapture?): Array<Any?>? {
        var result: Array<Any?>? = null
        val bgBimtap = backgroundImageBitmap
        val combinedBitmap = if (bgBimtap != null) BitmapUtils.GetCombinedBitmaps(bgBimtap, mContentBitmap,
                mContentBitmap!!.width, mContentBitmap!!.height) else mContentBitmap!!
        when (drawingCapture) {
            DrawingCapture.BITMAP -> {
                result = arrayOfNulls(2)
                result[0] = combinedBitmap
                result[1] = if (mBackgroundPaint!!.color == Color.TRANSPARENT) "PNG" else "JPG"
            }
            DrawingCapture.BYTES -> {
                result = arrayOfNulls(2)
                val stream = ByteArrayOutputStream()
                combinedBitmap.compress(
                        if (mBackgroundPaint!!.color == Color.TRANSPARENT) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG,
                        100, stream)
                result[0] = stream.toByteArray()
                result[1] = if (mBackgroundPaint!!.color == Color.TRANSPARENT) "PNG" else "JPG"
            }
        }
        return result
    }

    private val backgroundImageBitmap: Bitmap?
        private get() {
            if (mDrawMoveBackgroundIndex != -1 && mDrawMoveHistory != null && mDrawMoveHistory.size > 0) {
                val drawMove = mDrawMoveHistory[mDrawMoveBackgroundIndex]
                return BitmapFactory.decodeByteArray(drawMove.backgroundImage, 0,
                        drawMove.backgroundImage.size)
            }
            return null
        }

    fun createCapture(drawingCapture: DrawingCapture?, cameraView: CameraView): Array<Any?>? {
        var result: Array<Any?>? = null
        when (drawingCapture) {
            DrawingCapture.BITMAP -> {
                result = arrayOfNulls(2)
                val cameraBitmap = cameraView.getCameraFrame(drawingCapture) as Bitmap
                result[0] = BitmapUtils.GetCombinedBitmaps(cameraBitmap, mContentBitmap,
                        mContentBitmap!!.width, mContentBitmap!!.height)
                cameraBitmap.recycle()
                result[1] = "JPG"
            }
            DrawingCapture.BYTES -> {
                result = arrayOfNulls(2)
                val stream = ByteArrayOutputStream()
                val cameraBytes = cameraView.getCameraFrame(drawingCapture) as ByteArray
                val cameraBitmap = BitmapFactory.decodeByteArray(cameraBytes, 0, cameraBytes.size)
                val resultBitmap = BitmapUtils.GetCombinedBitmaps(cameraBitmap, mContentBitmap,
                        mContentBitmap!!.width, mContentBitmap!!.height)
                resultBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                resultBitmap.recycle()
                result[0] = stream.toByteArray()
                result[1] = "JPG"
            }
        }
        return result
    }

    /**
     * Refresh the text of the last movement item
     *
     * @param newText
     */
    fun refreshLastText(newText: String) {
        if (mDrawMoveHistory.isNullOrEmpty()) return
        val drawMove = mDrawMoveHistory[mDrawMoveHistory.size - 1]

        if (drawMove.drawingMode == DrawingMode.TEXT) {
            if (drawMove.textSticker == null) {
                drawMove.text = newText
            }
            invalidate()
        } else Log.e(TAG, "The last item that you want to refresh text isn't TEXT element.")
    }

    /**
     * Delete las history element, this can help for cancel the text request.
     */
    fun cancelTextRequest() {
        if (mDrawMoveHistory != null && mDrawMoveHistory.size > 0) {
            mDrawMoveHistory.removeAt(mDrawMoveHistory.size - 1)
            mDrawMoveHistoryIndex--
        }
    }

    val isDrawViewEmpty: Boolean
        get() = mDrawMoveHistory == null || mDrawMoveHistory.size == 0
    // SETTERS
    /**
     * Set the new draw parametters easily
     *
     * @param paint
     * @return this instance of the view
     */
    fun refreshAttributes(paint: SerializablePaint): DrawView {
        drawColor = paint.color
        paintStyle = paint.style
        isDither = paint.isDither
        drawWidth = paint.strokeWidth.toInt()
        drawAlpha = paint.alpha
        isAntiAlias = paint.isAntiAlias
        lineCap = paint.strokeCap
        fontFamily = paint.typeface
        fontSize = paint.textSize
        return this
    }

    /**
     * Set the current alpha value for the drawing
     *
     * @param drawAlpha
     * @return this instance of the view
     */
    fun setDrawAlpha(drawAlpha: Int): DrawView {
        this.drawAlpha = drawAlpha
        return this
    }

    /**
     * Set the current draw color for drawing
     *
     * @param drawColor
     * @return this instance of the view
     */
    fun setDrawColor(drawColor: Int): DrawView {
        this.drawColor = drawColor
        return this
    }

    /**
     * Set the current draw width
     *
     * @param drawWidth
     * @return this instance of the view
     */
    fun setDrawWidth(drawWidth: Int): DrawView {
        this.drawWidth = drawWidth
        return this
    }

    /**
     * Set the current draw mode like draw, text or eraser
     *
     * @param drawingMode
     * @return this instance of the view
     */
    fun setDrawingMode(drawingMode: DrawingMode?): DrawView {
        this.drawingMode = drawingMode
        return this
    }

    /**
     * Set the current draw tool like pen, line, circle, rectangle, circle
     *
     * @param drawingTool
     * @return this instance of the view
     */
    fun setDrawingTool(drawingTool: DrawingTool?): DrawView {
        this.drawingTool = drawingTool
        return this
    }

    /**
     * Set the current background color of draw view
     *
     * @param backgroundColor
     * @return this instance of the view
     */
    fun setBackgroundDrawColor(backgroundColor: Int): DrawView {
        this.backgroundDrawColor = backgroundColor
        return this
    }

    /**
     * Set the current paint style like fill, fill_stroke or stroke
     *
     * @param paintStyle
     * @return this instance of the view
     */
    fun setPaintStyle(paintStyle: Paint.Style?): DrawView {
        this.paintStyle = paintStyle
        return this
    }

    /**
     * Set the current line cap like round, square or butt
     *
     * @param lineCap
     * @return this instance of the view
     */
    fun setLineCap(lineCap: Cap?): DrawView {
        this.lineCap = lineCap
        return this
    }

    /**
     * Set the current typeface for the view when we like to draw text
     *
     * @param fontFamily
     * @return this instance of the view
     */
    fun setFontFamily(fontFamily: Typeface?): DrawView {
        this.fontFamily = fontFamily
        return this
    }

    /**
     * Set the current font size for the view when we like to draw text
     *
     * @param fontSize
     * @return this instance of the view
     */
    fun setFontSize(fontSize: Float): DrawView {
        this.fontSize = fontSize
        return this
    }

    /**
     * Set the current anti alias value for the view
     *
     * @param antiAlias
     * @return this instance of the view
     */
    fun setAntiAlias(antiAlias: Boolean): DrawView {
        isAntiAlias = antiAlias
        return this
    }

    /**
     * Set the current dither value for the view
     *
     * @param dither
     * @return this instance of the view
     */
    fun setDither(dither: Boolean): DrawView {
        isDither = dither
        return this
    }

    /**
     * Enables the zoom
     *
     * @param zoomEnabled Value that indicates if the Zoom is enabled
     * @return this instance of the view
     */
    fun setZoomEnabled(zoomEnabled: Boolean): DrawView {
        isZoomEnabled = zoomEnabled
        return this
    }

    /**
     * Set if the draw view is used for camera
     *
     * @param isForCamera Value that indicates if the draw view is for camera
     * @return this instance of the view
     */
    fun setIsForCamera(isForCamera: Boolean): DrawView {
        this.isForCamera = isForCamera
        return this
    }

    /**
     * Set the customized background color for the view
     *
     * @param backgroundColor The background color for the view
     * @return this instance of the view
     */
    fun setDrawViewBackgroundColor(backgroundColor: Int): DrawView {
        this.backgroundDrawColor = backgroundColor
        return this
    }

    /**
     * Set the background paint for the view
     *
     * @param backgroundPaint The background paint for the view
     * @return this instance of the view
     */
    fun setBackgroundPaint(backgroundPaint: SerializablePaint?): DrawView {
        mBackgroundPaint = backgroundPaint
        return this
    }

    /**
     * Set the background image for the DrawView. This image can be a File, Bitmap or ByteArray
     *
     * @param backgroundImage File that contains the background image
     * @param backgroundType  Background image type (File, Bitmap or ByteArray)
     * @param backgroundScale Background scale (Center crop, center inside, fit xy, fit top or fit bottom)
     * @return this instance of the view
     */
    fun setBackgroundImage(backgroundImage: Any,
                           backgroundType: BackgroundType,
                           backgroundScale: BackgroundScale): DrawView {
        if (backgroundImage !is File && backgroundImage !is Bitmap &&
                backgroundImage !is ByteArray) {
            throw RuntimeException("Background image must be File, Bitmap or ByteArray")
        }
        if (isForCamera) {
            Log.i(TAG, "You can't set a background image if your draw view is for camera")
            return this
        }
        if (onDrawViewListener != null) onDrawViewListener?.onStartDrawing()
        if (mDrawMoveHistoryIndex >= -1 &&
                mDrawMoveHistoryIndex < mDrawMoveHistory.size - 1) mDrawMoveHistory = mDrawMoveHistory.subList(0, mDrawMoveHistoryIndex + 1)
        val bitmap = BitmapUtils.GetBitmapForDrawView(this, backgroundImage, backgroundType, 50)
        var matrix = Matrix()
        when (backgroundScale) {
            BackgroundScale.CENTER_CROP -> matrix = MatrixUtils.GetCenterCropMatrix(RectF(0f, 0f,
                    bitmap.width.toFloat(),
                    bitmap.height.toFloat()),
                    RectF(0f, 0f, width.toFloat(), height.toFloat()))
            BackgroundScale.CENTER_INSIDE -> matrix.setRectToRect(RectF(0f, 0f,
                    bitmap.width.toFloat(),
                    bitmap.height.toFloat()),
                    RectF(0f, 0f, width.toFloat(), height.toFloat()), Matrix.ScaleToFit.CENTER)
            BackgroundScale.FIT_XY -> matrix.setRectToRect(RectF(0f, 0f,
                    bitmap.width.toFloat(),
                    bitmap.height.toFloat()),
                    RectF(0f, 0f, width.toFloat(), height.toFloat()), Matrix.ScaleToFit.FILL)
            BackgroundScale.FIT_START -> matrix.setRectToRect(RectF(0f, 0f,
                    bitmap.width.toFloat(),
                    bitmap.height.toFloat()),
                    RectF(0f, 0f, width.toFloat(), height.toFloat()), Matrix.ScaleToFit.START)
            BackgroundScale.FIT_END -> matrix.setRectToRect(RectF(0f, 0f,
                    bitmap.width.toFloat(),
                    bitmap.height.toFloat()),
                    RectF(0f, 0f, width.toFloat(), height.toFloat()), Matrix.ScaleToFit.END)
        }
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val bitmapArray = byteArrayOutputStream.toByteArray()
        bitmap.recycle()
        mDrawMoveHistory.add(DrawMove.newInstance()
                .setBackgroundImage(bitmapArray, matrix)
                .setPaint(SerializablePaint()))
        mDrawMoveHistoryIndex++
        mDrawMoveBackgroundIndex = mDrawMoveHistoryIndex
        if (onDrawViewListener != null) onDrawViewListener?.onEndDrawing()
        invalidate()
        return this
    }

    /**
     * Set the background image for the DrawView. This image can be a File, Bitmap or ByteArray
     *
     * @param backgroundImage  File that contains the background image
     * @param backgroundType   Background image type (File, Bitmap or ByteArray)
     * @param backgroundMatrix Background matrix for the image
     * @return this instance of the view
     */
    fun setBackgroundImage(backgroundImage: Any,
                           backgroundType: BackgroundType,
                           backgroundMatrix: Matrix): DrawView {
        if (backgroundImage !is File && backgroundImage !is Bitmap &&
                backgroundImage !is ByteArray) {
            throw RuntimeException("Background image must be File, Bitmap or ByteArray")
        }
        if (isForCamera) {
            Log.i(TAG, "You can't set a background image if your draw view is for camera")
            return this
        }
        if (onDrawViewListener != null) onDrawViewListener?.onStartDrawing()
        if (mDrawMoveHistoryIndex >= -1 &&
                mDrawMoveHistoryIndex < mDrawMoveHistory.size - 1) mDrawMoveHistory = mDrawMoveHistory.subList(0, mDrawMoveHistoryIndex + 1)
        val bitmap = BitmapUtils.GetBitmapForDrawView(this, backgroundImage, backgroundType, 50)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val bitmapArray = byteArrayOutputStream.toByteArray()
        bitmap.recycle()
        mDrawMoveHistory.add(DrawMove.newInstance()
                .setBackgroundImage(bitmapArray, backgroundMatrix)
                .setPaint(SerializablePaint()))
        mDrawMoveHistoryIndex++
        mDrawMoveBackgroundIndex = mDrawMoveHistoryIndex
        onDrawViewListener?.onEndDrawing()
        invalidate()
        return this
    }

    /**
     * Set the max zoom factor of the DrawView
     *
     * @param maxZoomFactor The max zoom factor target
     * @return this instance of the view
     */
    fun setMaxZoomFactor(maxZoomFactor: Float): DrawView {
        this.maxZoomFactor = maxZoomFactor
        return this
    }

    /**
     * Sets the ZoomRegionView scale factor
     *
     * @param zoomRegionScale ZoomRegionView scale factor (DrawView size / scale)
     * @return this instance of the view
     */
    fun setZoomRegionScale(zoomRegionScale: Float): DrawView {
        this.zoomRegionScale = zoomRegionScale
        return this
    }

    /**
     * Sets the ZoomRegionView minimum scale factor
     *
     * @param zoomRegionScaleMin ZoomRegionView scale factor minimum
     * @return this instance of the view
     */
    fun setZoomRegionScaleMin(zoomRegionScaleMin: Float): DrawView {
        this.zoomRegionScaleMin = zoomRegionScaleMin
        return this
    }

    /**
     * Sets the ZoomRegionView maximum scale factor
     *
     * @param zoomRegionScaleMax ZoomRegionView scale factor maximum
     * @return this instance of view
     */
    fun setZoomRegionScaleMax(zoomRegionScaleMax: Float): DrawView {
        this.zoomRegionScaleMax = zoomRegionScaleMax
        return this
    }

    // PRIVATE METHODS
    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        return super.onCreateDrawableState(extraSpace)
    }

    /**
     * Draw the background image on DrawViewCanvas
     *
     * @param drawMove the DrawMove that contains the background image
     * @param canvas   tha DrawView canvas
     */
    private fun drawBackgroundImage(drawMove: DrawMove?, canvas: Canvas) {
        canvas.drawBitmap(BitmapFactory.decodeByteArray(drawMove!!.backgroundImage, 0,
                drawMove.backgroundImage.size), drawMove.backgroundMatrix, null)
    }

    /**
     * Shows or hides ZoomRegionView
     *
     * @param visibility the ZoomRegionView visibility target
     */
    private fun showHideZoomRegionView(visibility: Int) {
        if (mZoomRegionCardView!!.animation == null) {
            var alphaAnimation: AlphaAnimation? = null
            if (visibility == INVISIBLE && mZoomRegionCardView!!.visibility == VISIBLE) alphaAnimation = AlphaAnimation(1f, 0f) else if (visibility == VISIBLE && mZoomRegionCardView!!.visibility == INVISIBLE) alphaAnimation = AlphaAnimation(0f, 1f)
            if (alphaAnimation != null) {
                alphaAnimation.duration = 300
                alphaAnimation.interpolator = AccelerateDecelerateInterpolator()
                alphaAnimation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {
                        if (visibility == VISIBLE) mZoomRegionCardView!!.visibility = VISIBLE
                    }

                    override fun onAnimationEnd(animation: Animation) {
                        if (visibility == INVISIBLE) mZoomRegionCardView!!.visibility = INVISIBLE
                        mZoomRegionCardView!!.animation = null
                    }

                    override fun onAnimationRepeat(animation: Animation) {}
                })
                mZoomRegionCardView!!.startAnimation(alphaAnimation)
            }
        }
    }

    // SCALE
    private inner class ScaleGestureListener : SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            if (isZoomEnabled) {
                mFromZoomRegion = false
                mZoomFactor *= detector.scaleFactor
                mZoomFactor = Math.max(1f, Math.min(mZoomFactor, maxZoomFactor))
                mZoomFactor = if (mZoomFactor > maxZoomFactor) maxZoomFactor else if (mZoomFactor < 1f) 1f else mZoomFactor
                mZoomCenterX = detector.focusX / mZoomFactor + mCanvasClipBounds!!.left
                mZoomCenterY = detector.focusY / mZoomFactor + mCanvasClipBounds!!.top
                if (mZoomFactor > 1f) showHideZoomRegionView(VISIBLE) else showHideZoomRegionView(INVISIBLE)
                invalidate()
            }
            return false
        }
    }

    private inner class GestureListener : SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            if (isZoomEnabled) {
                mFromZoomRegion = false
                var animationOption = -1
                if (mZoomFactor >= 1f && mZoomFactor < maxZoomFactor) animationOption = 0 else if (mZoomFactor <= maxZoomFactor && mZoomFactor > 1f) animationOption = 1
                if (animationOption != -1) {
                    var valueAnimator: ValueAnimator? = null
                    valueAnimator = if (animationOption == 0) ValueAnimator.ofFloat(mZoomFactor, maxZoomFactor) else {
                        val distance: Float = maxZoomFactor - mZoomFactor
                        ValueAnimator.ofFloat(mZoomFactor, distance)
                    }
                    valueAnimator.duration = 300
                    valueAnimator.interpolator = AccelerateDecelerateInterpolator()
                    valueAnimator.addUpdateListener(AnimatorUpdateListener { animation ->
                        mZoomFactor = animation.animatedValue as Float
                        //                            Log.i(TAG, "Current Zoom: " + mZoomFactor);
                        mZoomFactor = if (mZoomFactor < 1f) 1f else mZoomFactor
                        mZoomCenterX = e.x / mZoomFactor + mCanvasClipBounds!!.left
                        mZoomCenterY = e.y / mZoomFactor + mCanvasClipBounds!!.top
                        if (mZoomFactor > 1f) mZoomRegionCardView!!.visibility = VISIBLE else mZoomRegionCardView!!.visibility = INVISIBLE
                        invalidate()
                    })
                    valueAnimator.start()
                }
            }
            return true
        }
    }
    // LISTENER
    /**
     * Setting new OnDrawViewListener for this view
     *
     * @param onDrawViewListener
     */
    fun setOnDrawViewListener(onDrawViewListener: OnDrawViewListener?) {
        this.onDrawViewListener = onDrawViewListener
    }

    /**
     * Listener for registering drawing actions of the view
     */
    interface OnDrawViewListener {
        fun onStartDrawing()
        fun onEndDrawing()
        fun onClearDrawing()
        fun onRequestText()
        fun onAllMovesPainted()
    }
}
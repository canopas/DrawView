package com.byox.drawview.dictionaries;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import com.byox.drawview.enums.BackgroundScale;
import com.byox.drawview.enums.BackgroundType;
import com.byox.drawview.enums.DrawingMode;
import com.byox.drawview.enums.DrawingTool;
import com.byox.drawview.sticker.TextSticker;
import com.byox.drawview.utils.SerializableMatrix;
import com.byox.drawview.utils.SerializablePaint;
import com.byox.drawview.utils.SerializablePath;

import java.io.Serializable;

/**
 * Created by Ing. Oscar G. Medina Cruz on 07/11/2016.
 * <p>
 * Dictionary class that save move for draw in the view, this allow the user to make a history
 * of the user movements in the view and make a redo/undo.
 *
 * @author Ing. Oscar G. Medina Cruz
 */

public class DrawMove implements Serializable {

    private static DrawMove mSingleton;

    private SerializablePaint mPaint;
    private DrawingMode mDrawingMode = null;
    private DrawingTool mDrawingTool = null;
    private BackgroundType backgroundType = null;
    private BackgroundScale backgroundScale = null;
    //private List<SerializablePath> mDrawingPathList;
    private SerializablePath mDrawingPath;
    private float mStartX, mStartY, mEndX, mEndY;
    private transient TextSticker sticker;
    private String stickerText;
    private Boolean isTextDone = false;
    private SerializableMatrix mBackgroundMatrix;
    private String mBackgroundImagePath;
    public transient Bitmap cachedBackgroundImageBitmap;

    // METHODS
    private DrawMove() {
    }

    public static DrawMove newInstance() {
        mSingleton = new DrawMove();
        return mSingleton;
    }

    // GETTERS

    public SerializablePaint getPaint() {
        return mPaint;
    }

    public DrawingMode getDrawingMode() {
        return mDrawingMode;
    }

    public DrawingTool getDrawingTool() {
        return mDrawingTool;
    }

    public SerializablePath getDrawingPath() {
        return mDrawingPath;
    }

    public float getStartX() {
        return mStartX;
    }

    public float getStartY() {
        return mStartY;
    }

    public float getEndX() {
        return mEndX;
    }

    public float getEndY() {
        return mEndY;
    }

    public TextSticker getTextSticker() {
        return sticker;
    }

    public String getText() {
        return stickerText;
    }

    public Matrix getBackgroundMatrix() {
        return mBackgroundMatrix;
    }

    public String getBackgroundImage() {
        return mBackgroundImagePath;
    }

    public Boolean isTextDone() {
        return isTextDone;
    }

    // SETTERS
    public void setTextDone(Boolean textDone) {
        isTextDone = textDone;
    }

    public DrawMove setPaint(SerializablePaint paint) {
        if (mSingleton != null) {
            mSingleton.mPaint = paint;
            return mSingleton;
        } else throw new RuntimeException("Create new instance of DrawMove first!");
    }

    public DrawMove setDrawingMode(DrawingMode drawingMode) {
        if (mSingleton != null) {
            mSingleton.mDrawingMode = drawingMode;
            return mSingleton;
        } else throw new RuntimeException("Create new instance of DrawMove first!");
    }

    public DrawMove setDrawingTool(DrawingTool drawingTool) {
        if (mSingleton != null) {
            mSingleton.mDrawingTool = drawingTool;
            return mSingleton;
        } else throw new RuntimeException("Create new instance of DrawMove first!");
    }

    public DrawMove setDrawingPathList(SerializablePath drawingPath) {
        if (mSingleton != null) {
            mSingleton.mDrawingPath = drawingPath;
            return mSingleton;
        } else throw new RuntimeException("Create new instance of DrawMove first!");
    }

    public DrawMove setStartX(float startX) {
        if (mSingleton != null) {
            mSingleton.mStartX = startX;
            return mSingleton;
        } else throw new RuntimeException("Create new instance of DrawMove first!");
    }

    public DrawMove setStartY(float startY) {
        if (mSingleton != null) {
            mSingleton.mStartY = startY;
            return mSingleton;
        } else throw new RuntimeException("Create new instance of DrawMove first!");
    }

    public DrawMove setEndX(float endX) {
        if (mSingleton != null) {
            mSingleton.mEndX = endX;
            return mSingleton;
        } else throw new RuntimeException("Create new instance of DrawMove first!");
    }

    public DrawMove setEndY(float endY) {
        if (mSingleton != null) {
            mSingleton.mEndY = endY;
            return mSingleton;
        } else throw new RuntimeException("Create new instance of DrawMove first!");
    }

    public DrawMove setSticker(TextSticker sticker) {
        if (mSingleton != null) {
            mSingleton.sticker = sticker;
            return mSingleton;
        } else throw new RuntimeException("Create new instance of DrawMove first!");
    }


    public DrawMove setText(String text) {
        if (mSingleton != null) {
            mSingleton.stickerText = text;
            return mSingleton;
        } else throw new RuntimeException("Create new instance of DrawMove first!");
    }

    public DrawMove setBackgroundImage(String imagePath) {
        if (mSingleton != null) {
            mSingleton.mBackgroundImagePath = imagePath;
            return mSingleton;
        } else throw new RuntimeException("Create new instance of DrawMove first!");
    }

    public DrawMove setCachedBackgroundImageBitmap(Bitmap cachedBackgroundImageBitmap) {
        this.cachedBackgroundImageBitmap = cachedBackgroundImageBitmap;
        return mSingleton;
    }

    public BackgroundScale getBackgroundScale() {
        return backgroundScale;
    }

    public BackgroundType getBackgroundType() {
        return backgroundType;
    }

    public void setmBackgroundMatrix(SerializableMatrix mBackgroundMatrix) {
        this.mBackgroundMatrix = mBackgroundMatrix;
    }

    public DrawMove setBackgroundImage(String imagePath, BackgroundType backgroundType, BackgroundScale backgroundScale) {
        if (mSingleton != null) {
            mSingleton.mBackgroundImagePath = imagePath;
            mSingleton.backgroundType = backgroundType;
            mSingleton.backgroundScale = backgroundScale;
            return mSingleton;
        } else throw new RuntimeException("Create new instance of DrawMove first!");
    }
}
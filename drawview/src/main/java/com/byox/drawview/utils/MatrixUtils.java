package com.byox.drawview.utils;

import android.graphics.Matrix;
import android.graphics.RectF;

/**
 * Created by IngMedina on 29/04/2017.
 */

public class MatrixUtils {
    public static SerializableMatrix GetCenterCropMatrix(RectF srcSize, RectF destSize){
        SerializableMatrix matrix = new SerializableMatrix();
        float scale = Math.max(destSize.width() / srcSize.width(),
                destSize.height() / srcSize.height());
        matrix.setTranslate(-(destSize.width() / 2), - (destSize.height() / 2));
        matrix.setScale(scale, scale);

        return matrix;
    }
}

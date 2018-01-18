package com.linda.answerhelper.ocr;

import android.graphics.Bitmap;

import com.linda.answerhelper.model.ReasultModel;

/**
 * Created by zcz on 2018/1/17.
 */

public interface OcrIReader {
    void getOcr(Bitmap bitmap,final ServiceListener listener);
}

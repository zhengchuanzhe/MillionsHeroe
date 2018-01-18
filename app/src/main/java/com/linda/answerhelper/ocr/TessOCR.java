package com.linda.answerhelper.ocr;

import android.graphics.Bitmap;
import android.os.Environment;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;

/**
 * Created by zcz on 2018/1/17.
 */

public class TessOCR implements OcrIReader {

    private TessBaseAPI mBaseAPI;

    public TessOCR(String ABSOLUTE_PATH)
    {
        try {
            mBaseAPI = new TessBaseAPI();
            mBaseAPI.init(ABSOLUTE_PATH + File.separator, "chi_sim");
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getOcr(Bitmap bitmap, ServiceListener listener) {
        mBaseAPI.setImage(bitmap);
        String result = mBaseAPI.getUTF8Text();
    }
}

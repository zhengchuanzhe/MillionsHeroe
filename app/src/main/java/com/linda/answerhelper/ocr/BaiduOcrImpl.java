package com.linda.answerhelper.ocr;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.widget.Toast;


import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.sdk.model.GeneralBasicParams;
import com.baidu.ocr.sdk.model.GeneralResult;
import com.baidu.ocr.sdk.model.WordSimple;
import com.linda.answerhelper.model.ReasultModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by zcz on 2018/1/17.
 */

public class BaiduOcrImpl implements OcrIReader {

    private  boolean hasGotToken;
    public BaiduOcrImpl(Context myContext)
    {
        hasGotToken=false;
        initAccessTokenWithAkSk(myContext);
    }

    //转json
    private ReasultModel parseJSONWithJSONObject(String jsonData) {
        try {

            JSONObject jsob=new JSONObject(jsonData);
            String wordData=jsob.getString("words_result");
            System.out.println("获取wordData："+wordData);
            JSONArray jsonArray = new JSONArray(wordData);

            ReasultModel rm=new ReasultModel();
            if (jsonArray.length()<=0)
            {
                return null;
            }
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            String  questionStr = jsonObject.getString("words");
            rm.setQuestion(questionStr);
            String[] ans=new String[jsonArray.length()-1];
            for (int i = 1; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                ans[i-1] = jsonObject.getString("words");
            }
            rm.setAnswer(ans);
            return  rm;
        } catch (Exception e) {
            e.printStackTrace();
            return  null;
        }
    }

    /**
     * 初始化百度OCR
     * @param myContext
     */
    private void initAccessTokenWithAkSk(Context myContext) {
        OCR.getInstance().initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                String token = result.getAccessToken();
                hasGotToken = true;
                System.out.println("验证成功");
            }
            @Override
            public void onError(OCRError error) {
                error.printStackTrace();
                System.out.println("AK，SK方式获取token失败"+ error.getMessage());
            }
        },myContext, "GnxKrIQ1geRV99btKGdAF3Uc", "BnSMLOsrsl0OiS0ybI8MfkVkjez345rp");
    }

    @Override
    public void getOcr(Bitmap bitmap,final ServiceListener listener) {

        String SavePath = getSDCardPath() + "/AndyDemo/ScreenImage";
        String filepath="";
        //3.保存Bitmap
        try {
            File path = new File(SavePath);
            //文件
            filepath = SavePath + "/Screen_2.png";
            File file = new File(filepath);
            if (!path.exists()) {
                path.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }

            FileOutputStream fos = null;
            fos = new FileOutputStream(file);
            if (null != fos) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.flush();
                fos.close();
                //Toast.makeText(this, "截屏文件已保存至SDCard/AndyDemo/ScreenImage/下", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        recWebimage(filepath,listener);
    }

    /**
     * 文字识别
     * @param filePath
     * @param listener
     */
    public void recWebimage(String filePath,final ServiceListener listener) {
        GeneralBasicParams param = new GeneralBasicParams();
        param.setDetectDirection(true);
        param.setImageFile(new File(filePath));
        OCR.getInstance().recognizeWebimage(param, new OnResultListener<GeneralResult>() {
            @Override
            public void onResult(GeneralResult result) {
                StringBuilder sb = new StringBuilder();
                for (WordSimple wordSimple : result.getWordList()) {
                    WordSimple word = wordSimple;
                    sb.append(word.getWords());
                    sb.append("\n");
                }
               // alertText("成功", result.getJsonRes());
                parseJSONWithJSONObject(result.getJsonRes());
                listener.onResult( parseJSONWithJSONObject(result.getJsonRes()));
            }

            @Override
            public void onError(OCRError error) {
               // alertText("失败", error.getMessage());
                listener.onResult(null);
            }
        });
    }


    /**
     * 获取SDCard的目录路径功能
     * @return
     */
    private String getSDCardPath(){
        File sdcardDir = null;
        //判断SDCard是否存在
        boolean sdcardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if(sdcardExist){
            sdcardDir = Environment.getExternalStorageDirectory();
        }
        return sdcardDir.toString();
    }
}

package com.linda.answerhelper.service;

import com.linda.answerhelper.model.ReasultModel;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Created by zcz on 2018/1/17.
 */

public class httpSearch{



    public String searchResult(ReasultModel rm)
    {
        try {
            String[] ans=rm.getAnswer();

            FutureTask[] futureQA = new FutureTask[ans.length];
            FutureTask[] futureAnswers = new FutureTask[ans.length];

            Long[] ansCount=new Long[ans.length];
            Long[] ansAndQuestionCount=new Long[ans.length];
            FutureTask futureQuestion = new FutureTask<Long>(new httpTemp(rm.getQuestion()));
            new Thread(futureQuestion).start();
            for (int i=0;i<ans.length;i++)
            {
                futureQA[i]=new FutureTask<Long>(new httpTemp(rm.getQuestion()+ans[i]));
                new Thread(futureQA[i]).start();
                futureAnswers[i]=new FutureTask<Long>(new httpTemp(ans[i]));
                new Thread(futureAnswers[i]).start();
            }
            while (!futureQuestion.isDone()) {
            }
            Long questionCalu = (Long) futureQuestion.get();
            for (int i = 0; i < ans.length; i++) {
                while (true) {
                    if (futureAnswers[i].isDone() && futureQA[i].isDone()) {
                        break;
                    }
                }
                ansAndQuestionCount[i] = (Long) futureQA[i].get();
                ansCount[i] = (Long) futureAnswers[i].get();
            }

            double[] ansValue=new double[ans.length];
            double maxValue=0;
            int maxIdex=0;
            String result="";
            for (int i=0;i<ans.length;i++)
            {
                ansValue[i]=(double) ansAndQuestionCount[i]/(double) (questionCalu*ansCount[i]);
                if ( maxValue<ansValue[i])
                {
                    maxValue=ansValue[i];
                    maxIdex=i;
                }
                result=result+ans[i]+":"+ansValue[i]+"\r\n";
            }
            result="推荐选项："+(maxIdex+1)+","+ans[maxIdex]+"\r\n"+result;
            return  result;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return"计算错误";
    }



    public class  httpTemp  implements Callable {

        private  String path;
        public httpTemp(String qusetionStr)
        {
            try {
                path = "http://www.baidu.com/s?tn=ichuner&lm=-1&word=" +
                        URLEncoder.encode(qusetionStr, "gb2312") + "&rn=1";
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        @Override
        public Long call() throws Exception {
            String line = "";
            boolean findIt = false;
            while (!findIt) {
                HttpGet httpGet = new HttpGet(path);
                HttpClient httpclient = new DefaultHttpClient();

                HttpResponse response = null;
                InputStream is = null;
                try {
                    response = httpclient.execute(httpGet);
                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                try {
                    is = response.getEntity().getContent();
                } catch (IllegalStateException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                BufferedReader breaded = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                while ((line = breaded.readLine()) != null) {
                    if (line.contains("百度为您找到相关结果约")) {
                        findIt = true;
                        int start = line.indexOf("百度为您找到相关结果约") + 11;
                        line = line.substring(start);
                        int end = line.indexOf("个");
                        line = line.substring(0, end);
                        break;
                    }

                }

            }
            line = line.replace(",", "");
            return Long.valueOf(line);
        }

    }
}



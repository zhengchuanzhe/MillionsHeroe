package com.linda.answerhelper.model;


/**
 * Created by zcz on 2018/1/17.
 */

public class ReasultModel{

    public ReasultModel()
    {}

    public ReasultModel(String resultStr)
    {
        getInformation(resultStr);
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }


    private String question;

    public String[] getAnswer() {
        return answer;
    }

    public void setAnswer(String[] answer) {
        this.answer = answer;
    }

    private String[] answer;



    public void getInformation(String str) {
        //先去除空行
        str = str.replaceAll("((\r\n)|\n)[\\s\t ]*(\\1)+", "$1").
                replaceAll("^((\r\n)|\n)", "");
        str=str.replace('.',' ').replace(" ","");
        //问号统一替换为英文问号防止报错
        str=str.replace("？","?");
        int begin=(str.charAt(1)>='0'&& str.charAt(1)<='9')?2:1;
        question = str.trim().substring(begin, str.indexOf('?') + 1);
        question = question.replaceAll("((\r\n)|\n)", "");
        System.out.println(question);
        String remain = str.substring(str.indexOf("?") + 1);
        answer = remain.trim().split("\n");
    }
}

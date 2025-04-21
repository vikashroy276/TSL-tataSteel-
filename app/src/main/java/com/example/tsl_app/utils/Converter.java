package com.example.tsl_app.utils;

public class Converter {
    public static String hexaToString(String hexa){

        String result = "";

        char[] charArray = hexa.toCharArray();

        for(int i = 0; i < charArray.length;  i=i+2) {

            if(i+1< charArray.length && i<charArray.length){
                String st = ""+charArray[i]+""+charArray[i+1];
                char ch = (char)Integer.parseInt(st, 16);
                result = result + ch;

            }
        }
       return  result;
    }
}
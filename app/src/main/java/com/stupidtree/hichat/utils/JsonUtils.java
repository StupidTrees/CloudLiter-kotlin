package com.stupidtree.hichat.utils;

import com.google.gson.JsonObject;

/**
 * 此类整合了一些JSON格式数据解析有关的函数
 */
public class JsonUtils {
    public static Integer getIntegerData(JsonObject jo,String key){
        try {
            return jo.get(key).getAsInt();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static String getStringData(JsonObject jo,String key){
        try {
            return jo.get(key).getAsString();
        } catch (Exception ignored) {
            return null;
        }
    }

    public static JsonObject getObjectData(JsonObject jo,String key){
        try {
            return jo.get(key).getAsJsonObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

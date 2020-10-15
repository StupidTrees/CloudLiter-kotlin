package com.stupidtree.hichat.data.model;

import org.jetbrains.annotations.NotNull;

/**
 * 封装了服务器返回格式的类
 * @param <T> 返回数据的类型
 */
public class ApiResponse<T> {

    int code; //返回状态码
    String message; //返回message
    T data; //返回数据

    public ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @NotNull
    @Override
    public String toString() {
        return "ApiResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
package com.stupidtree.cloudliter.data.model

/**
 * 封装了服务器返回格式的类
 * @param <T> 返回数据的类型
</T> */
class ApiResponse<T>(//返回状态码
        var code: Int, //返回message
        var message: String, //返回数据
        var data: T?) {

    override fun toString(): String {
        return "ApiResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}'
    }

}
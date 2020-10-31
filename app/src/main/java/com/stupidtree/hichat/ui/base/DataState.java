package com.stupidtree.hichat.ui.base;

import androidx.annotation.NonNull;

import com.stupidtree.hichat.data.model.ChatMessage;

import java.util.List;

/**
 * 封装了某个UI中数据的状态
 * 例如好友列表界面，应当存放一个”列表数据“，直观上，其应当是一个好友对象的List
 * 但因为实际应用中，在UI中除了要显示这个List本身外，还应能够表示”获取成功“，”获取失败“，”用户未登录“ 等【状态信息】
 * 因此DataState这个类存在的目的，便是包装在数据外层，附加上【状态信息】
 * @param <T> 指定该数据类型
 */
public class DataState<T> {
    /**
     * 定义几种基本的状态
     * NOTHING 空状态，什么都不要做
     * SUCCESS 请求成功
     * FETCH_FAILED 请求失败
     * NOT_LOGGED_IN 用户未登录
     * TOKEN_INVALID token已失效
     * NOR_EXIST 查询数据不存在
     * SPECIAL 特殊状态
     */
    public enum STATE{NOTHING,NOT_LOGGED_IN,SUCCESS,FETCH_FAILED,TOKEN_INVALID,NOT_EXIST,SPECIAL}

    public enum LIST_ACTION{REPLACE_ALL,APPEND,PUSH_HEAD,DELETE,APPEND_ONE}
    //表征数据状态
    STATE state;
    //数据本体
    T data;
    //附带的message
    String message;
    //列表数据的动作
    LIST_ACTION listAction = LIST_ACTION.REPLACE_ALL;
    //是否为重试状态
    boolean retry;



    public DataState(@NonNull T data){
        this.data = data;
        this.state = STATE.SUCCESS;
    }

    public DataState(@NonNull T data,STATE state){
        this.data = data;
        this.state = state;
    }
    public DataState(STATE state){
        this.state = state;
    }

    public DataState(STATE state,String message){
        this.state = state;
        this.message = message;
    }
    public STATE getState() {
        return state;
    }

    public T getData() {
        return data;
    }

    public DataState<T> setRetry(boolean retry) {
        this.retry = retry;
        return this;
    }

    public boolean isRetry() {
        return retry;
    }

    public void setData(T data) {
        this.data = data;
    }

    public LIST_ACTION getListAction() {
        return listAction;
    }

    public DataState<T> setListAction(LIST_ACTION listAction) {
        this.listAction = listAction;
        return this;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "DataState{" +
                "state=" + state +
                ", data=" + data +
                ", message='" + message + '\'' +
                ", listAction=" + listAction +
                '}';
    }
}

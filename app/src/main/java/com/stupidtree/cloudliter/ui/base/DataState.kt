package com.stupidtree.cloudliter.ui.base

/**
 * 封装了某个UI中数据的状态
 * 例如好友列表界面，应当存放一个”列表数据“，直观上，其应当是一个好友对象的List
 * 但因为实际应用中，在UI中除了要显示这个List本身外，还应能够表示”获取成功“，”获取失败“，”用户未登录“ 等【状态信息】
 * 因此DataState这个类存在的目的，便是包装在数据外层，附加上【状态信息】
 * @param <T> 指定该数据类型
</T> */
class DataState<T> {
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
    enum class STATE {
        NOTHING, NOT_LOGGED_IN, SUCCESS, FETCH_FAILED, TOKEN_INVALID, NOT_EXIST, SPECIAL
    }

    enum class LIST_ACTION {
        REPLACE_ALL, APPEND, PUSH_HEAD, DELETE, APPEND_ONE
    }

    //表征数据状态
    var state: STATE

    //数据本体
    var data: T? = null

    //附带的message
    var message: String? = null

    //列表数据的动作
    var listAction = LIST_ACTION.REPLACE_ALL

    //是否为重试状态
    var isRetry = false

    constructor(data: T) {
        this.data = data
        state = STATE.SUCCESS
    }

    constructor(data: T, state: STATE) {
        this.data = data
        this.state = state
    }

    constructor(state: STATE) {
        this.state = state
    }

    constructor(state: STATE, message: String?) {
        this.state = state
        this.message = message
    }

    fun setRetry(retry: Boolean): DataState<T> {
        isRetry = retry
        return this
    }

    fun setListAction(listAction: LIST_ACTION): DataState<T> {
        this.listAction = listAction
        return this
    }


    override fun toString(): String {
        return "DataState{" +
                "state=" + state +
                ", data=" + data +
                ", message='" + message + '\'' +
                ", listAction=" + listAction +
                '}'
    }
}
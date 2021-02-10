package com.stupidtree.cloudliter.data.repository

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.stupidtree.cloudliter.data.AppDatabase
import com.stupidtree.cloudliter.data.model.ChatMessage
import com.stupidtree.cloudliter.data.source.dao.AiDao
import com.stupidtree.cloudliter.data.source.dao.ChatMessageDao
import com.stupidtree.cloudliter.data.source.websource.AiWebSource
import com.stupidtree.cloudliter.data.source.websource.ChatMessageWebSource
import com.stupidtree.cloudliter.data.source.websource.SocketWebSource
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.chat.MessageReadNotification
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import top.zibin.luban.Luban
import top.zibin.luban.OnCompressListener
import java.io.ByteArrayOutputStream
import java.io.File

class AiRepository(context: Context) {
    //数据源1：网络类型数据，消息记录的网络数据源
    var aiWebSource: AiWebSource = AiWebSource.instance!!

    //数据源2：和后台服务通信的Service
    private var socketWebSource: SocketWebSource = SocketWebSource()

    //数据源3：本地数据库的数据源
    private var aiDao: AiDao = AppDatabase.getDatabase(context).aiDao()

    // 动态数据对象：列表状态
//    private var listDataState = MediatorLiveData<DataState<List<Any>?>>()
//    private var localListState: LiveData<List<Any>?>? = null //本地获取数据
//    private var webListState: LiveData<DataState<List<Any>?>>? = null //网络获取数据
//    fun getListDataState(): MediatorLiveData<DataState<List<Any>?>> {
//        listDataState.addSource(socketWebSource.newMessageState) { message: ChatMessage? ->
//            if (message != null) {
//                listDataState.removeSource(localListState!!)
//                listDataState.value = DataState(listOf(message) as List?).setListAction(DataState.LIST_ACTION.APPEND_ONE)
//                saveMessageAsync(message)
//            }
//        }
//        listDataState.addSource(messageSentSate) { chatMessageDataState: DataState<ChatMessage> ->
//            listDataState.removeSource(localListState!!)
//            val sentMessage = chatMessageDataState.data
//            sentMessage?.let { saveMessageAsync(it) }
//        }
//        listDataState.addSource(messageReadState) { messageReadNotificationDataState: DataState<MessageReadNotification> ->
//            listDataState.removeSource(localListState!!)
//            markMessageReadAsync(messageReadNotificationDataState.data)
//        }
//        return listDataState
//    }

    /**
     * 发送图片
     *
     * @param token    令牌
     * @param filePath 文件
     * @return 返回结果
     */
    fun  ActionSendAiImage(context: Context, token: String, filePath: String, bitmap: Bitmap?): LiveData<DataState<String?>> {
        val result = MediatorLiveData<DataState<String?>>()
        //读取图片文件
        val f = File(filePath)
//        val tempMsg = ChatMessage(fromId, toId, filePath)
//        tempMsg.setType(ChatMessage.TYPE.IMG)
//        listDataState.value = DataState(listOf(tempMsg) as List?).setListAction(DataState.LIST_ACTION.APPEND_ONE)
        Luban.with(context)
                .setTargetDir(context.getExternalFilesDir("image")!!.absolutePath)
                .load(f)
                .setCompressListener(object : OnCompressListener {
                    //设置回调
                    override fun onStart() {
                        Log.e("luban", "开始压缩HHH")
                    }

                    override fun onSuccess(file: File) {
                        Log.e("luban", "压缩成功HHH")

                        // Bitmap转字节流转String
                        val baos = ByteArrayOutputStream()
                        bitmap?.compress(Bitmap.CompressFormat.PNG, 100, baos)
                        val img = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
                        val bbody = MultipartBody.Part.createFormData("upload", img)

                        val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
                        //构造一个图片格式的POST表单
                        val body = MultipartBody.Part.createFormData("upload", file.name, requestFile)

                        val sendResult = aiWebSource.sendAiImage(token, bbody)
//                        Log.e("body::", img)
                        result.addSource(sendResult) { value ->
                            result.setValue(value)
                        }
                        Log.d("senResult::", sendResult.toString())
                    }

                    override fun onError(e: Throwable) {
                        Log.e("luban", "压缩失败")
                    }
                }).launch() //启动压缩
        return result
    }

//    companion object {
//        //单例模式
//        @JvmStatic
//        @Volatile
//        private var instance: AiRepository? = null
//        fun getInstance(application: Application): AiRepository {
//            if (instance == null) {
//                instance = AiRepository(application)
//            }
//            return instance!!
//        }
//
//    }

    companion object {
        //不采用单例模式
        @JvmStatic
        fun getInstance(context: Context): AiRepository {
            return AiRepository(context.applicationContext)
        }
    }
}
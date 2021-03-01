package com.stupidtree.cloudliter.ui.chat

import android.app.Activity
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.format.DateFormat
import com.stupidtree.cloudliter.utils.PermissionUtils
import java.io.File
import java.util.*

class AudioRecordHelper(val context: Activity, val onRecordListener: OnRecordListener) {
    enum class STATE { RECORDING, IDLE, DONE }

    // 录音功能相关
    var recorder // MediaRecorder 实例
            : MediaRecorder? = null
    var state: STATE = STATE.IDLE//录音状态
    var fileName // 录音文件的名称
            : String? = null
    var filePath // 录音文件存储路径
            : String = ""
    var timeThread // 记录录音时长的线程
            : Thread? = null
    var timeCount // 录音时长 计数
            = 0
    val TIME_COUNT = 0x101
    val RECORD_START = 3
    val RECORD_STOP = 4

    // 录音文件存放目录
    private val audioSaveDir = context.getExternalFilesDir(null)!!.absolutePath + "/audio/"


    private val mainThreadHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                RECORD_START -> {
                    if (msg.obj is java.lang.Exception) {
                        onRecordListener.onRecordStart(msg.obj as java.lang.Exception)
                    } else {
                        onRecordListener.onRecordStart(null)
                    }
                }
                RECORD_STOP -> {
                    if (msg.obj is java.lang.Exception) {
                        onRecordListener.onRecordStop(null, 0, msg.obj as java.lang.Exception?)
                    } else {
                        onRecordListener.onRecordStop(msg.obj as String?, msg.arg1, null)
                    }
                }
                TIME_COUNT -> {
                    val count = msg.obj as Int
                    onRecordListener.onRecordTimeTick(count, null)
                    if (count > 10) {
                        stopRecord()
                    }
                }
            }
        }
    }

    interface OnRecordListener {
        fun onRecordStart(exception: java.lang.Exception?)
        fun onRecordStop(path: String?, seconds: Int, exception: java.lang.Exception?)
        fun onRecordTimeTick(count: Int, exception: java.lang.Exception?)
    }

    /**
     * 开始录音 使用amr格式
     * 录音文件
     * @return
     */
    fun startRecord() {
        PermissionUtils.grantAudioPermissions(context)
        if (state == STATE.DONE) {//有未发送的语音
            cancelRecord()
        } else if (state == STATE.RECORDING) {
            return
        }
        destroy()
        state = STATE.RECORDING
        // 开始录音
        /* ①Initial：实例化MediaRecorder对象 */
        Thread {
            try {
                recorder = MediaRecorder()
                /* ②setAudioSource/setVedioSource */
                recorder?.setAudioSource(MediaRecorder.AudioSource.MIC) // 设置麦克风
                /*
                 * ②设置输出文件的格式：THREE_GPP/MPEG-4/RAW_AMR/Default THREE_GPP(3gp格式
                 * ，H263视频/ARM音频编码)、MPEG-4、RAW_AMR(只支持音频且音频编码要求为AMR_NB)
                 */
                recorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                /* ②设置音频文件的编码：AAC/AMR_NB/AMR_MB/Default 声音的（波形）的采样 */
                recorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

                recorder?.setAudioChannels(1)
                recorder?.setAudioEncodingBitRate(128000)
                recorder?.setAudioSamplingRate(16000)
                fileName = DateFormat.format("yyyyMMdd_HHmmss", Calendar.getInstance(Locale.CHINA)).toString() + ".m4a"
                val f = File(audioSaveDir)
                if (!f.exists()) {
                    f.mkdirs()
                }
                filePath = audioSaveDir + fileName
                /* ③准备 */recorder?.setOutputFile(filePath)
                recorder?.prepare()
                /* ④开始 */recorder?.start()
                // 初始化录音时长记录
                if (timeThread != null) {
                    timeThread!!.interrupt()
                }
                timeThread = Thread { countTime() }
                timeThread!!.start()
                val msg = Message.obtain()
                msg.what = RECORD_START
                mainThreadHandler.sendMessage(msg)
            } catch (e: Exception) {
                e.printStackTrace()
                val msg = Message.obtain()
                msg.what = RECORD_START
                msg.obj = e
                state = STATE.IDLE
                mainThreadHandler.sendMessage(msg)
            }
        }.start()
    }


    /**
     * 停止录音
     */
    fun stopRecord() {
        if (state != STATE.RECORDING) {
            return
        }
        recorder?.setOnErrorListener(null)
        recorder?.setOnInfoListener(null)
        recorder?.setPreviewDisplay(null)
        Thread {
            try {
                recorder?.stop()
                recorder?.release()
                val mes = Message.obtain()
                mes.what = RECORD_STOP
                mes.obj = filePath
                mes.arg1 = timeCount
                mainThreadHandler.sendMessage(mes)
                state = STATE.DONE
            } catch (e: RuntimeException) {
                // e.printStackTrace()
                val file = File(filePath)
                if (file.exists()) file.delete()
                filePath = ""
                try {
                    recorder?.reset()
                    recorder?.release()
                } catch (e: Exception) {

                }
                val mes = Message.obtain()
                mes.what = RECORD_STOP
                mes.obj = e
                mes.arg1 = timeCount
                timeCount = 0
                state = STATE.IDLE
                mainThreadHandler.sendMessage(mes)
            }
            recorder = null
        }.start()
    }

    /**
     * 取消录音：删除录音文件
     */
    fun cancelRecord() {
        state = STATE.IDLE
        filePath = ""
        timeCount = 0
        Thread {
            val file = File(filePath)
            if (file.exists()) file.delete()
        }.start()
    }

    /**
     * 发送语音消息：转换状态
     */
    fun sendRecord() {
        state = STATE.IDLE
        filePath = ""
        timeCount = 0
    }

    private fun countTime() {
        while (state == STATE.RECORDING) {
            if (Thread.interrupted()) return
            timeCount++
            val msg = Message.obtain()
            msg.what = TIME_COUNT
            msg.obj = timeCount
            mainThreadHandler.sendMessage(msg)
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                return
            }
        }
        val msg = Message.obtain()
        msg.what = TIME_COUNT
        msg.obj = timeCount
        mainThreadHandler.sendMessage(msg)
    }

    fun destroy() {
        cancelRecord()
        timeThread?.interrupt()
        if (recorder != null) {
            recorder!!.reset()
            recorder!!.release()
            recorder = null
        }
    }

    companion object {
        fun getInstance(context: Activity, onRecordListener: OnRecordListener): AudioRecordHelper {
            return AudioRecordHelper(context = context, onRecordListener)
        }
    }


}
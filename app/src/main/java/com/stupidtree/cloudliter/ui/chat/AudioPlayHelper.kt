package com.stupidtree.cloudliter.ui.chat

import android.app.Activity
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.stupidtree.cloudliter.utils.PermissionUtils

class AudioPlayHelper internal constructor(val context: Activity, val voicePlayListener: VoicePlayListener) {
    var mMediaPlayer: MediaPlayer? = null
    var playingId:String? = null
    val PLAY_START = 3


    interface VoicePlayListener{
        fun onStartPlaying(playingId:String)
        fun onToggle(playingId: String,toStart: Boolean)
        fun onPlayingFinished(playingId: String)
    }

    val mainThreadHandler:Handler =  object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                PLAY_START-> playingId?.let { voicePlayListener.onStartPlaying(it) }
            }
        }
    }

    fun play(messageId:String,url: String) {
        PermissionUtils.grantExternalStoragePermissions(context)
        Thread {
            destroy()
            mMediaPlayer = MediaPlayer.create(context, Uri.parse("http://hita.store:3000/message/voice?path=$url"))
            if(mMediaPlayer!=null){
                mMediaPlayer?.start()
                playingId = messageId
                val msg = Message.obtain()
                msg.what = PLAY_START
                mainThreadHandler.sendMessage(msg)
                mMediaPlayer!!.setOnCompletionListener {
                    voicePlayListener.onPlayingFinished(playingId!!)
                    playingId = null
                }
            }

        }.start()
    }



    fun toggle(){
        if(playingId!=null){
            mMediaPlayer?.let {
                val playing = it.isPlaying
                if(playing){
                    it.pause()
                }else{
                    it.start()
                }
                voicePlayListener.onToggle(playingId!!,!playing)
            }
        }

    }

    fun destroy(){
        playingId = null
        if(mMediaPlayer!=null){
            mMediaPlayer!!.stop()
            mMediaPlayer!!.reset()
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
    }
}
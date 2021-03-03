package com.stupidtree.cloudliter.ui.chat

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.ChatMessage
import com.stupidtree.cloudliter.ui.base.BaseListAdapter
import com.stupidtree.cloudliter.ui.base.BaseListAdapterClassic
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.widgets.EmoticonsTextView
import com.stupidtree.cloudliter.utils.ActivityUtils
import com.stupidtree.cloudliter.utils.ImageUtils
import com.stupidtree.cloudliter.utils.TextUtils
import java.sql.Timestamp
import java.util.*
import kotlin.math.abs

/**
 * 聊天列表的适配器
 */
@SuppressLint("ParcelCreator")
internal class ChatListAdapter(var chatActivity: ChatActivity, mBeans: MutableList<ChatMessage>) : BaseListAdapterClassic<ChatMessage, ChatListAdapter.CHolder>(chatActivity, mBeans) {
    var onTTSButtonClickListener: OnTTSButtonClickListener? = null
    override fun getLayoutId(viewType: Int): Int {
        return when (viewType) {
            TYPE_MINE -> R.layout.activity_chat_message_text_mine
            TYPE_FRIEND -> R.layout.activity_chat_message_text_friend
            TYPE_FRIEND_IMAGE -> R.layout.activity_chat_message_image_friend
            TYPE_FRIEND_VOICE -> R.layout.activity_chat_message_voice_friend
            TYPE_MINE_VOICE -> R.layout.activity_chat_message_voice_mine
            TYPE_MINE_IMAGE -> R.layout.activity_chat_message_image_mine
            else -> R.layout.activity_chat_message_time
        }
    }

    override fun getItemViewType(position: Int): Int {
        val cm = mBeans[position]
        return when {
            cm.isTimeStamp -> {
                TYPE_TIME
            }
            cm.toId == chatActivity.viewModel.myId -> {
                when {
                    cm.getTypeEnum() == ChatMessage.TYPE.IMG -> TYPE_FRIEND_IMAGE
                    cm.getTypeEnum() == ChatMessage.TYPE.TXT -> TYPE_FRIEND
                    else -> TYPE_FRIEND_VOICE
                }
            }
            else -> {
                when {
                    cm.getTypeEnum() == ChatMessage.TYPE.IMG -> TYPE_MINE_IMAGE
                    cm.getTypeEnum() == ChatMessage.TYPE.VOICE -> TYPE_MINE_VOICE
                    else -> TYPE_MINE
                }
            }
        }

    }

    override fun createViewHolder(v: View, viewType: Int): CHolder {
        return CHolder(v, viewType)
    }

    override fun bindHolder(holder: CHolder, data: ChatMessage?, position: Int) {
        if (data != null) {
            if (holder.read != null) {
                holder.read?.visibility = if (data.read && !data.sensitive) View.VISIBLE else View.GONE
            }
            if (holder.viewType == TYPE_TIME) {
                bindTimestamp(holder, data)
            } else {
                holder.bindSensitiveAndEmotion(data)
                if (holder.viewType == TYPE_MINE || holder.viewType == TYPE_MINE_IMAGE || holder.viewType == TYPE_MINE_VOICE) {
                    chatActivity.viewModel.myAvatar?.let { ImageUtils.loadLocalAvatarInto(chatActivity, it, holder.avatar!!) }
                } else {
                    chatActivity.viewModel.friendAvatar?.let { ImageUtils.loadAvatarInto(chatActivity, it, holder.avatar!!) }
                }
                holder.avatar?.setOnClickListener { data.fromId?.let { ActivityUtils.startProfileActivity(chatActivity, it) } }
                holder.setSendState(data)
                //图片消息
                if (data.getTypeEnum() == ChatMessage.TYPE.IMG) {
                    if (holder.progress != null) {
                        if (holder.progress?.visibility != View.VISIBLE) {
                            data.fileId?.let { ImageUtils.loadChatMessageInto(chatActivity, it, holder.image!!) }
                        } else {
                            holder.image?.setImageResource(R.drawable.place_holder_loading)
                        }
                    } else {
                        data.fileId?.let { ImageUtils.loadChatMessageInto(chatActivity, it, holder.image!!) }
                    }
                } else if (data.getTypeEnum() == ChatMessage.TYPE.VOICE) {
                    holder.bindVoiceState(data)
                }
            }
            holder.bindClickAction(data, position)
        }
    }


    private fun bindTimestamp(holder: CHolder, data: ChatMessage) {
        holder.content?.let {
            it.text = data.createdAt?.let { it1 ->
                TextUtils.getChatTimeText(chatActivity, it1)
            }
        }
    }

    /**
     * 获取列表中所有图片的url
     *
     * @return 结果
     */
    val imageUrls: List<String>
        get() {
            val res: MutableList<String> = ArrayList()
            for (cm in mBeans) {
                if (!cm.isTimeStamp && cm.getTypeEnum() == ChatMessage.TYPE.IMG) {
                    cm.fileId?.let {
                        res.add(ImageUtils.getChatMessageImageUrl(it))
                    }
                }
            }
            return res
        }

    /**
     * 判断两时间戳相隔是否太远
     */
    private fun tooFar(t1: Timestamp?, t2: Timestamp?): Boolean {
        if (t1 != null && t2 != null) {
            return abs(t1.time - t2.time) > 10.toLong() * 60 * 1000
        } //取10分钟
        return false
    }

    /**
     * 当消息被对方阅读后，更新界面
     *
     * @param list         列表
     * @param notification
     */
    fun messageRead(list: RecyclerView, notification: MessageReadNotification) {
        val indexes: MutableList<Int> = ArrayList()
        for (i in mBeans.indices.reversed()) {
            if (notification.type == MessageReadNotification.TYPE.ALL) {
                mBeans[i].createdAt?.let {
                    if (it.after(notification.fromTime)) {
                        indexes.add(i)
                    }
                }
            } else if (mBeans[i].id == notification.id) {
                indexes.add(i)
                break
            }
        }
        for (index in indexes) {
            mBeans[index].read = true
            val holder = list.findViewHolderForAdapterPosition(index) as CHolder?
            if (!mBeans[index].sensitive) holder?.showRead()
        }
    }

    /**
     * 当消息发送成功后，通知该消息更新界面
     *
     * @param list        recyclerview
     * @param sentMessage 已发送消息实体
     */
    fun messageSent(list: RecyclerView, sentMessage: ChatMessage?, uuid: String, state: DataState.STATE) {
        var index = -1
        for (i in mBeans.indices.reversed()) {
            if (mBeans[i].uuid == uuid) {
                index = i
                break
            }
        }
        if (index >= 0) {
            val sendState = if (state == DataState.STATE.SUCCESS) ChatMessage.SEND_STATE.SUCCESS else ChatMessage.SEND_STATE.FAILED
            sentMessage?.sendingState = sendState
            mBeans[index].sendingState = sendState
            if (sentMessage != null) {
                mBeans[index] = sentMessage
            }
            val holder = list.findViewHolderForAdapterPosition(index) as CHolder?
            if (holder != null) {
                holder.setSendState(mBeans[index])
                sentMessage?.let {
                    holder.bindSensitiveAndEmotion(it)
                    holder.bindClickAction(it, index)
                    if (it.getTypeEnum() == ChatMessage.TYPE.IMG) {
                        holder.updateImage(it)
                    }
                }
            } else {
                list.postDelayed({
                    val h = list.findViewHolderForAdapterPosition(index) as CHolder?
                    h?.setSendState(mBeans[index])
                    sentMessage?.let {
                        h?.bindSensitiveAndEmotion(it)
                        h?.bindClickAction(it, index)
                        if (it.getTypeEnum() == ChatMessage.TYPE.IMG) {
                            h?.updateImage(it)
                        }
                    }
                }, 100)
            }
        }
    }


    /**
     * 语音开始播放
     */
    fun changeAudioState(list: RecyclerView, id: String, action: ChatMessage.VOICE_STATE) {
        var index = -1
        for (i in mBeans.indices.reversed()) {
            if (mBeans[i].id == id) {
                index = i
                break
            }
        }
        if (index >= 0) {
            mBeans[index].playing = action
            val holder = list.findViewHolderForAdapterPosition(index) as CHolder?
            holder?.bindVoiceState(mBeans[index])
        }
    }


    /**
     * 语音识别状态变更
     */
    fun changeTTSState(list: RecyclerView, id: String, chatMessage: ChatMessage?, action: ChatMessage.TTS_STATE) {
        var index = -1
        for (i in mBeans.indices.reversed()) {
            if (mBeans[i].id == id) {
                index = i
                break
            }
        }
        if (index >= 0) {
            chatMessage?.let {
                mBeans[index].ttsResult = it.ttsResult
                mBeans[index].extra = it.extra
                mBeans[index].emotion = it.emotion
                mBeans[index].sensitive = it.sensitive
            }
            mBeans[index].ttsState = action
            val holder = list.findViewHolderForAdapterPosition(index) as CHolder?
            holder?.bindVoiceState(mBeans[index])
        }
    }

    /**
     * 清空列表
     */
    fun clear() {
        mBeans.clear()
        notifyDataSetChanged()
    }

    override fun notifyItemsAppended(list: List<ChatMessage>) {
        val newL = mutableListOf<ChatMessage>()
        newL.addAll(list)
        //注意要取反
        newL.reverse()
        if (mBeans.size > 0 && newL.isNotEmpty()) {
            val last = mBeans[mBeans.size - 1]
            if (tooFar(last.createdAt, newL[0].createdAt)) {
                super.notifyItemAppended(ChatMessage.getTimeStampHolderInstance(newL[0].createdAt))
            }
        } else if (mBeans.size == 0 && newL.isNotEmpty()) {
            newL.add(0, ChatMessage.getTimeStampHolderInstance(newL[0].createdAt))
        }
        super.notifyItemsAppended(newL)
    }

    /**
     * 头部数据的更新，存在则更新，其余的插入到头部
     * @param chatMessages 新的头部消息
     */
    fun notifyHeadItemsUpdated(chatMessages: List<ChatMessage>) {
        val ids = HashMap<String, ChatMessage?>()
        for (cm in chatMessages) {
            ids[cm.id] = cm
        }
        val toDelete: MutableList<ChatMessage?> = ArrayList()
        for (i in mBeans.indices.reversed()) {
            val cm = mBeans[i]
            if (ids.containsKey(cm.id)) {
                if (ids[cm.id] != cm) {
                    notifyItemChanged(i)
                }
                toDelete.add(ids[cm.id])
            }
        }
        if (chatMessages is MutableList) {
            chatMessages.removeAll(toDelete)
        }
        //其余的加到头部
        notifyItemsPushHead(chatMessages)
    }

    override fun notifyItemsPushHead(newL: List<ChatMessage>) {
        if (newL is MutableList) {
            newL.reverse() //取反
        }
        if (mBeans.size > 0 && newL.isNotEmpty()) {
            val top = mBeans[0]
            val newBottom = newL[newL.size - 1]
            if (tooFar(top.createdAt, newBottom.createdAt)) {
                if (!top.isTimeStamp) {
                    super.notifyItemPushHead(ChatMessage.getTimeStampHolderInstance(top.createdAt))
                }
            } else if (top.isTimeStamp) {
                super.notifyItemRemoveFromHead()
            }
        }
        if (newL is MutableList && newL.isNotEmpty()) {
            newL.add(0, ChatMessage.getTimeStampHolderInstance(newL[0].createdAt))
        }
        super.notifyItemsPushHead(newL)
    }

    /**
     * 为了在时间跨度太大的两项间插入时间戳显示
     *
     * @param newL             新的数据List
     */
    override fun notifyItemChangedSmooth(newL: List<ChatMessage>) {
        val toAdd: MutableList<ChatMessage> = LinkedList()
        if (newL.size == 1) {
            toAdd.addAll(newL)
        }
        for (i in 1 until newL.size) {
            val last = newL[i - 1]
            val thi = newL[i]
            toAdd.add(0, last)
            if (tooFar(last.createdAt, thi.createdAt)) {
                toAdd.add(0, ChatMessage.getTimeStampHolderInstance(thi.createdAt))
            }
            if (i == newL.size - 1) {
                toAdd.add(0, thi)
            }
        }
        if (toAdd.size > 0) {
            toAdd.add(0, ChatMessage.getTimeStampHolderInstance(toAdd[0].createdAt))
        }
        super.notifyItemChangedSmooth(toAdd, object : BaseListAdapter.RefreshJudge<ChatMessage> {
            override fun judge(oldData: ChatMessage, newData: ChatMessage): Boolean {
                return oldData != newData
            }
        })
    }

    internal inner class CHolder(itemView: View, var viewType: Int) : RecyclerView.ViewHolder(itemView) {
        private val ttsButtonAnimation: ValueAnimator = ValueAnimator.ofFloat(0f, 360f)

        init {
            ttsButtonAnimation.repeatCount = -1
            ttsButtonAnimation.addUpdateListener {
                ttsButton?.rotation = it.animatedValue as Float
            }
            ttsButtonAnimation.duration = 500
            ttsButtonAnimation.interpolator = LinearInterpolator()
        }

        var content: EmoticonsTextView? = itemView.findViewById(R.id.content)
        var avatar: ImageView? = itemView.findViewById(R.id.avatar)
        var bubble: View? = itemView.findViewById(R.id.bubble)
        var progress: View? = itemView.findViewById(R.id.progress)
        var fail: View? = itemView.findViewById(R.id.fail)

        var see //点击查看敏感消息
                : ImageView? = itemView.findViewById(R.id.see)

        var emotion: ImageView? = itemView.findViewById(R.id.emotion)

        var image //图片
                : ImageView? = itemView.findViewById(R.id.image)
        var ttsButton: ImageView? = itemView.findViewById(R.id.tts)//语音识别按钮
        var ttsResult: TextView? = itemView.findViewById(R.id.tts_result)//语音识别结果

        var read: View? = itemView.findViewById(R.id.read)


        var imageSensitivePlaceHolder: ViewGroup? = itemView.findViewById(R.id.image_sensitive)
        var sensitiveExpanded = false


        //设置发送状态
        fun setSendState(data: ChatMessage) {
            when (data.sendingState) {
                ChatMessage.SEND_STATE.SENDING -> {
                    fail?.visibility = View.GONE
                    progress?.visibility = View.VISIBLE
                }
                ChatMessage.SEND_STATE.SUCCESS -> {
                    fail?.visibility = View.GONE
                    progress?.visibility = View.GONE
                }
                ChatMessage.SEND_STATE.FAILED -> {
                    fail?.visibility = View.VISIBLE
                    progress?.visibility = View.GONE
                }
            }

        }

        fun showRead() {
            if (read != null) {
                read?.visibility = View.VISIBLE
            }
        }

        //切换敏感消息查看模式
        private fun switchSensitiveModeText(data: ChatMessage) {
            sensitiveExpanded = !sensitiveExpanded
            if (see == null) return
            if (sensitiveExpanded) {
                content?.text = data.content
                see?.setImageResource(R.drawable.ic_baseline_visibility_off_24)
                if (data.getTypeEnum() == ChatMessage.TYPE.IMG && image != null && imageSensitivePlaceHolder != null) {
                    image?.visibility = View.VISIBLE
                    imageSensitivePlaceHolder?.visibility = View.GONE
                }
            } else if (data.sensitive) {
                see?.setImageResource(R.drawable.ic_baseline_visibility_24)
                content?.setText(R.string.hint_sensitive_message)
                if (data.getTypeEnum() == ChatMessage.TYPE.IMG && image != null && imageSensitivePlaceHolder != null) {
                    image?.visibility = View.INVISIBLE
                    imageSensitivePlaceHolder?.visibility = View.VISIBLE
                }
            }
        }

        //绑定敏感词状态
        fun bindSensitiveAndEmotion(data: ChatMessage) {
            sensitiveExpanded = false
            if (data.getTypeEnum() == ChatMessage.TYPE.IMG) {
                if (data.sensitive) {
                    see?.visibility = View.VISIBLE
                    read?.visibility = View.GONE
                    see?.setOnClickListener { switchSensitiveModeText(data) }
                    image?.visibility = View.INVISIBLE
                    imageSensitivePlaceHolder?.visibility = View.VISIBLE
                    content?.setText(R.string.hint_sensitive_message)
                } else {
                    imageSensitivePlaceHolder?.visibility = View.GONE
                    image?.visibility = View.VISIBLE
                    see?.visibility = View.GONE
                }
            } else if (data.getTypeEnum() == ChatMessage.TYPE.TXT) {
                if (data.sensitive) {
                    read?.let { it.visibility = View.GONE }
                    see?.visibility = View.VISIBLE
                    emotion?.visibility = View.GONE
                    see?.setImageResource(R.drawable.ic_baseline_visibility_24)
                    see?.setOnClickListener { switchSensitiveModeText(data) }
                    content?.setText(R.string.hint_sensitive_message)
                } else {
                    see?.visibility = View.GONE
                    content?.text = data.content
                    emotion?.visibility = View.VISIBLE
                    setEmotionIcon(data.emotion)
                }
            } else {
                data.extra?.let {
                    content?.text = TextUtils.getVoiceTimeText(mContext, Integer.parseInt(it.replace("\"", "")))
                }
            }
        }

        private fun setEmotionIcon(emotionValue: Float) {
            var iconRes = R.drawable.ic_emotion_normal
            when {
                emotionValue >= 0.7 -> {
                    iconRes = R.drawable.ic_emotion_pos_3
                }
                emotionValue >= 0.4 -> {
                    iconRes = R.drawable.ic_emotion_pos_2
                }
                emotionValue > 0 -> {
                    iconRes = R.drawable.ic_emotion_pos_1
                }
                emotionValue <= -0.7 -> {
                    iconRes = R.drawable.ic_emotion_neg_3
                }
                emotionValue <= -0.4 -> {
                    iconRes = R.drawable.ic_emotion_neg_2
                }
                emotionValue < 0 -> {
                    iconRes = R.drawable.ic_emotion_neg_1
                }
            }
            emotion?.setImageResource(iconRes)
        }

        fun bindVoiceState(data: ChatMessage) {
            if (image != null) {
                when (data.playing) {
                    ChatMessage.VOICE_STATE.STOPPED -> {
                        image?.setImageResource(R.drawable.ic_voice_wave)
                    }
                    ChatMessage.VOICE_STATE.PAUSED -> {
                        image?.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
                    }
                    else -> {
                        image?.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24)
                    }
                }
            }
            if (!data.ttsResult.isNullOrEmpty()) {
                emotion?.visibility = View.VISIBLE
                setEmotionIcon(data.emotion)
                data.ttsState = ChatMessage.TTS_STATE.SUCCESS
            } else {
                emotion?.visibility = View.INVISIBLE
            }
            if (ttsButton != null) {
                when (data.ttsState) {
                    ChatMessage.TTS_STATE.STOPPED -> {
                        ttsButton?.visibility = View.VISIBLE
                        ttsResult?.visibility = View.GONE
                        ttsButtonAnimation.cancel()
                    }
                    ChatMessage.TTS_STATE.SUCCESS -> {
                        ttsButton?.visibility = View.GONE
                        ttsResult?.visibility = View.VISIBLE
                        ttsResult?.text = data.ttsResult
                        ttsButtonAnimation.cancel()
                    }
                    ChatMessage.TTS_STATE.FAILED -> {
                        ttsButton?.visibility = View.VISIBLE
                        ttsResult?.visibility = View.VISIBLE
                        ttsResult?.text = "转文字失败"
                        ttsButtonAnimation.cancel()
                    }
                    ChatMessage.TTS_STATE.PROCESSING -> {
                        ttsButton?.visibility = View.VISIBLE
                        ttsButtonAnimation.start()
                        ttsResult?.visibility = View.VISIBLE
                        ttsResult?.text = "转换中..."
                    }
                }
            }

        }

        //绑定点击事件
        fun bindClickAction(data: ChatMessage, position: Int) {
            if (mOnItemLongClickListener != null && bubble != null) {
                bubble?.setOnLongClickListener { view: View? -> mOnItemLongClickListener?.onItemLongClick(data, view, position)!! }
            }
            if (mOnItemClickListener != null && bubble != null) {
                bubble?.setOnClickListener { view: View? -> mOnItemClickListener?.onItemClick(data, view, position) }
            }
            ttsButton?.setOnClickListener {
                onTTSButtonClickListener?.onClick(it, data, position)
            }
        }

        fun updateImage(data: ChatMessage) {
            if (image != null) {
                data.fileId?.let { ImageUtils.loadChatMessageInto(chatActivity, it, image!!) }
            }
        }

    }

    interface OnTTSButtonClickListener {
        fun onClick(v: View, data: ChatMessage, position: Int)
    }

    companion object {
        private const val TYPE_MINE = 287
        private const val TYPE_FRIEND = 509
        private const val TYPE_MINE_IMAGE = 944
        private const val TYPE_FRIEND_IMAGE = 598
        private const val TYPE_MINE_VOICE = 333
        private const val TYPE_FRIEND_VOICE = 444
        private const val TYPE_TIME = 774
    }


}
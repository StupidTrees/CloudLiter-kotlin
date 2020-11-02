package com.stupidtree.cloudliter.ui.chat

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.ChatMessage
import com.stupidtree.cloudliter.ui.base.BaseListAdapter
import com.stupidtree.cloudliter.ui.base.BaseViewHolder
import com.stupidtree.cloudliter.ui.chat.MessageReadNotification
import com.stupidtree.cloudliter.ui.widgets.EmoticonsTextView
import com.stupidtree.cloudliter.utils.ActivityUtils
import com.stupidtree.cloudliter.utils.ImageUtils
import com.stupidtree.cloudliter.utils.TextUtils
import java.sql.Timestamp
import java.util.*

/**
 * 聊天列表的适配器
 */
@SuppressLint("NonConstantResourceId")
internal class ChatListAdapter(var chatActivity: ChatActivity, mBeans: MutableList<ChatMessage>) : BaseListAdapter<ChatMessage, ChatListAdapter.CHolder>(chatActivity, mBeans) {
    override fun getLayoutId(viewType: Int): Int {
        return when (viewType) {
            TYPE_MINE -> R.layout.activity_chat_message_text_mine
            TYPE_FRIEND -> R.layout.activity_chat_message_text_friend
            TYPE_FRIEND_IMAGE -> R.layout.activity_chat_message_image_friend
            TYPE_MINE_IMAGE -> R.layout.activity_chat_message_image_mine
            else -> R.layout.activity_chat_message_time
        }
    }

    override fun getItemViewType(position: Int): Int {
        val cm = mBeans[position]
        return if (cm.isTimeStamp) {
            TYPE_TIME
        } else if (cm.getToId() == chatActivity.viewModel!!.myId) {
            if (cm.getType() == ChatMessage.TYPE.IMG) TYPE_FRIEND_IMAGE else TYPE_FRIEND
        } else {
            if (cm.getType() == ChatMessage.TYPE.IMG) TYPE_MINE_IMAGE else TYPE_MINE
        }
    }

    override fun createViewHolder(v: View, viewType: Int): CHolder {
        return CHolder(v, viewType)
    }

    protected override fun bindHolder(holder: CHolder, data: ChatMessage?, position: Int) {
        if (data != null) {
            if (holder.read != null) {
                holder.read!!.visibility = if (data.isRead) View.VISIBLE else View.GONE
            }
            if (holder.viewType == TYPE_TIME && holder.content != null) {
                holder.content!!.text = TextUtils.getChatTimeText(chatActivity, data.createdTime)
            } else if (holder.avatar != null) {
                if (holder.viewType == TYPE_MINE || holder.viewType == TYPE_MINE_IMAGE) {
                    ImageUtils.loadLocalAvatarInto(chatActivity, chatActivity.viewModel!!.myAvatar, holder.avatar!!)
                } else {
                    ImageUtils.loadAvatarInto(chatActivity, chatActivity.viewModel!!.friendAvatar, holder.avatar!!)
                }
                if (holder.progress != null) {
                    if (data.isProgressing) {
                        holder.progress!!.visibility = View.VISIBLE
                    } else {
                        holder.progress!!.visibility = View.GONE
                    }
                }
                holder.bindSensitiveAndEmotion(data)
                holder.avatar!!.setOnClickListener { view: View? -> ActivityUtils.startProfileActivity(chatActivity, data.getFromId()) }
                if (holder.image != null && holder.progress != null) {
                    if (holder.progress!!.visibility != View.VISIBLE) {
                        ImageUtils.loadChatMessageInto(chatActivity, data.getContent(), holder.image!!)
                    } else {
                        //Glide.with(getThis()).load(data.getContent()).into(holder.image);
                        holder.image!!.setImageResource(R.drawable.place_holder_loading)
                    }
                } else if (holder.image != null) {
                    ImageUtils.loadChatMessageInto(chatActivity, data.getContent(), holder.image!!)
                }
            }
            holder.bindClickAction(data, position)
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
                if (!cm.isTimeStamp && cm.getType() == ChatMessage.TYPE.IMG) {
                    res.add(ImageUtils.getChatMessageImageUrl(cm.getContent()))
                }
            }
            return res
        }

    /**
     * 判断两时间戳相隔是否太远
     */
    private fun tooFar(t1: Timestamp, t2: Timestamp): Boolean {
        return Math.abs(t1.time - t2.time) > 10.toLong() * 60 * 1000 //取10分钟
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
            if (notification.getType() == MessageReadNotification.TYPE.ALL) {
                if (mBeans[i].createdTime.after(notification.getFromTime())) {
                    indexes.add(i)
                }
            } else if (mBeans[i].getId() == notification.getId()) {
                indexes.add(i)
                break
            }
        }
        for (index in indexes) {
            mBeans[index].isRead = true
            val holder = list.findViewHolderForAdapterPosition(index) as CHolder?
            holder?.showRead()
        }
    }

    /**
     * 当消息发送成功后，通知该消息更新界面
     *
     * @param list        recyclerview
     * @param sentMessage 已发送消息实体
     */
    fun messageSent(list: RecyclerView, sentMessage: ChatMessage) {
        var index = -1
        for (i in mBeans.indices.reversed()) {
            if (mBeans[i].uuid == sentMessage.uuid) {
                index = i
                break
            }
        }
        if (index >= 0) {
            mBeans[index] = sentMessage
            val holder = list.findViewHolderForAdapterPosition(index) as CHolder?
            if (holder != null) {
                holder.hideProgress()
                holder.bindSensitiveAndEmotion(sentMessage)
                holder.bindClickAction(sentMessage, index)
                if (sentMessage.getType() == ChatMessage.TYPE.IMG) {
                    holder.updateImage(sentMessage)
                }
            }
        }
    }

    /**
     * 清空列表
     */
    fun clear() {
        mBeans.clear()
        notifyDataSetChanged()
    }

    override fun notifyItemsAppended(newL: List<ChatMessage>) {
        //注意要取反
        if(newL is MutableList){
            newL.reverse()
        }
        if (mBeans.size > 0 && newL.size > 0) {
            val last = mBeans[mBeans.size - 1]
            if (tooFar(last.createdTime, newL[0].createdTime)) {
                super.notifyItemAppended(ChatMessage.getTimeStampHolderInstance(newL[0].createdTime))
            }
        }
        super.notifyItemsAppended(newL)
    }

    /**
     * 头部数据的更新，存在则更新，其余的插入到头部
     * @param chatMessages 新的头部消息
     */
    fun notifyHeadItemsUpdated(chatMessages:  List<ChatMessage>) {
        val ids = HashMap<String, ChatMessage?>()
        for (cm in chatMessages) {
            ids[cm.getId()] = cm
        }
        val toDelete: MutableList<ChatMessage?> = ArrayList()
        for (i in mBeans.indices.reversed()) {
            val cm = mBeans[i]
            if (ids.containsKey(cm.getId())) {
                if (ids[cm.getId()] != cm) {
                    notifyItemChanged(i)
                }
                toDelete.add(ids[cm.getId()])
            }
        }
        if(chatMessages is MutableList){
            chatMessages.removeAll(toDelete)
        }
        //其余的加到头部
        notifyItemsPushHead(chatMessages)
    }

    override fun notifyItemsPushHead(newL:  List<ChatMessage>) {
        if(newL is MutableList){
            newL.reverse() //取反
        }
        if (mBeans.size > 0 && newL.size > 0) {
            val top = mBeans[0]
            val newBottom = newL[newL.size - 1]
            if (tooFar(top.createdTime, newBottom.createdTime)) {
                if (!top.isTimeStamp) {
                    super.notifyItemPushHead(ChatMessage.getTimeStampHolderInstance(top.createdTime))
                }
            } else if (top.isTimeStamp) {
                super.notifyItemRemoveFromHead()
            }
        }
        if(newL is MutableList && newL.isNotEmpty()){
            newL.add(0, ChatMessage.getTimeStampHolderInstance(newL[0].createdTime))
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
            if (tooFar(last.createdTime, thi.createdTime)) {
                toAdd.add(0, ChatMessage.getTimeStampHolderInstance(thi.createdTime))
            }
            if (i == newL.size - 1) {
                toAdd.add(0, thi)
            }
        }
        if (toAdd.size > 0) {
            toAdd.add(0, ChatMessage.getTimeStampHolderInstance(toAdd[0].createdTime))
        }
        super.notifyItemChangedSmooth(toAdd, object : RefreshJudge<ChatMessage> {
            override fun judge(oldData: ChatMessage, newData: ChatMessage): Boolean {
                return oldData != newData
            }
        })
    }

    internal inner class CHolder(itemView: View, var viewType: Int) : BaseViewHolder(itemView) {

        @JvmField
        @BindView(R.id.content)
        var content: EmoticonsTextView? = null

        @JvmField
        @BindView(R.id.avatar)
        var avatar: ImageView? = null

        @JvmField
        @BindView(R.id.bubble)
        var bubble: View? = null


        @JvmField
        @BindView(R.id.progress)
        var progress: View? = null

        @JvmField
        @BindView(R.id.see)
        var see //点击查看敏感消息
                : ImageView? = null

        @JvmField
        @BindView(R.id.emotion)
        var emotion: ImageView? = null

        @JvmField
        @BindView(R.id.image)
        var image //图片
                : ImageView? = null

        @JvmField
        @BindView(R.id.read)
        var read: View? = null

        @JvmField
        @BindView(R.id.image_sensitive)
        var imageSensitivePlaceHolder: ViewGroup? = null
        var isSensitiveExpanded = false

        //隐藏加载圈圈
        fun hideProgress() {
            if (progress != null) {
                progress!!.visibility = View.GONE
            }
        }

        fun showRead() {
            if (read != null) {
                read!!.visibility = View.VISIBLE
            }
        }

        //切换敏感消息查看模式
        private fun switchSensitiveModeText(data: ChatMessage) {
            isSensitiveExpanded = !isSensitiveExpanded
            if (see == null) return
            if (isSensitiveExpanded) {
                content!!.text = data.getContent()
                see!!.setImageResource(R.drawable.ic_baseline_visibility_off_24)
                if (data.getType() == ChatMessage.TYPE.IMG && image != null && imageSensitivePlaceHolder != null) {
                    image!!.visibility = View.VISIBLE
                    imageSensitivePlaceHolder!!.visibility = View.GONE
                }
            } else if (data.isSensitive) {
                see!!.setImageResource(R.drawable.ic_baseline_visibility_24)
                content!!.setText(R.string.hint_sensitive_message)
                if (data.getType() == ChatMessage.TYPE.IMG && image != null && imageSensitivePlaceHolder != null) {
                    image!!.visibility = View.INVISIBLE
                    imageSensitivePlaceHolder!!.visibility = View.VISIBLE
                }
            }
        }

        //绑定敏感词状态
        fun bindSensitiveAndEmotion(data: ChatMessage) {
            isSensitiveExpanded = false
            if (data.getType() == ChatMessage.TYPE.IMG && see != null && image != null && imageSensitivePlaceHolder != null) {
                if (data.isSensitive) {
                    see!!.visibility = View.VISIBLE
                    see!!.setOnClickListener { view: View? -> switchSensitiveModeText(data) }
                    image!!.visibility = View.INVISIBLE
                    imageSensitivePlaceHolder!!.visibility = View.VISIBLE
                    content!!.setText(R.string.hint_sensitive_message)
                } else {
                    imageSensitivePlaceHolder!!.visibility = View.GONE
                    image!!.visibility = View.VISIBLE
                    see!!.visibility = View.GONE
                }
            } else if (see != null && emotion != null) {
                if (data.isSensitive) {
                    see!!.visibility = View.VISIBLE
                    emotion!!.visibility = View.GONE
                    see!!.setImageResource(R.drawable.ic_baseline_visibility_24)
                    see!!.setOnClickListener { view: View? -> switchSensitiveModeText(data) }
                    content!!.setText(R.string.hint_sensitive_message)
                } else {
                    see!!.visibility = View.GONE
                    content!!.text = data.getContent()
                    emotion!!.visibility = View.VISIBLE
                    val emotionValue = data.getEmotion()
                    var iconRes = R.drawable.ic_emotion_normal
                    if (emotionValue >= 2) {
                        iconRes = R.drawable.ic_emotion_pos_3
                    } else if (emotionValue >= 1) {
                        iconRes = R.drawable.ic_emotion_pos_2
                    } else if (emotionValue > 0) {
                        iconRes = R.drawable.ic_emotion_pos_1
                    } else if (emotionValue <= -2) {
                        iconRes = R.drawable.ic_emotion_neg_3
                    } else if (emotionValue <= -1) {
                        iconRes = R.drawable.ic_emotion_neg_2
                    } else if (emotionValue < 0) {
                        iconRes = R.drawable.ic_emotion_neg_1
                    }
                    emotion!!.setImageResource(iconRes)
                }
            }
        }

        //绑定点击事件
        fun bindClickAction(data: ChatMessage, position: Int) {
            if (mOnItemLongClickListener != null && bubble != null) {
                bubble!!.setOnLongClickListener { view: View? -> mOnItemLongClickListener!!.onItemLongClick(data, view, position) }
            }
            if (mOnItemClickListener != null && bubble != null) {
                bubble!!.setOnClickListener { view: View? -> mOnItemClickListener!!.onItemClick(data, view, position) }
            }
        }

        fun updateImage(data: ChatMessage) {
            if (image != null) {
                ImageUtils.loadChatMessageInto(chatActivity, data.getContent(), image!!)
            }
        }

    }

    companion object {
        private const val TYPE_MINE = 287
        private const val TYPE_FRIEND = 509
        private const val TYPE_MINE_IMAGE = 944
        private const val TYPE_FRIEND_IMAGE = 598
        private const val TYPE_TIME = 774
    }


}
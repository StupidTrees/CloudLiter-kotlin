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
import kotlin.math.abs

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
        } else if (cm.toId == chatActivity.viewModel!!.myId) {
            if (cm.getType() == ChatMessage.TYPE.IMG) TYPE_FRIEND_IMAGE else TYPE_FRIEND
        } else {
            if (cm.getType() == ChatMessage.TYPE.IMG) TYPE_MINE_IMAGE else TYPE_MINE
        }
    }

    override fun createViewHolder(v: View, viewType: Int): CHolder {
        return CHolder(v, viewType)
    }

    override fun bindHolder(holder: CHolder, data: ChatMessage?, position: Int) {
        if (data != null) {
            if (holder.read != null) {
                holder.read!!.visibility = if (data.read) View.VISIBLE else View.GONE
            }
            if (holder.viewType == TYPE_TIME && holder.content != null) {
                holder.content!!.text = data.createdAt?.let { TextUtils.getChatTimeText(chatActivity, it) }
            } else if (holder.avatar != null) {
                if (holder.viewType == TYPE_MINE || holder.viewType == TYPE_MINE_IMAGE) {
                    chatActivity.viewModel!!.myAvatar?.let { ImageUtils.loadLocalAvatarInto(chatActivity, it, holder.avatar!!) }
                } else {
                    chatActivity.viewModel!!.friendAvatar?.let { ImageUtils.loadAvatarInto(chatActivity, it, holder.avatar!!) }
                }
                if (holder.progress != null) {
                    if (data.isProgressing) {
                        holder.progress!!.visibility = View.VISIBLE
                    } else {
                        holder.progress!!.visibility = View.GONE
                    }
                }
                holder.bindSensitiveAndEmotion(data)
                holder.avatar!!.setOnClickListener { view: View? -> data.fromId?.let { ActivityUtils.startProfileActivity(chatActivity, it) } }
                if (holder.image != null && holder.progress != null) {
                    if (holder.progress!!.visibility != View.VISIBLE) {
                        data.content?.let { ImageUtils.loadChatMessageInto(chatActivity, it, holder.image!!) }
                    } else {
                        //Glide.with(getThis()).load(data.getContent()).into(holder.image);
                        holder.image!!.setImageResource(R.drawable.place_holder_loading)
                    }
                } else if (holder.image != null) {
                    data.content?.let { ImageUtils.loadChatMessageInto(chatActivity, it, holder.image!!) }
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
                    cm.content?.let {
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
        if (newL is MutableList) {
            newL.reverse()
        }
        if (mBeans.size > 0 && newL.size > 0) {
            val last = mBeans[mBeans.size - 1]
            if (tooFar(last.createdAt, newL[0].createdAt)) {
                super.notifyItemAppended(ChatMessage.getTimeStampHolderInstance(newL[0].createdAt))
            }
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
        if (mBeans.size > 0 && newL.size > 0) {
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
        var sensitiveExpanded = false

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
            sensitiveExpanded = !sensitiveExpanded
            if (see == null) return
            if (sensitiveExpanded) {
                content!!.text = data.content
                see!!.setImageResource(R.drawable.ic_baseline_visibility_off_24)
                if (data.getType() == ChatMessage.TYPE.IMG && image != null && imageSensitivePlaceHolder != null) {
                    image!!.visibility = View.VISIBLE
                    imageSensitivePlaceHolder!!.visibility = View.GONE
                }
            } else if (data.sensitive) {
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
            sensitiveExpanded = false
            if (data.getType() == ChatMessage.TYPE.IMG && see != null && image != null && imageSensitivePlaceHolder != null) {
                if (data.sensitive) {
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
                if (data.sensitive) {
                    see!!.visibility = View.VISIBLE
                    emotion!!.visibility = View.GONE
                    see!!.setImageResource(R.drawable.ic_baseline_visibility_24)
                    see!!.setOnClickListener { view: View? -> switchSensitiveModeText(data) }
                    content!!.setText(R.string.hint_sensitive_message)
                } else {
                    see!!.visibility = View.GONE
                    content!!.text = data.content
                    emotion!!.visibility = View.VISIBLE
                    val emotionValue = data.emotion
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
                data.content?.let { ImageUtils.loadChatMessageInto(chatActivity, it, image!!) }
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
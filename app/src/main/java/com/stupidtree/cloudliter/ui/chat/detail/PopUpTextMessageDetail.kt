package com.stupidtree.cloudliter.ui.chat.detail

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.ChatMessage
import com.stupidtree.cloudliter.databinding.ActivityChatPopupTextMessageDetailBinding
import com.stupidtree.cloudliter.databinding.ActivityChatPopupTextMessageDetailSegmentationItemBinding
import com.stupidtree.cloudliter.ui.base.BaseListAdapter
import com.stupidtree.cloudliter.ui.base.BaseViewHolder
import com.stupidtree.cloudliter.ui.widgets.TransparentBottomSheetDialog
import java.text.DecimalFormat

/**
 * 圆角的文本框底部弹窗
 */
@SuppressLint("NonConstantResourceId")
class PopUpTextMessageDetail : TransparentBottomSheetDialog<ActivityChatPopupTextMessageDetailBinding>() {

    /**
     * 适配器区
     */
    var listAdapter: LAdapter? = null

    /**
     * 不得已放在UI里的数据
     */
    private lateinit var chatMessage: ChatMessage
    fun setChatMessage(chatMessage: ChatMessage): PopUpTextMessageDetail {
        this.chatMessage = chatMessage
        return this
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_chat_popup_text_message_detail
    }

    override fun onStart() {
        super.onStart()
        listAdapter!!.notifyDataSetChanged()
    }

    override fun initViews(v: View) {
        listAdapter = context?.let { LAdapter(it, chatMessage.getExtraAsSegmentation()) }
        binding.list.adapter = listAdapter
        binding.list.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.emotion.text = DecimalFormat("#.###").format(chatMessage.emotion)
    }

    inner class LAdapter(mContext: Context, mBeans: MutableList<String>) : BaseListAdapter<String, LAdapter.LHolder>(mContext, mBeans) {


        override fun bindHolder(holder: LHolder, data: String?, position: Int) {
            if (data != null) {
                holder.binding.text.text = data
            }
            holder.binding.item.setOnClickListener { view: View? ->
                if (mOnItemClickListener != null) {
                    data?.let { mOnItemClickListener!!.onItemClick(it, view, position) }
                }
            }
        }

        inner class LHolder(itemView: ActivityChatPopupTextMessageDetailSegmentationItemBinding) : BaseViewHolder<ActivityChatPopupTextMessageDetailSegmentationItemBinding>(itemView)

        override fun getViewBinding(parent: ViewGroup, viewType: Int): ViewBinding {
            return ActivityChatPopupTextMessageDetailSegmentationItemBinding.inflate(layoutInflater,parent,false)
        }

        override fun createViewHolder(viewBinding: ViewBinding, viewType: Int): LHolder {
            return LHolder(viewBinding as ActivityChatPopupTextMessageDetailSegmentationItemBinding)
        }
    }

    override fun initViewBinding(v: View): ActivityChatPopupTextMessageDetailBinding {
        return ActivityChatPopupTextMessageDetailBinding.bind(v)
    }
}
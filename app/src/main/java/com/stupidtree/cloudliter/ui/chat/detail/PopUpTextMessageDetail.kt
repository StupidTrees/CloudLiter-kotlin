package com.stupidtree.cloudliter.ui.chat.detail

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.ChatMessage
import com.stupidtree.cloudliter.ui.base.BaseListAdapter
import com.stupidtree.cloudliter.ui.base.BaseViewHolder
import com.stupidtree.cloudliter.ui.widgets.TransparentBottomSheetDialog
import java.text.DecimalFormat

/**
 * 圆角的文本框底部弹窗
 */
@SuppressLint("NonConstantResourceId")
class PopUpTextMessageDetail : TransparentBottomSheetDialog() {
    /**
     * View绑定区
     */

    @JvmField
    @BindView(R.id.list)
    var list: RecyclerView? = null

    @JvmField
    @BindView(R.id.emotion)
    var emotion: TextView? = null

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
        list!!.adapter = listAdapter
        list!!.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        emotion!!.text = DecimalFormat("#.###").format(chatMessage.emotion)
    }

    inner class LAdapter(mContext: Context, mBeans: MutableList<String>) : BaseListAdapter<String, LAdapter.LHolder>(mContext, mBeans) {
        override fun getLayoutId(viewType: Int): Int {
            return R.layout.activity_chat_popup_text_message_detail_segmentation_item
        }

        override fun createViewHolder(v: View, viewType: Int): LHolder {
            return LHolder(v)
        }

        protected override fun bindHolder(holder: LHolder, data: String?, position: Int) {
            if (data != null) {
                holder.text!!.text = data
            }
            holder.item!!.setOnClickListener { view: View? ->
                if (mOnItemClickListener != null) {
                    data?.let { mOnItemClickListener!!.onItemClick(it, view, position) }
                }
            }
        }

        inner class LHolder(itemView: View) : BaseViewHolder(itemView) {
            @JvmField
            @BindView(R.id.text)
            var text: TextView? = null

            @JvmField
            @BindView(R.id.item)
            var item: ViewGroup? = null
        }
    }
}
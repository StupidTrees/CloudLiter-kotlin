package com.stupidtree.cloudliter.ui.chat

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.stupidtree.cloudliter.data.model.Yunmoji
import com.stupidtree.cloudliter.databinding.ActivityChatYunmojiItemBinding
import com.stupidtree.cloudliter.ui.base.BaseListAdapter
import com.stupidtree.cloudliter.ui.base.BaseViewHolder
import com.stupidtree.cloudliter.ui.chat.YunmojiListAdapter.YunmojiItemHolder

/**
 * 表情列表的适配器
 */
internal class YunmojiListAdapter(mContext: Context?, mBeans: MutableList<Yunmoji>) : BaseListAdapter<Yunmoji, YunmojiItemHolder>(mContext!!, mBeans) {


    override fun bindHolder(holder: YunmojiItemHolder, data: Yunmoji?, position: Int) {
        val yunmoji = mBeans[position]
        holder.binding.image.setImageResource(yunmoji.imageID)
        //表示当这项的图片点击时调用onItemClickListener
        if (mOnItemClickListener != null) {
            holder.binding.image.setOnClickListener { view: View? -> data?.let { mOnItemClickListener!!.onItemClick(it, view, position) } }
        }
    }

    internal class YunmojiItemHolder(itemView: ActivityChatYunmojiItemBinding) : BaseViewHolder<ActivityChatYunmojiItemBinding>(itemView)

    override fun getViewBinding(parent: ViewGroup, viewType: Int): ViewBinding {
        return ActivityChatYunmojiItemBinding.inflate(mInflater,parent,false)
    }

    override fun createViewHolder(viewBinding: ViewBinding, viewType: Int): YunmojiItemHolder {
        return YunmojiItemHolder(viewBinding as ActivityChatYunmojiItemBinding)
    }
}
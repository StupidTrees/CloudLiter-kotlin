package com.stupidtree.cloudliter.ui.chat

import android.content.Context
import android.view.View
import android.widget.ImageView
import butterknife.BindView
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.Yunmoji
import com.stupidtree.cloudliter.ui.base.BaseListAdapter
import com.stupidtree.cloudliter.ui.base.BaseViewHolder
import com.stupidtree.cloudliter.ui.chat.YunmojiListAdapter.YunmojiItemHolder

/**
 * 表情列表的适配器
 */
internal class YunmojiListAdapter(mContext: Context?, mBeans: MutableList<Yunmoji>) : BaseListAdapter<Yunmoji, YunmojiItemHolder>(mContext!!, mBeans) {
    override fun getLayoutId(viewType: Int): Int {
        return R.layout.activity_chat_yunmoji_item
    }

    override fun createViewHolder(v: View, viewType: Int): YunmojiItemHolder {
        return YunmojiItemHolder(v)
    }

    override fun bindHolder(holder: YunmojiItemHolder, data: Yunmoji?, position: Int) {
        val yunmoji = mBeans[position]
        holder.image!!.setImageResource(yunmoji.imageID)
        //表示当这项的图片点击时调用onItemClickListener
        if (mOnItemClickListener != null) {
            holder.image!!.setOnClickListener { view: View? -> data?.let { mOnItemClickListener!!.onItemClick(it, view, position) } }
        }
    }

    internal class YunmojiItemHolder(itemView: View) : BaseViewHolder(itemView) {
        @JvmField
        @BindView(R.id.image)
        var image: ImageView? = null
    }
}
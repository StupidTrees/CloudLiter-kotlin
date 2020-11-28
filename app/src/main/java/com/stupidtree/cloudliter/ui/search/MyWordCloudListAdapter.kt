package com.stupidtree.cloudliter.ui.search

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import butterknife.BindView
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.ui.base.BaseListAdapter
import com.stupidtree.cloudliter.ui.base.BaseViewHolder

@SuppressLint("NonConstantResourceId")
class MyWordCloudListAdapter(mContext: Context, mBeans: MutableList<String>) : BaseListAdapter<String, MyWordCloudListAdapter.WHolder>(mContext, mBeans) {

    class WHolder(itemView: View) : BaseViewHolder(itemView) {
        @BindView(R.id.title)
        lateinit var title: TextView

        @BindView(R.id.card)
        lateinit var card: CardView
    }

    override fun getLayoutId(viewType: Int): Int {
        return R.layout.activity_search_my_word_item
    }

    override fun createViewHolder(v: View, viewType: Int): WHolder {
        return WHolder(v)
    }

    override fun bindHolder(holder: WHolder, data: String?, position: Int) {
        holder.title.text = data
        if (mOnItemClickListener != null && data != null) {
            holder.card.setOnClickListener { v ->
                mOnItemClickListener!!.onItemClick(data, v, position)
            }
        }
    }

}
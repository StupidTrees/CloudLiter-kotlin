package com.stupidtree.cloudliter.ui.search

import android.content.Context
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.stupidtree.cloudliter.databinding.ActivitySearchMyWordItemBinding
import com.stupidtree.style.base.BaseListAdapter
import com.stupidtree.style.base.BaseViewHolder


class MyWordCloudListAdapter(mContext: Context, mBeans: MutableList<String>) : BaseListAdapter<String, MyWordCloudListAdapter.WHolder>(mContext, mBeans) {

    class WHolder(itemView: ActivitySearchMyWordItemBinding) : BaseViewHolder<ActivitySearchMyWordItemBinding>(itemView)




    override fun bindHolder(holder: WHolder, data: String?, position: Int) {
        holder.binding.title.text = data
        if (mOnItemClickListener != null && data != null) {
            holder.binding.card.setOnClickListener { v ->
                mOnItemClickListener!!.onItemClick(data, v, position)
            }
        }
    }

    override fun getViewBinding(parent: ViewGroup, viewType: Int): ViewBinding {
        return ActivitySearchMyWordItemBinding.inflate(mInflater,parent,false)
    }

    override fun createViewHolder(viewBinding: ViewBinding, viewType: Int): WHolder {
        return WHolder(viewBinding as ActivitySearchMyWordItemBinding)
    }

}
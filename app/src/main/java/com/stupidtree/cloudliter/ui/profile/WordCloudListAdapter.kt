package com.stupidtree.cloudliter.ui.profile

import android.content.Context
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.stupidtree.cloudliter.databinding.ActivityWordCloudListItemBinding
import com.stupidtree.cloudliter.ui.base.BaseListAdapter
import com.stupidtree.cloudliter.ui.base.BaseViewHolder

class WordCloudListAdapter(mContext: Context, mBeans: MutableList<Pair<String, Float?>>) : BaseListAdapter<Pair<String, Float?>, WordCloudListAdapter.WHolder>(mContext, mBeans) {


    class WHolder(viewBinding: ActivityWordCloudListItemBinding) : BaseViewHolder<ActivityWordCloudListItemBinding>(viewBinding)

    override fun getViewBinding(parent: ViewGroup, viewType: Int): ViewBinding {
        return ActivityWordCloudListItemBinding.inflate(mInflater,parent,false)
    }

    override fun createViewHolder(viewBinding: ViewBinding, viewType: Int): WHolder {
        return WHolder(viewBinding as ActivityWordCloudListItemBinding)
    }


    override fun bindHolder(holder: WHolder, data: Pair<String, Float?>?, position: Int) {
        holder.binding.name.text = data?.first
        holder.binding.delete.setOnClickListener {
            data?.let { it1 -> mOnItemClickListener?.onItemClick(it1,it,position) }
        }
    }

}
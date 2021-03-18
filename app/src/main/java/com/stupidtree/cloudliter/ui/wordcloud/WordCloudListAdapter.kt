package com.stupidtree.cloudliter.ui.wordcloud

import android.content.Context
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.stupidtree.cloudliter.databinding.ActivityWordCloudListItemBinding
import com.stupidtree.style.base.BaseListAdapter
import com.stupidtree.style.base.BaseViewHolder

class WordCloudListAdapter(mContext: Context, mBeans: MutableList<WordCloudEntity>) : BaseListAdapter<WordCloudEntity, WordCloudListAdapter.WHolder>(mContext, mBeans) {


    class WHolder(viewBinding: ActivityWordCloudListItemBinding) : BaseViewHolder<ActivityWordCloudListItemBinding>(viewBinding)

    override fun getViewBinding(parent: ViewGroup, viewType: Int): ViewBinding {
        return ActivityWordCloudListItemBinding.inflate(mInflater,parent,false)
    }

    override fun createViewHolder(viewBinding: ViewBinding, viewType: Int): WHolder {
        return WHolder(viewBinding as ActivityWordCloudListItemBinding)
    }


    override fun bindHolder(holder: WHolder, data: WordCloudEntity?, position: Int) {
        holder.binding.name.text = data?.name
        holder.binding.rank.text = (position+1).toString()
        holder.binding.delete.setOnClickListener {
            data?.let { it1 -> mOnItemClickListener?.onItemClick(it1,it,position) }
        }
    }

}
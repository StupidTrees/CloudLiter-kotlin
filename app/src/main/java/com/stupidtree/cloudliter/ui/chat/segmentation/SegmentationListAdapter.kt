package com.stupidtree.cloudliter.ui.chat.segmentation

import android.content.Context
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.stupidtree.accessibility.ai.segmentation.Token
import com.stupidtree.cloudliter.databinding.ActivityChatSegmentationItemBinding
import com.stupidtree.style.base.BaseListAdapter
import com.stupidtree.style.base.BaseViewHolder

class SegmentationListAdapter(mContext: Context, mBeans: MutableList<Token>) : BaseListAdapter<Token, SegmentationListAdapter.SHolder>(mContext, mBeans) {

    class SHolder(viewBinding: ActivityChatSegmentationItemBinding) : BaseViewHolder<ActivityChatSegmentationItemBinding>(viewBinding)

    override fun getViewBinding(parent: ViewGroup, viewType: Int): ViewBinding {
        return ActivityChatSegmentationItemBinding.inflate(mInflater, parent, false)
    }

    override fun createViewHolder(viewBinding: ViewBinding, viewType: Int): SHolder {
        return SHolder(viewBinding as ActivityChatSegmentationItemBinding)
    }

    override fun bindHolder(holder: SHolder, data: Token?, position: Int) {
        holder.binding.name.text = data?.name
    }
}
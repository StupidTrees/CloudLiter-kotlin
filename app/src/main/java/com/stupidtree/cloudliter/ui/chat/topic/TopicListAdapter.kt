package com.stupidtree.cloudliter.ui.chat.topic

import android.content.Context
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.stupidtree.cloudliter.databinding.ActivityChatTopicItemBinding
import com.stupidtree.cloudliter.ui.wordcloud.WordCloudEntity
import com.stupidtree.style.base.BaseListAdapter
import com.stupidtree.style.base.BaseViewHolder

class TopicListAdapter(mContext: Context, mBeans: MutableList<WordCloudEntity>) : BaseListAdapter<WordCloudEntity, TopicListAdapter.SHolder>(mContext, mBeans) {

    class SHolder(viewBinding: ActivityChatTopicItemBinding) : BaseViewHolder<ActivityChatTopicItemBinding>(viewBinding)

    override fun getViewBinding(parent: ViewGroup, viewType: Int): ViewBinding {
        return ActivityChatTopicItemBinding.inflate(mInflater, parent, false)
    }

    override fun createViewHolder(viewBinding: ViewBinding, viewType: Int): SHolder {
        return SHolder(viewBinding as ActivityChatTopicItemBinding)
    }

    override fun bindHolder(holder: SHolder, data: WordCloudEntity?, position: Int) {
        holder.binding.name.text = data?.name
    }
}
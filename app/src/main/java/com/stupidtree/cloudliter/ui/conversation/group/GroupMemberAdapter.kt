package com.stupidtree.cloudliter.ui.conversation.group

import android.content.Context
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.stupidtree.cloudliter.databinding.ActivityConversationGroupMemberItemBinding
import com.stupidtree.cloudliter.utils.ImageUtils
import com.stupidtree.style.base.BaseListAdapter
import com.stupidtree.style.base.BaseViewHolder

class GroupMemberAdapter(mContext: Context, mBeans: MutableList<GroupMemberEntity>) : BaseListAdapter<GroupMemberEntity, GroupMemberAdapter.GHolder>(mContext, mBeans) {

    class GHolder(viewBinding: ActivityConversationGroupMemberItemBinding) : BaseViewHolder<ActivityConversationGroupMemberItemBinding>(viewBinding)

    override fun getViewBinding(parent: ViewGroup, viewType: Int): ViewBinding {
        return ActivityConversationGroupMemberItemBinding.inflate(mInflater,parent,false)
    }

    override fun createViewHolder(viewBinding: ViewBinding, viewType: Int): GHolder {
        return GHolder(viewBinding as ActivityConversationGroupMemberItemBinding)
    }

    override fun bindHolder(holder: GHolder, data: GroupMemberEntity?, position: Int) {
        ImageUtils.loadAvatarInto(mContext,data?.userAvatar,holder.binding.avatar)
        holder.binding.remark.text = data?.getName()
        holder.binding.userLayout.setOnClickListener {
            if (data != null) {
                mOnItemClickListener?.onItemClick(data,it,position)
            }
        }
    }
}
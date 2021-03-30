package com.stupidtree.cloudliter.ui.chat.read

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.stupidtree.cloudliter.data.model.UserRelation
import com.stupidtree.cloudliter.databinding.DialogBottomReadUserItemBinding
import com.stupidtree.cloudliter.databinding.DialogBottomPickFriendBinding
import com.stupidtree.cloudliter.databinding.FragmentContactListItemBinding
import com.stupidtree.cloudliter.utils.ImageUtils
import com.stupidtree.cloudliter.utils.TextUtils
import com.stupidtree.style.base.BaseListAdapter
import com.stupidtree.style.base.BaseViewHolder
import com.stupidtree.style.base.BasicMultipleCheckableListAdapter

class ReadUserListAdapter(mContext: Context, mBeans: MutableList<ReadUser>, minCheckNumber: Int) : BaseListAdapter<ReadUser, ReadUserListAdapter.XHolder>(mContext, mBeans) {

    class XHolder(itemView: DialogBottomReadUserItemBinding) : BaseViewHolder<DialogBottomReadUserItemBinding>(itemView)

    override fun getViewBinding(parent: ViewGroup, viewType: Int): ViewBinding {
        return DialogBottomReadUserItemBinding.inflate(mInflater, parent, false)
    }

    override fun createViewHolder(viewBinding: ViewBinding, viewType: Int): XHolder {
        return XHolder(viewBinding as DialogBottomReadUserItemBinding)
    }

    override fun bindHolder(holder: XHolder, data: ReadUser?, position: Int) {
        holder.binding.name.text = data?.name
        ImageUtils.loadAvatarInto(mContext, data?.userId, holder.binding.avatar, useUserId = true)
    }
}

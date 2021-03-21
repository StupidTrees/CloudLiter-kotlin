package com.stupidtree.cloudliter.ui.main.contact.popup

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.stupidtree.cloudliter.data.model.UserRelation
import com.stupidtree.cloudliter.databinding.DialogBottomPickFreindItemBinding
import com.stupidtree.cloudliter.databinding.DialogBottomPickFriendBinding
import com.stupidtree.cloudliter.databinding.FragmentContactListItemBinding
import com.stupidtree.cloudliter.utils.ImageUtils
import com.stupidtree.cloudliter.utils.TextUtils
import com.stupidtree.style.base.BaseViewHolder
import com.stupidtree.style.base.BasicMultipleCheckableListAdapter

class PickFriendListAdapter(mContext: Context, mBeans: MutableList<UserRelation>, minCheckNumber: Int) : BasicMultipleCheckableListAdapter<UserRelation, PickFriendListAdapter.XHolder>(mContext, mBeans, minCheckNumber) {
    override fun bindHolder(holder: XHolder, data: UserRelation?, position: Int) {
        if (data != null) {
            //显示头像
            ImageUtils.loadAvatarInto(mContext, data.friendAvatar, holder.binding.avatar)
            //显示名称(备注)
            if (!TextUtils.isEmpty(data.remark)) {
                holder.binding.name.text = data.remark
            } else {
                holder.binding.name.text = data.friendNickname
            }
            if (selectedIndex.contains(position)) { //若被选中
                holder.binding.check.visibility = View.VISIBLE
            } else {
                holder.binding.check.visibility = View.GONE
            }
            holder.binding.item.setOnClickListener {
                checkItem(position)
            }
        }
    }

    class XHolder(itemView: DialogBottomPickFreindItemBinding) : BaseViewHolder<DialogBottomPickFreindItemBinding>(itemView)

    override fun getViewBinding(parent: ViewGroup, viewType: Int): ViewBinding {
        return DialogBottomPickFreindItemBinding.inflate(mInflater,parent,false)
    }

    override fun createViewHolder(viewBinding: ViewBinding, viewType: Int): XHolder {
        return XHolder(viewBinding as DialogBottomPickFreindItemBinding)
    }
}

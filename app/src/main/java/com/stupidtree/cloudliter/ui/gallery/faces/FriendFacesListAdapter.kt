package com.stupidtree.cloudliter.ui.gallery.faces

import android.content.Context
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.stupidtree.cloudliter.databinding.ActivityFriendFacesListItemBinding
import com.stupidtree.style.base.BaseListAdapter
import com.stupidtree.style.base.BaseViewHolder
import com.stupidtree.cloudliter.utils.ImageUtils
import com.stupidtree.cloudliter.utils.PlacesUtils

class FriendFacesListAdapter(mContext: Context, mBeans: MutableList<FriendFaceEntity>) : BaseListAdapter<FriendFaceEntity, FriendFacesListAdapter.SHolder>(mContext, mBeans) {

    class SHolder(viewBinding: ActivityFriendFacesListItemBinding) : BaseViewHolder<ActivityFriendFacesListItemBinding>(viewBinding)

    override fun bindHolder(holder: SHolder, data: FriendFaceEntity?, position: Int) {
        holder.binding.name.text = data?.userName
        holder.binding.card.setOnClickListener {
            data?.let { it1 -> mOnItemClickListener?.onItemClick(it1,it,position) }
        }
        ImageUtils.loadAvatarInto(mContext, data?.userId, holder.binding.image,useUserId = true)
    }

    override fun getViewBinding(parent: ViewGroup, viewType: Int): ViewBinding {
        return ActivityFriendFacesListItemBinding.inflate(mInflater, parent, false)
    }

    override fun createViewHolder(viewBinding: ViewBinding, viewType: Int): SHolder {
        return SHolder(viewBinding as ActivityFriendFacesListItemBinding)
    }
}
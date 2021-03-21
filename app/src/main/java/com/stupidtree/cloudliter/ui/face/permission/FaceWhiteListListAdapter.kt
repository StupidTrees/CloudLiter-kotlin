package com.stupidtree.cloudliter.ui.face.permission

import android.content.Context
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.stupidtree.cloudliter.databinding.ActivityFaceWhiteListListItemBinding
import com.stupidtree.cloudliter.utils.ImageUtils
import com.stupidtree.style.base.BaseListAdapter
import com.stupidtree.style.base.BaseViewHolder

class FaceWhiteListListAdapter(mContext: Context, mBeans: MutableList<FaceWhiteListEntity>) : BaseListAdapter<FaceWhiteListEntity, FaceWhiteListListAdapter.WHolder>(mContext, mBeans) {


    class WHolder(viewBinding: ActivityFaceWhiteListListItemBinding) : BaseViewHolder<ActivityFaceWhiteListListItemBinding>(viewBinding)

    override fun getViewBinding(parent: ViewGroup, viewType: Int): ViewBinding {
        return ActivityFaceWhiteListListItemBinding.inflate(mInflater,parent,false)
    }

    override fun createViewHolder(viewBinding: ViewBinding, viewType: Int): WHolder {
        return WHolder(viewBinding as ActivityFaceWhiteListListItemBinding)
    }


    override fun bindHolder(holder: WHolder, data: FaceWhiteListEntity?, position: Int) {
        holder.binding.name.text = data?.userName
        ImageUtils.loadAvatarInto(mContext,data?.userAvatar,holder.binding.avatar)
        holder.binding.delete.setOnClickListener {
            data?.let { it1 -> mOnItemClickListener?.onItemClick(it1,it,position) }
        }
    }

}
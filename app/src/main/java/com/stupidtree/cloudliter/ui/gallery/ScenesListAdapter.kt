package com.stupidtree.cloudliter.ui.gallery

import android.content.Context
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.stupidtree.cloudliter.databinding.ActivityScenesListItemBinding
import com.stupidtree.style.base.BaseListAdapter
import com.stupidtree.style.base.BaseViewHolder
import com.stupidtree.cloudliter.utils.ImageUtils
import com.stupidtree.cloudliter.utils.PlacesUtils

class ScenesListAdapter(mContext: Context, mBeans: MutableList<SceneEntity>) : BaseListAdapter<SceneEntity, ScenesListAdapter.SHolder>(mContext, mBeans) {

    class SHolder(viewBinding: ActivityScenesListItemBinding) : BaseViewHolder<ActivityScenesListItemBinding>(viewBinding)

    override fun bindHolder(holder: SHolder, data: SceneEntity?, position: Int) {
        holder.binding.name.text = data?.key?.let { PlacesUtils.getNameForSceneKey(mContext, it) }
        holder.binding.card.setOnClickListener {
            data?.let { it1 -> mOnItemClickListener?.onItemClick(it1,it,position) }
        }
        ImageUtils.loadCloudImageInto(mContext, data?.representId, holder.binding.image)
    }

    override fun getViewBinding(parent: ViewGroup, viewType: Int): ViewBinding {
        return ActivityScenesListItemBinding.inflate(mInflater, parent, false)
    }

    override fun createViewHolder(viewBinding: ViewBinding, viewType: Int): SHolder {
        return SHolder(viewBinding as ActivityScenesListItemBinding)
    }
}
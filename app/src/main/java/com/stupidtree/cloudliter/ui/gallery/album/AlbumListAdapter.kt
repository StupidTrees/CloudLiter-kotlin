package com.stupidtree.cloudliter.ui.gallery.album

import android.content.Context
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.stupidtree.cloudliter.databinding.ActivityAlbumListItemBinding
import com.stupidtree.cloudliter.ui.base.BaseListAdapter
import com.stupidtree.cloudliter.ui.base.BaseViewHolder
import com.stupidtree.cloudliter.utils.ImageUtils

class AlbumListAdapter(mContext: Context, mBeans: MutableList<String>) : BaseListAdapter<String, AlbumListAdapter.AHolder>(mContext, mBeans) {


    class AHolder(viewBinding: ActivityAlbumListItemBinding) : BaseViewHolder<ActivityAlbumListItemBinding>(viewBinding)

    override fun getViewBinding(parent: ViewGroup, viewType: Int): ViewBinding {
        return ActivityAlbumListItemBinding.inflate(mInflater, parent, false)
    }

    override fun createViewHolder(viewBinding: ViewBinding, viewType: Int): AHolder {
        return AHolder(viewBinding as ActivityAlbumListItemBinding)
    }

    override fun bindHolder(holder: AHolder, data: String?, position: Int) {
        ImageUtils.loadCloudImageInto(mContext, data, holder.binding.image)
        holder.binding.card.setOnClickListener {
            if (data != null) {
                mOnItemClickListener?.onItemClick(data, it, position)
            }
        }
    }

}
package com.stupidtree.cloudliter.ui.face

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.stupidtree.cloudliter.databinding.ActivityMyFaceListItemBinding
import com.stupidtree.cloudliter.ui.base.BaseListAdapter
import com.stupidtree.cloudliter.ui.base.BaseViewHolder

class MyFaceAdapter(mContext: Context, mBeans: MutableList<FaceEntity>, val viewModel:MyFaceViewModel) : BaseListAdapter<FaceEntity, MyFaceAdapter.MHolder>(mContext, mBeans) {

    class MHolder(viewBinding: ActivityMyFaceListItemBinding) :BaseViewHolder<ActivityMyFaceListItemBinding>(viewBinding)

    override fun getViewBinding(parent: ViewGroup, viewType: Int): ViewBinding {
        return ActivityMyFaceListItemBinding.inflate(mInflater,parent,false)
    }

    override fun createViewHolder(viewBinding: ViewBinding, viewType: Int): MHolder {
        return MHolder(viewBinding as ActivityMyFaceListItemBinding)
    }

    @SuppressLint("SetTextI18n")
    override fun bindHolder(holder: MHolder, data: FaceEntity?, position: Int) {
        data?.let{
            holder.binding.name.text = "FACE"+data.id
        }
        com.stupidtree.cloudliter.utils.ImageUtils.loadFaceImageInto(mContext,
                viewModel.getUserToken(),data?.id?:"",holder.binding.imageView)
        holder.binding.delete.setOnClickListener {
            data?.let { it1 -> mOnItemClickListener?.onItemClick(it1,it,position) }
        }
    }
}
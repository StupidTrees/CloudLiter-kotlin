package com.stupidtree.cloudliter.ui.group

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.stupidtree.cloudliter.data.model.RelationGroup
import com.stupidtree.cloudliter.databinding.ActivityGroupEditorListItemBinding
import com.stupidtree.style.base.BaseListAdapter
import com.stupidtree.style.base.BaseViewHolder
import com.stupidtree.cloudliter.ui.group.GroupListAdapter.GHolder

class GroupListAdapter(mContext: Context, mBeans: MutableList<RelationGroup>) : BaseListAdapter<RelationGroup, GHolder>(mContext, mBeans) {
    interface OnDeleteClickListener {
        fun onDeleteClick(button: View?, group: RelationGroup, position: Int)
    }

    var onDeleteClickListener: OnDeleteClickListener? = null



    override fun bindHolder(holder: GHolder, data: RelationGroup?, position: Int) {
        if (data != null) {
            holder.binding.name.text = data.groupName
            if (onDeleteClickListener != null) {
                holder.binding.delete.setOnClickListener { view: View? -> onDeleteClickListener!!.onDeleteClick(view, data, position) }
            }
            if(mOnItemClickListener!=null){
                holder.binding.card.setOnClickListener{
                    mOnItemClickListener!!.onItemClick(data,it,position)
                }
            }
        }
    }

    class GHolder(itemView: ActivityGroupEditorListItemBinding) : BaseViewHolder<ActivityGroupEditorListItemBinding>(itemView)

    override fun getViewBinding(parent: ViewGroup, viewType: Int): ViewBinding {
        return ActivityGroupEditorListItemBinding.inflate(mInflater,parent,false)
    }

    override fun createViewHolder(viewBinding: ViewBinding, viewType: Int): GHolder {
        return GHolder(viewBinding as ActivityGroupEditorListItemBinding)
    }
}
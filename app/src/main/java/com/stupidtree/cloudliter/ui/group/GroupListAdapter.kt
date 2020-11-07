package com.stupidtree.cloudliter.ui.group

import android.content.Context
import android.view.View
import android.widget.TextView
import butterknife.BindView
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.RelationGroup
import com.stupidtree.cloudliter.ui.base.BaseListAdapter
import com.stupidtree.cloudliter.ui.base.BaseViewHolder
import com.stupidtree.cloudliter.ui.group.GroupListAdapter.GHolder

class GroupListAdapter(mContext: Context, mBeans: MutableList<RelationGroup>) : BaseListAdapter<RelationGroup, GHolder>(mContext, mBeans) {
    interface OnDeleteClickListener {
        fun OnDeleteClick(button: View?, group: RelationGroup, position: Int)
    }

    var onDeleteClickListener: OnDeleteClickListener? = null

    override fun getLayoutId(viewType: Int): Int {
        return R.layout.activity_group_editor_list_item
    }

    override fun createViewHolder(v: View, viewType: Int): GHolder {
        return GHolder(v)
    }

    protected override fun bindHolder(holder: GHolder, data: RelationGroup?, position: Int) {
        if (data != null) {
            holder.name!!.text = data.groupName
            if (onDeleteClickListener != null) {
                holder.delete!!.setOnClickListener { view: View? -> onDeleteClickListener!!.OnDeleteClick(view, data, position) }
            }
        }
    }

    class GHolder(itemView: View) : BaseViewHolder(itemView) {
        @JvmField
        @BindView(R.id.delete)
        var delete: View? = null

        @JvmField
        @BindView(R.id.name)
        var name: TextView? = null
    }
}
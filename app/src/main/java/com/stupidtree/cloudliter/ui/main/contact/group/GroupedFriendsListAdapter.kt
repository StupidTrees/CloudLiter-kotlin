package com.stupidtree.cloudliter.ui.main.contact.group

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.donkingliang.groupedadapter.adapter.GroupedRecyclerViewAdapter
import com.donkingliang.groupedadapter.holder.BaseViewHolder
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.UserRelation
import com.stupidtree.cloudliter.utils.AnimationUtils
import com.stupidtree.cloudliter.utils.ImageUtils
import com.stupidtree.cloudliter.utils.TextUtils
import java.util.*

class GroupedFriendsListAdapter(context: Context?) : GroupedRecyclerViewAdapter(context) {

    val TYPE_LABEL = 0
    val TYPE_ADD = 1

    var groupEntities: MutableList<ExpandableGroupEntity> = ArrayList()
    override fun getGroupCount(): Int {
        return groupEntities.size+1
    }

    override fun getHeaderViewType(groupPosition: Int): Int {
        return if(groupPosition>=groupEntities.size){
            TYPE_ADD
        }else{
            TYPE_LABEL
        }
    }

    override fun getChildrenCount(groupPosition: Int): Int {

        return if (groupPosition>=groupEntities.size||!groupEntities[groupPosition].isExpanded) {
            0
        } else groupEntities[groupPosition].childrenCount
    }

    override fun hasHeader(groupPosition: Int): Boolean {
        return true
    }

    override fun hasFooter(groupPosition: Int): Boolean {
        return false
    }

    override fun getHeaderLayout(viewType: Int): Int {
        return if(viewType==TYPE_LABEL) R.layout.fragment_contact_group_item_label
        else R.layout.fragment_contact_group_item_add
    }

    override fun getFooterLayout(viewType: Int): Int {
        return 0
    }

    override fun getChildLayout(viewType: Int): Int {
        return R.layout.fragment_contact_list_item
    }

    override fun onBindHeaderViewHolder(holder: BaseViewHolder, groupPosition: Int) {
        if(groupPosition<groupEntities.size){
            holder.get<View>(R.id.icon).rotation = if (groupEntities[groupPosition].isExpanded) 90f else 0f
            if (groupEntities[groupPosition].groupId == "null") {
                holder.setText(R.id.name, mContext.getString(R.string.not_assigned_group))
            } else {
                holder.setText(R.id.name, groupEntities[groupPosition].groupName)
            }
        }
    }

    override fun onBindFooterViewHolder(holder: BaseViewHolder, groupPosition: Int) {}
    override fun onBindChildViewHolder(holder: BaseViewHolder, groupPosition: Int, childPosition: Int) {
        val avatar = holder.get<ImageView>(R.id.avatar)
        val name = holder.get<TextView>(R.id.name)
        val data = groupEntities[groupPosition].getChildAt(childPosition)
        //显示头像
        ImageUtils.loadAvatarInto(mContext, data.friendAvatar, avatar)
        //显示名称(备注)
        if (!TextUtils.isEmpty(data.remark)) {
            name.text = data.remark
        } else {
            name.text = data.friendNickname
        }
    }

    /**
     * 收起一个组
     */
    fun toggleGroup(recyclerView: RecyclerView, groupPosition: Int) {
        val entity = groupEntities[groupPosition]
        if (entity.isExpanded) {
            entity.isExpanded = false
            notifyChildrenRemoved(groupPosition)
        } else {
            entity.isExpanded = true
            notifyChildrenInserted(groupPosition)
        }
        val index = getPositionForGroup(groupPosition)
        val holder = recyclerView.findViewHolderForAdapterPosition(index) as BaseViewHolder?
        if (holder != null) {
            holder.get<View>(R.id.icon).rotation = if (entity.isExpanded) 0f else 90f
            AnimationUtils.rotateRightQuarterTo(holder.get(R.id.icon), entity.isExpanded)
        }
    }

    /**
     * 通知一组里的所有子项删除
     *
     * @param groupPosition
     */
    override fun notifyChildrenRemoved(groupPosition: Int) {
        if (groupPosition < mStructures.size) {
            val index = getPositionForChild(groupPosition, 0)
            if (index >= 0) {
                val structure = mStructures[groupPosition]
                val itemCount = structure.childrenCount
                structure.childrenCount = 0
                notifyItemRangeRemoved(index, itemCount)
                Log.e("remove", "$index,$itemCount")
            }
        }
    }

    fun notifyItemChangedSmooth(newL: List<UserRelation>) {
        val oldExpanded = HashMap<String, Boolean>()
        for (entity in groupEntities) {
            oldExpanded[entity.groupId] = entity.isExpanded
        }
        groupEntities.clear()
        val groupMap = HashMap<String, ExpandableGroupEntity?>()
        for (ur in newL) {
            val tmpId = if (ur.groupId == null) "null" else ur.groupId!!
            if (!groupMap.containsKey(tmpId)) {
                val e = ExpandableGroupEntity(tmpId, ur.groupName)
                val expanded = oldExpanded[tmpId]
                e.isExpanded = expanded ?: false
                groupMap[tmpId] = e
            }
            groupMap[tmpId]?.addChild(ur)
        }
        for( vl in groupMap.values){
            vl?.let { groupEntities.add(it) }
        }
        notifyDataChanged()
    }

    class ExpandableGroupEntity(var groupId: String, var groupName: String?) {
        var children: MutableList<UserRelation> = LinkedList()
        var isExpanded = false
        val childrenCount: Int
            get() = children.size

        fun getChildAt(childPosition: Int): UserRelation {
            return children[childPosition]
        }

        fun addChild(ur: UserRelation) {
            children.add(ur)
        }

    }
}
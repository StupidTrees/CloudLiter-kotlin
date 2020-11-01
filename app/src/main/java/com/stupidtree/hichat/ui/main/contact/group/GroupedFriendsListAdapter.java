package com.stupidtree.hichat.ui.main.contact.group;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.donkingliang.groupedadapter.adapter.GroupedRecyclerViewAdapter;
import com.donkingliang.groupedadapter.holder.BaseViewHolder;
import com.donkingliang.groupedadapter.structure.GroupStructure;
import com.stupidtree.hichat.R;
import com.stupidtree.hichat.data.model.UserRelation;
import com.stupidtree.hichat.ui.base.BaseListAdapter;
import com.stupidtree.hichat.utils.AnimationUtils;
import com.stupidtree.hichat.utils.ImageUtils;
import com.stupidtree.hichat.utils.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;

public class GroupedFriendsListAdapter extends GroupedRecyclerViewAdapter {

    List<ExpandableGroupEntity> groupEntities = new ArrayList<>();

    public GroupedFriendsListAdapter(Context context) {
        super(context);
    }

    @Override
    public int getGroupCount() {
        return groupEntities.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (!groupEntities.get(groupPosition).isExpanded()) {
            return 0;
        }
        return groupEntities.get(groupPosition).getChildrenCount();
    }

    @Override
    public boolean hasHeader(int groupPosition) {
        return true;
    }

    @Override
    public boolean hasFooter(int groupPosition) {
        return false;
    }

    @Override
    public int getHeaderLayout(int viewType) {
        return R.layout.fragment_contact_group_item_label;
    }


    @Override
    public int getFooterLayout(int viewType) {
        return 0;
    }

    @Override
    public int getChildLayout(int viewType) {
        return R.layout.fragment_contact_list_item;
    }

    @Override
    public void onBindHeaderViewHolder(BaseViewHolder holder, int groupPosition) {
        holder.get(R.id.icon).setRotation(groupEntities.get(groupPosition).isExpanded()?90f:0f);
        Log.e("rotation",groupPosition+":"+holder.get(R.id.icon).getRotation());
        if (Objects.equals(groupEntities.get(groupPosition).groupId, "null")) {
            holder.setText(R.id.name, mContext.getString(R.string.not_assigned_group));
        } else {
            holder.setText(R.id.name, groupEntities.get(groupPosition).groupName);
        }
    }

    @Override
    public void onBindFooterViewHolder(BaseViewHolder holder, int groupPosition) {

    }

    @Override
    public void onBindChildViewHolder(BaseViewHolder holder, int groupPosition, int childPosition) {
        ImageView avatar = holder.get(R.id.avatar);
        TextView name = holder.get(R.id.name);
        UserRelation data = groupEntities.get(groupPosition).getChildAt(childPosition);
        //显示头像
        ImageUtils.loadAvatarInto(mContext, data.getFriendAvatar(), avatar);
        //显示名称(备注)
        if (!TextUtils.isEmpty(data.getRemark())) {
            name.setText(data.getRemark());
        } else {
            name.setText(data.getFriendNickname());
        }

    }


    /**
     * 收起一个组
     */
    public void toggleGroup(RecyclerView recyclerView, int groupPosition) {
        ExpandableGroupEntity entity = groupEntities.get(groupPosition);
        if (entity.isExpanded()) {
            entity.setExpanded(false);
            notifyChildrenRemoved(groupPosition);
        } else {
            entity.setExpanded(true);
            notifyChildrenInserted(groupPosition);
        }
        int index = getPositionForGroup(groupPosition);
        BaseViewHolder holder = (BaseViewHolder) recyclerView.findViewHolderForAdapterPosition(index);
        if (holder != null) {
            holder.get(R.id.icon).setRotation(entity.isExpanded() ? 0f : 90f);
            AnimationUtils.rotateRightQuarterTo(holder.get(R.id.icon), entity.isExpanded());
        }

    }

    /**
     * 通知一组里的所有子项删除
     *
     * @param groupPosition
     */
    @Override
    public void notifyChildrenRemoved(int groupPosition) {
        if (groupPosition < mStructures.size()) {
            int index = getPositionForChild(groupPosition, 0);
            if (index >= 0) {
                GroupStructure structure = mStructures.get(groupPosition);
                int itemCount = structure.getChildrenCount();
                structure.setChildrenCount(0);
                notifyItemRangeRemoved(index, itemCount);
                Log.e("remove", index + "," + itemCount);
            }
        }
    }

    public void notifyItemChangedSmooth(List<UserRelation> newL) {

        HashMap<String, Boolean> oldExpanded = new HashMap<>();
        for (ExpandableGroupEntity entity : groupEntities) {
            oldExpanded.put(entity.groupId, entity.expanded);
        }
        groupEntities.clear();
        HashMap<String, ExpandableGroupEntity> groupMap = new HashMap<>();
        for (UserRelation ur : newL) {
            String tmpId = ur.getGroupId() == null ? "null" : ur.getGroupId();
            if (!groupMap.containsKey(tmpId)) {
                ExpandableGroupEntity e = new ExpandableGroupEntity(tmpId, ur.getGroupName());
                Boolean expanded = oldExpanded.get(tmpId);
                e.setExpanded(expanded == null ? false : expanded);
                groupMap.put(tmpId, e);
            }
            Objects.requireNonNull(groupMap.get(tmpId)).addChild(ur);
        }
        groupEntities.addAll(groupMap.values());
        notifyDataChanged();
    }


    static class ExpandableGroupEntity {
        List<UserRelation> children;
        String groupId;
        String groupName;
        boolean expanded;

        ExpandableGroupEntity(String groupId, String groupName) {
            this.groupName = groupName;
            this.groupId = groupId;
            expanded = false;
            children = new LinkedList<>();
        }

        public int getChildrenCount() {
            return children.size();
        }

        public boolean isExpanded() {
            return expanded;
        }

        public UserRelation getChildAt(int childPosition) {
            return children.get(childPosition);
        }

        public void setExpanded(boolean expanded) {
            this.expanded = expanded;
        }

        public void addChild(UserRelation ur) {
            this.children.add(ur);
        }
    }

}

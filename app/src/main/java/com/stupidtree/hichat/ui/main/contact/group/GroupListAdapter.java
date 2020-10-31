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
import com.stupidtree.hichat.R;
import com.stupidtree.hichat.data.model.UserRelation;
import com.stupidtree.hichat.ui.base.BaseListAdapter;
import com.stupidtree.hichat.utils.ImageUtils;
import com.stupidtree.hichat.utils.TextUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;

public class GroupListAdapter extends GroupedRecyclerViewAdapter {

    List<String> groupList = new LinkedList<>();
    HashMap<String, String> id2Name = new HashMap<>();
    HashMap<String, List<UserRelation>> groupSorted = new HashMap<>();

    public GroupListAdapter(Context context) {
        super(context);
    }

    @Override
    public int getGroupCount() {
        return groupList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return groupSorted.get(groupList.get(groupPosition)).size();
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
        holder.setText(R.id.name, id2Name.get(groupList.get(groupPosition)));
    }

    @Override
    public void onBindFooterViewHolder(BaseViewHolder holder, int groupPosition) {

    }

    @Override
    public void onBindChildViewHolder(BaseViewHolder holder, int groupPosition, int childPosition) {
        ImageView avatar = holder.get(R.id.avatar);
        TextView name = holder.get(R.id.name);
        UserRelation data = groupSorted.get(groupList.get(groupPosition)).get(childPosition);
        //显示头像
        ImageUtils.loadAvatarInto(mContext, data.getFriendAvatar(), avatar);
        //显示名称(备注)
        if (!TextUtils.isEmpty(data.getRemark())) {
            name.setText(data.getRemark());
        } else {
            name.setText(data.getFriendNickname());
        }
//        //设置点击事件
//        if (mOnItemClickListener != null) {
//            holder.item.setOnClickListener(view -> mOnItemClickListener.onItemClick(data, view, position));
//        }

    }


    /**
     * 展开一个组
     *
     * @param groupPosition
     */
    public void expandGroup(int groupPosition) {
        notifyChildrenInserted(groupPosition);
    }

    /**
     * 收起一个组
     *
     * @param groupPosition
     */
    public void collapseGroup(int groupPosition) {
        Log.e("groupPosition", groupPosition + "," + getChildrenCount(groupPosition));
        notifyChildrenRemoved(groupPosition);
    }

    public void notifyItemChangedSmooth(List<UserRelation> newL) {
        groupSorted.clear();
        id2Name.clear();
        groupList.clear();
        for (UserRelation ur : newL) {
            String tmpId = ur.getGroupId() == null ? "null" : ur.getGroupId();
            if (!groupSorted.containsKey(tmpId)) {
                groupList.add(tmpId);
                groupSorted.put(tmpId, new LinkedList<>());
                id2Name.put(tmpId, ur.getGroupName() == null ? "未分组" : ur.getGroupName());
            }
            List<UserRelation> list = groupSorted.get(tmpId);
            if (list != null) {
                list.add(ur);
            }
        }
        notifyDataChanged();
    }


}

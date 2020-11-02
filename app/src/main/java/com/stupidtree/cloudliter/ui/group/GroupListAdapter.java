package com.stupidtree.cloudliter.ui.group;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.stupidtree.cloudliter.R;
import com.stupidtree.cloudliter.data.model.RelationGroup;
import com.stupidtree.cloudliter.ui.base.BaseListAdapter;
import com.stupidtree.cloudliter.ui.base.BaseViewHolder;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import butterknife.BindView;

public class GroupListAdapter extends BaseListAdapter<RelationGroup, GroupListAdapter.GHolder> {


    public interface OnDeleteClickListener {
        void OnDeleteClick(View button, @NotNull RelationGroup group, int position);
    }

    OnDeleteClickListener onDeleteClickListener;

    public void setOnDeleteClickListener(OnDeleteClickListener onDeleteClickListener) {
        this.onDeleteClickListener = onDeleteClickListener;
    }

    public GroupListAdapter(Context mContext, List<RelationGroup> mBeans) {
        super(mContext, mBeans);
    }

    @Override
    protected int getLayoutId(int viewType) {
        return R.layout.activity_group_editor_list_item;
    }

    @Override
    public GHolder createViewHolder(View v, int viewType) {
        return new GHolder(v);
    }


    @Override
    protected void bindHolder(@NonNull GHolder holder, @Nullable RelationGroup data, int position) {
        if (data != null) {
            holder.name.setText(data.getGroupName());
            if (onDeleteClickListener != null) {
                holder.delete.setOnClickListener(view -> onDeleteClickListener.OnDeleteClick(view, data, position));
            }
        }
    }


    static class GHolder extends BaseViewHolder {
        @BindView(R.id.delete)
        View delete;
        @BindView(R.id.name)
        TextView name;

        public GHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}

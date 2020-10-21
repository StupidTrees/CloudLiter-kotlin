package com.stupidtree.hichat.ui.chat;


import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.stupidtree.hichat.R;
import com.stupidtree.hichat.data.model.Yunmoji;
import com.stupidtree.hichat.ui.base.BaseListAdapter;
import com.stupidtree.hichat.ui.base.BaseViewHolder;

import java.util.List;

import butterknife.BindView;

/**
 * 表情列表的适配器
 */
class YunmojiListAdapter extends BaseListAdapter<Yunmoji, YunmojiListAdapter.YunmojiItemHolder> {


    public YunmojiListAdapter(Context mContext, List<Yunmoji> mBeans) {
        super(mContext, mBeans);
    }

    @Override
    protected int getLayoutId(int viewType) {
        return R.layout.activity_chat_yunmoji_item;
    }

    @Override
    public YunmojiItemHolder createViewHolder(View v, int viewType) {
        return new YunmojiItemHolder(v);
    }

    @Override
    protected void bindHolder(@NonNull YunmojiItemHolder holder, @Nullable Yunmoji data, int position) {
        Yunmoji yunmoji = mBeans.get(position);
        holder.image.setImageResource(yunmoji.getImageID());
        //表示当这项的图片点击时调用onItemClickListener
        if (mOnItemClickListener != null) {
            holder.image.setOnClickListener(view -> mOnItemClickListener.onItemClick(data, view, position));
        }
    }

    static class YunmojiItemHolder extends BaseViewHolder {
        @BindView(R.id.image)
        ImageView image;

        public YunmojiItemHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}

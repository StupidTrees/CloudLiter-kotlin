package com.stupidtree.cloudliter.ui.chat.detail;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.stupidtree.cloudliter.R;
import com.stupidtree.cloudliter.data.model.ChatMessage;
import com.stupidtree.cloudliter.ui.base.BaseListAdapter;
import com.stupidtree.cloudliter.ui.base.BaseViewHolder;
import com.stupidtree.cloudliter.ui.widgets.TransparentBottomSheetDialog;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.List;

import butterknife.BindView;

/**
 * 圆角的文本框底部弹窗
 */
public class PopUpTextMessageDetail extends TransparentBottomSheetDialog {
    /**
     * View绑定区
     */

    @BindView(R.id.list)
    RecyclerView list;

    @BindView(R.id.emotion)
    TextView emotion;

    /**
     * 适配器区
     */
    LAdapter listAdapter;

    /**
     * 不得已放在UI里的数据
     */
    @NotNull
    ChatMessage chatMessage;




    public PopUpTextMessageDetail setChatMessage(@NotNull ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
        return this;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_chat_popup_text_message_detail;
    }

    @Override
    public void onStart() {
        super.onStart();
        listAdapter.notifyDataSetChanged();
    }

    @Override
    protected void initViews(View v) {
        listAdapter = new LAdapter(getContext(), chatMessage.getExtraAsSegmentation());
        list.setAdapter(listAdapter);
        list.setLayoutManager(new LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false));
        emotion.setText(new DecimalFormat("#.###").format(chatMessage.getEmotion()));
    }


    static class LAdapter extends BaseListAdapter<String, LAdapter.LHolder> {


        public LAdapter(Context mContext, List<String> mBeans) {
            super(mContext, mBeans);
        }

        @Override
        protected int getLayoutId(int viewType) {
            return R.layout.activity_chat_popup_text_message_detail_segmentation_item;
        }

        @Override
        public LHolder createViewHolder(View v, int viewType) {
            return new LHolder(v);
        }

        @Override
        protected void bindHolder(@NonNull LHolder holder, @Nullable String data, int position) {
            if (data != null) {
                holder.text.setText(data);
            }
            holder.item.setOnClickListener(view -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(data, view, position);
                }
            });
        }


        static class LHolder extends BaseViewHolder {
            @BindView(R.id.text)
            TextView text;
            @BindView(R.id.item)
            ViewGroup item;

            public LHolder(@NonNull View itemView) {
                super(itemView);
            }
        }

    }
}

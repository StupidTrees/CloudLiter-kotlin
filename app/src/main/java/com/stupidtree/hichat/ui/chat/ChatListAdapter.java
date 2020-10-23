package com.stupidtree.hichat.ui.chat;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.stupidtree.hichat.R;
import com.stupidtree.hichat.data.model.ChatMessage;
import com.stupidtree.hichat.ui.base.BaseListAdapter;
import com.stupidtree.hichat.ui.base.BaseViewHolder;
import com.stupidtree.hichat.ui.widgets.EmoticonsTextView;
import com.stupidtree.hichat.utils.ActivityUtils;
import com.stupidtree.hichat.utils.ImageUtils;
import com.stupidtree.hichat.utils.TextUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;

/**
 * 聊天列表的适配器
 */
class ChatListAdapter extends BaseListAdapter<ChatMessage, ChatListAdapter.CHolder> {
    private static final int TYPE_MINE = 287;
    private static final int TYPE_FRIEND = 509;
    private static final int TYPE_MINE_IMAGE = 944;
    private static final int TYPE_FRIEND_IMAGE = 598;
    private static final int TYPE_TIME = 774;

    @NonNull
    ChatActivity chatActivity;

    public ChatListAdapter(@NonNull ChatActivity mContext, List<ChatMessage> mBeans) {
        super(mContext, mBeans);
        this.chatActivity = mContext;
    }

    @Override
    protected int getLayoutId(int viewType) {
        switch (viewType) {
            case TYPE_MINE:
                return R.layout.activity_chat_message_text_mine;
            case TYPE_FRIEND:
                return R.layout.activity_chat_message_text_friend;
            case TYPE_FRIEND_IMAGE:
                return R.layout.activity_chat_message_image_friend;
            case TYPE_MINE_IMAGE:
                return R.layout.activity_chat_message_image_mine;
            default:
                return R.layout.activity_chat_message_time;
        }
    }


    @Override
    public int getItemViewType(int position) {
        ChatMessage cm = mBeans.get(position);
        if (cm != null) {
            if (cm.isTimeStamp()) {
                return TYPE_TIME;
            } else if (Objects.equals(cm.getToId(), chatActivity.getViewModel().getMyId())) {
                return cm.getType() == ChatMessage.TYPE.IMG ? TYPE_FRIEND_IMAGE : TYPE_FRIEND;
            } else {
                return cm.getType() == ChatMessage.TYPE.IMG ? TYPE_MINE_IMAGE : TYPE_MINE;
            }
        }
        return TYPE_TIME;
    }


    @Override
    public CHolder createViewHolder(View v, int viewType) {
        return new CHolder(v, viewType);
    }

    @Override
    protected void bindHolder(@NonNull CHolder holder, @Nullable ChatMessage data, int position) {
        if (data != null) {
            if (holder.viewType == TYPE_TIME && holder.content != null) {
                holder.content.setText(TextUtils.getChatTimeText(mContext, data.getCreatedTime()));
            } else if (holder.avatar != null) {
                if (holder.viewType == TYPE_MINE || holder.viewType == TYPE_MINE_IMAGE) {
                    ImageUtils.loadLocalAvatarInto(mContext, chatActivity.getViewModel().getMyAvatar(), holder.avatar);
                } else {
                    ImageUtils.loadAvatarInto(mContext, chatActivity.getViewModel().getFriendAvatar(), holder.avatar);
                }
                if (holder.progress != null) {
                    if (data.isProgressing()) {
                        holder.progress.setVisibility(View.VISIBLE);
                    } else {
                        holder.progress.setVisibility(View.GONE);
                    }
                }
                holder.bindSensitiveAndEmotion(data);
                holder.avatar.setOnClickListener(view -> {
                    ActivityUtils.startProfileActivity(mContext, data.getFromId());
                });

                if (holder.image != null && holder.progress != null) {
                    if (holder.progress.getVisibility() != View.VISIBLE) {
                        ImageUtils.loadChatMessageInto(mContext, data.getContent(), holder.image);
                    } else {
                        //Glide.with(getThis()).load(data.getContent()).into(holder.image);
                        holder.image.setImageResource(R.drawable.place_holder_loading);
                    }
                } else if (holder.image != null) {
                    ImageUtils.loadChatMessageInto(mContext, data.getContent(), holder.image);
                }

            }
            holder.bindClickAction(data, position);
        }
    }


    /**
     * 获取列表中所有图片的url
     * @return 结果
     */
    public List<String> getImageUrls(){
        List<String> res = new ArrayList<>();
        for(ChatMessage cm:mBeans){
            if(!cm.isTimeStamp()&&cm.getType()== ChatMessage.TYPE.IMG){
                res.add(ImageUtils.getChatMessageImageUrl(cm.getContent()));
            }
        }
        return res;
    }

    /**
     * 判断两时间戳相隔是否太远
     */
    private boolean tooFar(Timestamp t1, Timestamp t2) {
        return Math.abs(t1.getTime() - t2.getTime()) > ((long) 10 * 60 * 1000); //取10分钟
    }

    /**
     * 当消息发送成功后，通知该消息更新界面
     *
     * @param list        recyclerview
     * @param sentMessage 已发送消息实体
     */
    public void messageSent(RecyclerView list, @NonNull ChatMessage sentMessage) {
        int index = -1;
        for (int i = mBeans.size() - 1; i >= 0; i--) {
            if (Objects.equals(mBeans.get(i).getUuid(), sentMessage.getUuid())) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            mBeans.set(index, sentMessage);
            CHolder holder = (CHolder) list.findViewHolderForAdapterPosition(index);
            if (holder != null) {
                holder.hideProgress();
                holder.bindSensitiveAndEmotion(sentMessage);
                holder.bindClickAction(sentMessage,index);
                if (sentMessage.getType() == ChatMessage.TYPE.IMG) {
                    holder.updateImage(sentMessage);
                }
            }
        }

    }

    /**
     * 清空列表
     */
    public void clear() {
        mBeans.clear();
        notifyDataSetChanged();
    }

    @Override
    public void notifyItemsAppended(List<ChatMessage> newL) {
        //注意要取反
        Collections.reverse(newL);
        if (mBeans.size() > 0 && newL.size() > 0) {
            ChatMessage last = mBeans.get(mBeans.size() - 1);
            if (tooFar(last.getCreatedTime(), newL.get(0).getCreatedTime())) {
                super.notifyItemAppended(ChatMessage.getTimeStampHolderInstance(newL.get(0).getCreatedTime()));
            }
        }
        super.notifyItemsAppended(newL);

    }

    @Override
    public void notifyItemsPushHead(List<ChatMessage> newL) {
        Collections.reverse(newL);//取反
        if (mBeans.size() > 0 && newL.size() > 0) {
            ChatMessage top = mBeans.get(0);
            ChatMessage newBottom = newL.get(newL.size() - 1);
            if (tooFar(top.getCreatedTime(), newBottom.getCreatedTime())) {
                super.notifyItemPushHead(ChatMessage.getTimeStampHolderInstance(top.getCreatedTime()));
            } else if (top.isTimeStamp()) {
                super.notifyItemRemoveFromHead();
            }
        }
        if (newL.size() > 0) {
            newL.add(0, ChatMessage.getTimeStampHolderInstance(newL.get(0).getCreatedTime()));
        }
        super.notifyItemsPushHead(newL);
    }

    /**
     * 为了在时间跨度太大的两项间插入时间戳显示
     *
     * @param newL             新的数据List
     * @param notifyNormalItem 对于那些位置不变的项目，是否原地刷新
     */
    @Override
    public void notifyItemChangedSmooth(List<ChatMessage> newL, boolean notifyNormalItem) {
        List<ChatMessage> toAdd = new LinkedList<>();
        if (newL.size() == 1) {
            toAdd.addAll(newL);
        }
        for (int i = 1; i < newL.size(); i++) {
            ChatMessage last = newL.get(i - 1);
            ChatMessage thi = newL.get(i);
            toAdd.add(0, last);
            if (tooFar(last.getCreatedTime(), thi.getCreatedTime())) {
                toAdd.add(0, ChatMessage.getTimeStampHolderInstance(thi.getCreatedTime()));
            }
            if (i == newL.size() - 1) {
                toAdd.add(0, thi);
            }
        }
        if (toAdd.size() > 0) {
            toAdd.add(0, ChatMessage.getTimeStampHolderInstance(toAdd.get(0).getCreatedTime()));
        }
        super.notifyItemChangedSmooth(toAdd, notifyNormalItem);
    }

    class CHolder extends BaseViewHolder {
        int viewType;
        @BindView(R.id.content)
        EmoticonsTextView content;
        @BindView(R.id.avatar)
        @Nullable
        ImageView avatar;
        @BindView(R.id.bubble)
        @Nullable
        View bubble;
        @BindView(R.id.progress)
        @Nullable
        View progress;
        @BindView(R.id.see)
        @Nullable
        ImageView see;//点击查看敏感消息
        @BindView(R.id.emotion)
        @Nullable
        ImageView emotion;
        @BindView(R.id.image)
        @Nullable
        ImageView image;//图片
        @BindView(R.id.image_sensitive)
        @Nullable
        ViewGroup imageSensitivePlaceHolder;
        boolean isSensitiveExpanded = false;

        //隐藏加载圈圈
        public void hideProgress() {
            if (progress != null) {
                progress.setVisibility(View.GONE);
            }
        }

        //切换敏感消息查看模式
        private void switchSensitiveModeText(@NonNull ChatMessage data) {
            isSensitiveExpanded = !isSensitiveExpanded;
            if (see == null) return;
            if (isSensitiveExpanded) {
                content.setText(data.getContent());
                see.setImageResource(R.drawable.ic_baseline_visibility_off_24);
                if (data.getType() == ChatMessage.TYPE.IMG && image != null && imageSensitivePlaceHolder != null) {
                    image.setVisibility(View.VISIBLE);
                    imageSensitivePlaceHolder.setVisibility(View.GONE);
                }
            } else if (data.isSensitive()) {
                see.setImageResource(R.drawable.ic_baseline_visibility_24);
                content.setText(R.string.hint_sensitive_message);
                if (data.getType() == ChatMessage.TYPE.IMG && image != null && imageSensitivePlaceHolder != null) {
                    image.setVisibility(View.INVISIBLE);
                    imageSensitivePlaceHolder.setVisibility(View.VISIBLE);
                }
            }
        }

        //绑定敏感词状态
        public void bindSensitiveAndEmotion(@NonNull ChatMessage data) {
            isSensitiveExpanded = false;
            if (data.getType() == ChatMessage.TYPE.IMG && see != null && image != null && imageSensitivePlaceHolder != null) {
                if (data.isSensitive()) {
                    see.setVisibility(View.VISIBLE);
                    see.setOnClickListener(view -> switchSensitiveModeText(data));
                    image.setVisibility(View.INVISIBLE);
                    imageSensitivePlaceHolder.setVisibility(View.VISIBLE);
                    content.setText(R.string.hint_sensitive_message);
                } else {
                    imageSensitivePlaceHolder.setVisibility(View.GONE);
                    image.setVisibility(View.VISIBLE);
                    see.setVisibility(View.GONE);
                }
            } else if (see != null && emotion != null) {
                if (data.isSensitive()) {
                    see.setVisibility(View.VISIBLE);
                    emotion.setVisibility(View.GONE);
                    see.setImageResource(R.drawable.ic_baseline_visibility_24);
                    see.setOnClickListener(view -> switchSensitiveModeText(data));
                    content.setText(R.string.hint_sensitive_message);
                } else {
                    see.setVisibility(View.GONE);
                    content.setText(data.getContent());
                    emotion.setVisibility(View.VISIBLE);
                    float emotionValue = data.getEmotion();
                    int iconRes = R.drawable.ic_emotion_normal;
                    if (emotionValue >= 2) {
                        iconRes = R.drawable.ic_emotion_pos_3;
                    } else if (emotionValue >= 1) {
                        iconRes = R.drawable.ic_emotion_pos_2;
                    } else if (emotionValue > 0) {
                        iconRes = R.drawable.ic_emotion_pos_1;
                    } else if (emotionValue <= -2) {
                        iconRes = R.drawable.ic_emotion_neg_3;
                    } else if (emotionValue <= -1) {
                        iconRes = R.drawable.ic_emotion_neg_2;
                    } else if (emotionValue < 0) {
                        iconRes = R.drawable.ic_emotion_neg_1;
                    }
                    emotion.setImageResource(iconRes);
                }

            }

        }

        //绑定点击事件
        public void bindClickAction(@NonNull ChatMessage data, int position) {
            if (mOnItemLongClickListener != null && bubble != null) {
                bubble.setOnLongClickListener(view -> mOnItemLongClickListener.onItemLongClick(data,view, position));
            }
            if (mOnItemClickListener != null && bubble != null) {
                bubble.setOnClickListener(view -> mOnItemClickListener.onItemClick(data, view, position));
            }
        }

        public void updateImage(@NonNull ChatMessage data) {
            if (image != null) {
                ImageUtils.loadChatMessageInto(mContext, data.getContent(), image);
            }
        }

        public CHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;
        }
    }
}

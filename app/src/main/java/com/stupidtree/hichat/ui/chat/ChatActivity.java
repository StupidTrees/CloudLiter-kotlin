package com.stupidtree.hichat.ui.chat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.stupidtree.hichat.R;
import com.stupidtree.hichat.data.model.ChatMessage;
import com.stupidtree.hichat.data.model.Conversation;
import com.stupidtree.hichat.data.model.Yunmoji;
import com.stupidtree.hichat.ui.base.BaseActivity;
import com.stupidtree.hichat.ui.base.BaseListAdapter;
import com.stupidtree.hichat.ui.base.BaseViewHolder;
import com.stupidtree.hichat.ui.base.DataState;
import com.stupidtree.hichat.ui.chat.detail.PopUpTextMessageDetail;
import com.stupidtree.hichat.ui.widgets.EmoticonsEditText;
import com.stupidtree.hichat.ui.widgets.EmoticonsTextView;
import com.stupidtree.hichat.utils.ActivityUtils;
import com.stupidtree.hichat.utils.AnimationUtils;
import com.stupidtree.hichat.utils.FileProviderUtils;
import com.stupidtree.hichat.utils.GalleryPicker;
import com.stupidtree.hichat.utils.ImageUtils;
import com.stupidtree.hichat.utils.TextUtils;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;

import static com.stupidtree.hichat.ui.myprofile.MyProfileActivity.RC_CHOOSE_PHOTO;

/**
 * 对话窗口
 */
public class ChatActivity extends BaseActivity<ChatViewModel> {
    /**
     * View绑定区
     */

    @BindView(R.id.back)
    View back;
    @BindView(R.id.menu)
    View menu;
    @BindView(R.id.input)
    EmoticonsEditText inputEditText;
    @BindView(R.id.title)
    TextView titleText;
    @BindView(R.id.list)
    RecyclerView list;
    @BindView(R.id.send)
    View send;
    @BindView(R.id.state)
    TextView stateText;
    @BindView(R.id.state_icon)
    ImageView stateIcon;
    @BindView(R.id.state_bar)
    ViewGroup stateBar;
    @BindView(R.id.refresh)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.add)
    View add;
    @BindView(R.id.image)
    View imageButton;
    @BindView(R.id.expand)
    ExpandableLayout expandableLayout;

    @BindView(R.id.yunmoji_list)
    RecyclerView yunmojiList; //表情列表


    /**
     * 适配器
     */
    ChatListAdapter listAdapter;
    YunmojiListAdapter yunmojiListAdapter;//表情列表适配器


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true, true, false);

    }


    @Override
    protected void onStart() {
        super.onStart();
        viewModel.bindService(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getExtras() != null && intent.getExtras().getSerializable("conversation") != null) {
            Conversation conversation = (Conversation) intent.getExtras().getSerializable("conversation");
            Log.e("变更Intent", String.valueOf(conversation));
            if (conversation != null) {
                if (!Objects.equals(viewModel.getConversationId(), conversation.getId())) {
                    viewModel.setConversation(conversation);
                    listAdapter.clear();
                    viewModel.fetchHistoryData(); //变更对话时，重新加载聊天记录
                }
                viewModel.setConversation(conversation);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent().getExtras() != null && getIntent().getExtras().getSerializable("conversation") != null) {
            Conversation conversation = (Conversation) getIntent().getExtras().getSerializable("conversation");
            if (conversation != null) {
                if (viewModel.getConversationId() == null) {
                    viewModel.setConversation(conversation);
                    viewModel.fetchHistoryData(); //初次进入时，重新加载聊天记录
                }
            }
        }

        viewModel.getIntoConversation(this);
        viewModel.fetchNewData();
        viewModel.markAllRead(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("ChatActivity", "onStop");
        viewModel.leftConversation(this);
        viewModel.unbindService(this);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_chat;
    }

    @Override
    protected Class<ChatViewModel> getViewModelClass() {
        return ChatViewModel.class;
    }

    @Override
    protected void initViews() {
        setUpToolbar();
        setUpMessageList();
        setUpYunmojiList();
        setUpButtons();
        viewModel.getConversation().observe(this, this::setConversationViews);
        viewModel.getListData().observe(this, listDataState -> {
            refreshLayout.setRefreshing(false);
            //  Log.e("ChatActivity列表变动", String.valueOf(listDataState));
            if (listDataState.getState() == DataState.STATE.SUCCESS) {
                if (listDataState.getListAction() == DataState.LIST_ACTION.APPEND) {
                    if (listDataState.getData().size() == 1) {
                        viewModel.markRead(this, listDataState.getData().get(0));
                    }
                    listAdapter.notifyItemsAppended(listDataState.getData());
                    if (listAdapter.getItemCount() > 0) {
                        list.smoothScrollToPosition(listAdapter.getItemCount() - 1);
                    }
                } else if (listDataState.getListAction() == DataState.LIST_ACTION.PUSH_HEAD) {
                    listAdapter.notifyItemsPushHead(listDataState.getData());
                    if (listDataState.getData().size() > 0) {
                        list.smoothScrollBy(0, -150);
                    }
                } else {
                    listAdapter.notifyItemChangedSmooth(listDataState.getData(), false);
                    if (listAdapter.getItemCount() > 0) {
                        list.smoothScrollToPosition(listAdapter.getItemCount() - 1);
                    }
                }
            }
        });
        viewModel.getFriendStateLiveData().observe(this, friendStateDataState -> {
            if (friendStateDataState.getState() == DataState.STATE.SUCCESS) {
                switch (friendStateDataState.getData().getState()) {
                    case ONLINE:
                        stateText.setText(R.string.online);
                        stateText.setTextColor(getColorPrimary());
                        stateIcon.setImageResource(R.drawable.element_round_primary);
                        stateBar.setBackgroundResource(R.drawable.element_rounded_bar_primary);
                        break;
                    case YOU:
                        stateText.setText(R.string.with_you);
                        stateText.setTextColor(getColorPrimary());
                        stateIcon.setImageResource(R.drawable.element_round_primary);
                        stateBar.setBackgroundResource(R.drawable.element_rounded_bar_primary);
                        break;
                    case OTHER:
                        stateText.setText(R.string.with_other);
                        stateText.setTextColor(getColorPrimary());
                        stateIcon.setImageResource(R.drawable.element_round_primary);
                        stateBar.setBackgroundResource(R.drawable.element_rounded_bar_primary);
                        break;
                    case OFFLINE:
                        stateText.setText(R.string.offline);
                        stateText.setTextColor(getTextColorSecondary());
                        stateIcon.setImageResource(R.drawable.element_round_grey);
                        stateBar.setBackgroundResource(R.drawable.element_rounded_bar_grey);
                        break;
                }
            }
        });

        viewModel.getImageSentResult().observe(this, chatMessageDataState -> {
//            if (chatMessageDataState.getState() == DataState.STATE.SUCCESS) {
//                //listAdapter.notifyItemsAppended(Collections.singletonList(chatMessageDataState.getData()));
//            }
        });

        viewModel.getMessageSentState().observe(this, chatMessageDataState -> {

            if (chatMessageDataState.getState() == DataState.STATE.SUCCESS) {
                listAdapter.messageSent(list, chatMessageDataState.getData());
            }
        });


    }

    //设置toolbar
    private void setUpToolbar() {
        back.setOnClickListener(view -> onBackPressed());
        menu.setOnClickListener(view -> ActivityUtils.startConversationActivity(getThis(), viewModel.getFriendId()));
    }


    //初始化聊天列表
    @SuppressLint("ClickableViewAccessibility")
    private void setUpMessageList() {

        listAdapter = new ChatListAdapter(this, new LinkedList<>());
        list.setAdapter(listAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);  //从底部往上堆
        list.setLayoutManager(layoutManager);
        //触摸recyclerView的监听
        list.setOnTouchListener((view, motionEvent) -> {
            //隐藏键盘
            hideSoftInput(getThis(), inputEditText);
            collapseEmotionPanel();
            return false;
        });
        refreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent);
        refreshLayout.setOnRefreshListener(() -> viewModel.loadMore());
        listAdapter.setOnItemClickListener((data, card, position) -> {
            //注意，data可能是未更新的已发送对象
            if (position < listAdapter.getBeans().size()) {
                ChatMessage cm = listAdapter.getBeans().get(position);
                if (cm.getType() == ChatMessage.TYPE.TXT && !cm.isTimeStamp()) {
                    new PopUpTextMessageDetail().setListData(cm.getExtraAsSegmentation())
                            .show(getSupportFragmentManager(), "txt");
                }
            }


        });
    }


    //初始化各种按钮
    private void setUpButtons() {
        //点击发送
        send.setOnClickListener(view -> {
            if (!TextUtils.isEmpty(inputEditText.getText().toString())) {
                viewModel.sendMessage(inputEditText.getText().toString());
                inputEditText.setText("");
            }
        });
        add.setOnClickListener(view -> {

            if (expandableLayout.isExpanded()) {
                collapseEmotionPanel();
            } else {
                hideSoftInput(getApplicationContext(), inputEditText);
                expandEmotionPanel();
            }
        });
        imageButton.setOnClickListener(view -> GalleryPicker.choosePhoto(getThis(), false));
    }


    //初始化表情列表
    private void setUpYunmojiList() {
        ArrayList<Yunmoji> ymList = new ArrayList<>();
        for (int i = 1; i < 21; i++) {
            Yunmoji item = new Yunmoji(getResources().getIdentifier(String.format(Locale.getDefault(), "yunmoji_y%03d", i), "drawable", getPackageName()));
            ymList.add(item);
        }
        yunmojiList.setLayoutManager(new GridLayoutManager(this, 6));
        yunmojiListAdapter = new YunmojiListAdapter(this, ymList);
        yunmojiList.setAdapter(yunmojiListAdapter);
        BaseListAdapter.OnItemClickListener<Yunmoji> yunmojiOnItemClickListener = (data, card, position) -> inputEditText.append(data.getLastname(position));
        yunmojiListAdapter.setOnItemClickListener(yunmojiOnItemClickListener);
    }


    private void setConversationViews(@NonNull Conversation conversation) {
        if (TextUtils.isEmpty(conversation.getFriendRemark())) {
            titleText.setText(conversation.getFriendNickname());
        } else {
            titleText.setText(conversation.getFriendRemark());
        }
    }

    //收起表情栏
    private void collapseEmotionPanel() {
        if (expandableLayout.isExpanded()) {
            expandableLayout.collapse();
            AnimationUtils.rotateTo(add, false);
        }
    }

    //展开表情栏
    private void expandEmotionPanel() {
        if (!expandableLayout.isExpanded()) {
            expandableLayout.expand();
            AnimationUtils.rotateTo(add, true);
        }

    }


    //隐藏键盘
    public static void hideSoftInput(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    /**
     * 聊天列表的适配器
     */
    class ChatListAdapter extends BaseListAdapter<ChatMessage, ChatListAdapter.CHolder> {
        private static final int TYPE_MINE = 287;
        private static final int TYPE_FRIEND = 509;
        private static final int TYPE_MINE_IMAGE = 944;
        private static final int TYPE_FRIEND_IMAGE = 598;
        private static final int TYPE_TIME = 774;


        public ChatListAdapter(Context mContext, List<ChatMessage> mBeans) {
            super(mContext, mBeans);
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
                } else if (Objects.equals(cm.getToId(), viewModel.getMyId())) {
                    return cm.getType() == ChatMessage.TYPE.IMG ? TYPE_FRIEND_IMAGE : TYPE_FRIEND;
                } else {
                    return cm.getType() == ChatMessage.TYPE.IMG ? TYPE_MINE_IMAGE : TYPE_MINE;
                }
            }
            return TYPE_TIME;

        }


        public void messageSent(RecyclerView list, @NonNull ChatMessage sentMessage) {
            Log.e("消息发送，界面更新", String.valueOf(sentMessage));
            int index = -1;
            for (int i = mBeans.size() - 1; i >= 0; i--) {
                if (Objects.equals(mBeans.get(i).getUuid(), sentMessage.getUuid())) {
                    index = i;
                    break;
                }
            }
            Log.e("index", String.valueOf(index));
            Log.e("size", String.valueOf(mBeans.size()));
            if (index >= 0) {
                mBeans.set(index, sentMessage);
                CHolder holder = (CHolder) list.findViewHolderForAdapterPosition(index);
                if (holder != null) {
                    holder.hideProgress();
                    holder.bindSensitiveAndEmotion(sentMessage);
                    if (sentMessage.getType() == ChatMessage.TYPE.IMG) {
                        holder.updateImage(sentMessage);
                    }
                }
            }

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
                        ImageUtils.loadLocalAvatarInto(mContext, viewModel.getMyAvatar(), holder.avatar);
                    } else {
                        ImageUtils.loadAvatarInto(mContext, viewModel.getFriendAvatar(), holder.avatar);
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
                            ImageUtils.loadChatMessageInto(getThis(), data.getContent(), holder.image);
                        } else {
                            //Glide.with(getThis()).load(data.getContent()).into(holder.image);
                            holder.image.setImageResource(R.drawable.place_holder_loading);
                        }
                    } else if (holder.image != null) {
                        ImageUtils.loadChatMessageInto(getThis(), data.getContent(), holder.image);
                    }

                }
                if (mOnItemClickListener != null && holder.bubble != null) {
                    holder.bubble.setOnClickListener(view -> mOnItemClickListener.onItemClick(data, view, position));
                }

            }
        }

        /**
         * 判断两时间戳相隔是否太远
         */
        private boolean tooFar(Timestamp t1, Timestamp t2) {
            return Math.abs(t1.getTime() - t2.getTime()) > ((long) 10 * 60 * 1000); //取10分钟
        }


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

            public void updateImage(@NonNull ChatMessage data) {
                if (image != null) {
                    ImageUtils.loadChatMessageInto(getThis(), data.getContent(), image);
                }
            }

            public CHolder(@NonNull View itemView, int viewType) {
                super(itemView);
                this.viewType = viewType;
            }
        }
    }


    /**
     * 表情列表的适配器
     */
    static class YunmojiListAdapter extends BaseListAdapter<Yunmoji, YunmojiListAdapter.YunmojiItemHolder> {


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


    /**
     * 当用户通过系统相册选择图片返回时，将调用本函数
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == RC_CHOOSE_PHOTO) {//选择图片返回
            if (null == data) {
                Toast.makeText(this, R.string.no_image_selected, Toast.LENGTH_SHORT).show();
                return;
            }
            Uri uri = data.getData();
            if (null == uri) { //如果单个Uri为空，则可能是1:多个数据 2:没有数据
                Toast.makeText(this, R.string.no_image_selected, Toast.LENGTH_SHORT).show();
                return;
            }
            String filePath = FileProviderUtils.getFilePathByUri(getThis(), uri);
            viewModel.sendImageMessage(filePath);
        }
    }

}

package com.stupidtree.hichat.ui.chat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.stupidtree.hichat.R;
import com.stupidtree.hichat.data.model.ChatMessage;
import com.stupidtree.hichat.data.model.Yunmoji;
import com.stupidtree.hichat.ui.base.BaseActivity;
import com.stupidtree.hichat.ui.base.BaseListAdapter;
import com.stupidtree.hichat.ui.base.BaseViewHolder;
import com.stupidtree.hichat.ui.base.DataState;
import com.stupidtree.hichat.ui.widgets.EmoticonsEditText;
import com.stupidtree.hichat.ui.widgets.EmoticonsTextView;
import com.stupidtree.hichat.utils.ActivityUtils;
import com.stupidtree.hichat.utils.AnimationUtils;
import com.stupidtree.hichat.utils.ImageUtils;
import com.stupidtree.hichat.utils.TextUtils;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;

/**
 * 对话窗口
 */
public class ChatActivity extends BaseActivity<ChatViewModel> {
    /**
     * View绑定区
     */
    @BindView(R.id.toolbar)
    Toolbar toolbar;
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
    @BindView(R.id.refresh)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.add)
    View add;
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
        if (getIntent().getStringExtra("friendId") != null) {
            viewModel.setFriendId(getIntent().getStringExtra("friendId"));
            viewModel.startFetchingConversation(getIntent().getStringExtra("friendId"));
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        viewModel.bindService(this);
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
        //给viewModel设置监听
        viewModel.getConversationLiveData().observe(this, conversationDataState -> {
            if (conversationDataState.getState() == DataState.STATE.SUCCESS) {
                //获取到对话，才刷新
                if (TextUtils.isEmpty(conversationDataState.getData().getFriendRemark())) {
                    titleText.setText(conversationDataState.getData().getFriendNickname());
                } else {
                    titleText.setText(conversationDataState.getData().getFriendRemark());
                }

                viewModel.markAllRead(this);
                viewModel.getIntoConversation(this);
                viewModel.fetchHistoryData();
            }
        });
        viewModel.getListData().observe(this, listDataState -> {
            refreshLayout.setRefreshing(false);
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
                        stateIcon.setImageResource(R.drawable.element_round_primary);
                        break;
                    case YOU:
                        stateText.setText(R.string.with_you);
                        stateIcon.setImageResource(R.drawable.element_round_primary);
                        break;
                    case OTHER:
                        stateText.setText(R.string.with_other);
                        stateIcon.setImageResource(R.drawable.element_round_primary);
                        break;
                    case OFFLINE:
                        stateText.setText(R.string.offline);
                        stateIcon.setImageResource(R.drawable.element_round_grey);
                        break;
                }
            }
        });

        viewModel.getMessageSentState().observe(this, chatMessageDataState -> {

            if(chatMessageDataState.getState()== DataState.STATE.SUCCESS){
                listAdapter.messageSent(list,chatMessageDataState.getData());
            }
        });


    }

    //设置toolbar
    private void setUpToolbar(){
        toolbar.inflateMenu(R.menu.toolbar_chat_menu);
        setToolbarActionBack(toolbar);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_action_make_friend && viewModel.getFriendId() != null) {
                ActivityUtils.startConversationActivity(getThis(), viewModel.getFriendId());
            }
            return true;
        });
    }

    //初始化聊天列表
    @SuppressLint("ClickableViewAccessibility")
    private void setUpMessageList(){

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
    }

    //初始化各种按钮
    private void setUpButtons(){
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
                hideSoftInput(getApplicationContext(),inputEditText);
                expandEmotionPanel();
            }
        });
    }

    private List<Yunmoji> ymList = new ArrayList<>();

    public void initYunmoji() {
       ;
        for(int i=1;i<21;i++){
            Yunmoji item = new Yunmoji(getResources().getIdentifier( String.format(Locale.getDefault(),"yunmoji_y%03d",i) , "drawable", getPackageName()));
            ymList.add(item);
        }
    }

    //初始化表情列表
    private void setUpYunmojiList(){
        yunmojiList.setLayoutManager(new GridLayoutManager(this,6));
        //1：初始化表情资源
        initYunmoji();
        //2：初始化适配器
        yunmojiListAdapter = new YunmojiListAdapter(this, ymList);
        //3：设置适配器
        yunmojiList.setAdapter(yunmojiListAdapter);
        //4：设置点击监听
        BaseListAdapter.OnItemClickListener<Yunmoji> yunmojiOnItemClickListener = (data, card, position) -> inputEditText.append(data.getLastname(position));
        yunmojiListAdapter.setOnItemClickListener(yunmojiOnItemClickListener);
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


    @Override
    protected void onStop() {
        super.onStop();
        viewModel.leftConversation(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("ChatActivity", "destroy");
        viewModel.unbindService(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_chat_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    /**
     * 聊天列表的适配器
     */
    class ChatListAdapter extends BaseListAdapter<ChatMessage, ChatListAdapter.CHolder> {
        private static final int TYPE_MINE = 287;
        private static final int TYPE_FRIEND = 509;
        private static final int TYPE_TIME = 774;

        public ChatListAdapter(Context mContext, List<ChatMessage> mBeans) {
            super(mContext, mBeans);
        }

        @Override
        protected int getLayoutId(int viewType) {
            if (viewType == TYPE_MINE) return R.layout.activity_chat_message_text_mine;
            else if (viewType == TYPE_FRIEND) return R.layout.activity_chat_message_text_friend;
            else return R.layout.activity_chat_message_time;
        }

        @Override
        public int getItemViewType(int position) {

            ChatMessage cm = mBeans.get(position);
            if (cm != null && Objects.equals(cm.getId(), "TIME")) {
                return TYPE_TIME;
            } else if (cm != null && Objects.equals(cm.getToId(), viewModel.getMyId())) {
                return TYPE_FRIEND;
            } else {
                return TYPE_MINE;
            }
        }

        public void messageSent(RecyclerView list,@NonNull ChatMessage sentMessage){
            int index = -1;
            for(int i=mBeans.size()-1;i>=0;i--){
                if(Objects.equals(mBeans.get(i).getUuid(),sentMessage.getUuid())){
                    index = i;
                    break;
                }
            }
            if(index>=0){
                mBeans.set(index,sentMessage);
                CHolder holder = (CHolder) list.findViewHolderForAdapterPosition(index);
                if(holder!=null){
                    holder.hideProgress();
                    holder.bindSensitive(sentMessage);
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
                if (holder.viewType == TYPE_TIME) {
                    holder.content.setText(TextUtils.getChatTimeText(mContext, data.getCreatedTime()));
                } else {

                    if (holder.viewType == TYPE_MINE) {
                        ImageUtils.loadLocalAvatarInto(mContext, viewModel.getMyAvatar(), holder.avatar);
                    } else {
                        ImageUtils.loadAvatarInto(mContext, viewModel.getFriendAvatar(), holder.avatar);
                    }
                    if(holder.progress!=null){
                        if(data.isProgressing()){
                            holder.progress.setVisibility(View.VISIBLE);
                        }else{
                            holder.progress.setVisibility(View.GONE);
                        }
                    }
//                    holder.content.setText(data.getContent());
                    holder.bindSensitive(data);
                    holder.avatar.setOnClickListener(view -> {
                        if (holder.viewType == TYPE_FRIEND) {
                            ActivityUtils.startProfileActivity(mContext, data.getFromId());
                        } else {
                            ActivityUtils.startMyProfileActivity(mContext);
                        }

                    });

                }

            }
        }

        /**
         * 判断两时间戳相隔是否太远
         */
        private boolean tooFar(Timestamp t1, Timestamp t2) {
            return Math.abs(t1.getTime() - t2.getTime()) > ((long) 10 * 60 * 1000); //取10分钟
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
                } else if (Objects.equals(top.getId(), "TIME")) {
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

        class CHolder extends RecyclerView.ViewHolder {
            int viewType;
            EmoticonsTextView content;
            ImageView avatar;
            View bubble;
            View progress;
            ImageView see;//点击查看敏感消息
            boolean isSensitiveExpanded = false;

            //隐藏加载圈圈
            public void hideProgress(){
                if(progress!=null){
                    progress.setVisibility(View.GONE);
                }
            }

            //切换敏感消息查看模式
            public void switchSensitiveMode(@NonNull ChatMessage data){
                isSensitiveExpanded = !isSensitiveExpanded;
                if(isSensitiveExpanded){
                    content.setText(data.getContent());
                    see.setImageResource(R.drawable.ic_baseline_visibility_off_24);
                }else if(data.isSensitive()){
                    content.setText(R.string.hint_sensitive_message);
                    see.setImageResource(R.drawable.ic_baseline_visibility_24);
                }
            }

            //绑定敏感词状态
            public void bindSensitive(@NonNull ChatMessage data){
                isSensitiveExpanded = false;
                if(data.isSensitive()){
                    see.setVisibility(View.VISIBLE);
                    see.setImageResource(R.drawable.ic_baseline_visibility_24);
                    content.setText(R.string.hint_sensitive_message);
                    see.setOnClickListener(view -> switchSensitiveMode(data));
                }else{
                    see.setVisibility(View.GONE);
                    content.setText(data.getContent());
                }
            }

            public CHolder(@NonNull View itemView, int viewType) {
                super(itemView);
                this.viewType = viewType;
                content = itemView.findViewById(R.id.content);
                avatar = itemView.findViewById(R.id.avatar);
                bubble = itemView.findViewById(R.id.bubble);
                progress = itemView.findViewById(R.id.progress);
                see = itemView.findViewById(R.id.see);
            }
        }
    }


    /**
     * 表情列表的适配器
     */
    //TODO
    static class YunmojiListAdapter extends BaseListAdapter<Yunmoji, YunmojiListAdapter.YunmojiItemHolder>{


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
            if(mOnItemClickListener!=null){
                holder.image.setOnClickListener(view -> mOnItemClickListener.onItemClick(data, view, position));
            }
        }

        static class YunmojiItemHolder extends BaseViewHolder{
            @BindView(R.id.image)
            ImageView image;
            public YunmojiItemHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }
}

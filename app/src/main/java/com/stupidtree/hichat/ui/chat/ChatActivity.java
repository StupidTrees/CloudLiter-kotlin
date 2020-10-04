package com.stupidtree.hichat.ui.chat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.stupidtree.hichat.R;
import com.stupidtree.hichat.data.model.ChatMessage;
import com.stupidtree.hichat.ui.base.BaseActivity;
import com.stupidtree.hichat.ui.base.BaseListAdapter;
import com.stupidtree.hichat.ui.base.BaseViewHolder;
import com.stupidtree.hichat.ui.base.DataState;
import com.stupidtree.hichat.utils.ImageUtils;
import com.stupidtree.hichat.utils.TextUtils;

import java.util.LinkedList;
import java.util.List;
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
    EditText inputEditText;
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

    /**
     * 适配器
     */
    CAdapter listAdapter;




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


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initViews() {
        toolbar.inflateMenu(R.menu.toolbar_chat_menu);
        setToolbarActionBack(toolbar);
        //给viewModel设置监听
        viewModel.getConversationLiveData().observe(this, conversationDataState -> {
            if (conversationDataState.getState() == DataState.STATE.SUCCESS) {
                //获取到对话，才刷新
                titleText.setText(conversationDataState.getData().getFriendNickname());
                viewModel.markAllRead(this);
                viewModel.getIntoConversation(this);
                viewModel.fetchHistoryData();
            }
        });
        viewModel.getListData().observe(this, listDataState -> {
            if (listDataState.getState() == DataState.STATE.SUCCESS) {
                if (listDataState.getListAction() == DataState.LIST_ACTION.APPEND) {
                    if (listDataState.getData().size() == 1) {
                        viewModel.markRead(this, listDataState.getData().get(0));
                    }
                    listAdapter.notifyItemAppended(listDataState.getData());
                } else {
                    listAdapter.notifyItemChangedSmooth(listDataState.getData(), false);
                }
                if (listAdapter.getItemCount() > 0) {
                    list.smoothScrollToPosition(listAdapter.getItemCount() - 1);
                }
            }
        });
        viewModel.getFriendStateLiveData().observe(this, friendStateDataState -> {
            if (friendStateDataState.getState() == DataState.STATE.SUCCESS) {
                if (friendStateDataState.getData().getState() == FriendState.STATE.ONLINE) {
                    stateText.setText(R.string.online);
                    stateIcon.setImageResource(R.drawable.element_round_primary);
                } else {
                    stateText.setText(R.string.offline);
                    stateIcon.setImageResource(R.drawable.element_round_grey);
                }
            }
        });

        //初始化列表
        listAdapter = new CAdapter(this, new LinkedList<>());
        list.setAdapter(listAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);  //从底部往上堆
        list.setLayoutManager(layoutManager);

        //触摸recyclerView的监听
        list.setOnTouchListener((view, motionEvent) -> {
            //隐藏键盘
            hideSoftInput(getThis(), inputEditText);
            return false;
        });

        //点击发送
        send.setOnClickListener(view -> {
            if (!TextUtils.isEmpty(inputEditText.getText().toString())) {
                viewModel.sendMessage(inputEditText.getText().toString());
                inputEditText.setText("");
            }
        });

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


    class CAdapter extends BaseListAdapter<ChatMessage, CAdapter.CHolder> {
        private static final int TYPE_MINE = 287;
        private static final int TYPE_FRIEND = 509;

        public CAdapter(Context mContext, List<ChatMessage> mBeans) {
            super(mContext, mBeans);
        }

        @Override
        protected int getLayoutId(int viewType) {
            if (viewType == TYPE_MINE) return R.layout.activity_chat_message_text_mine;
            else return R.layout.activity_chat_message_text_friend;
        }

        @Override
        public int getItemViewType(int position) {

            ChatMessage cm = mBeans.get(position);
            if (cm != null && Objects.equals(cm.getToId(), viewModel.getMyId())) {
                return TYPE_FRIEND;
            } else {
                return TYPE_MINE;
            }
        }

        @Override
        public CHolder createViewHolder(View v, int viewType) {
            return new CHolder(v, viewType);
        }

        @Override
        protected void bindHolder(@NonNull CHolder holder, @Nullable ChatMessage data, int position) {
            if (data != null) {
                if (holder.viewType == TYPE_MINE) {
                    ImageUtils.loadLocalAvatarInto(mContext, viewModel.getMyAvatar(), holder.avatar);
                } else {
                    ImageUtils.loadAvatarInto(mContext, viewModel.getFriendAvatar(), holder.avatar);
                }

                holder.content.setText(data.getContent());
            }
        }

        class CHolder extends BaseViewHolder {
            int viewType;
            @BindView(R.id.content)
            TextView content;
            @BindView(R.id.avatar)
            ImageView avatar;
            @BindView(R.id.bubble)
            View bubble;


            public CHolder(@NonNull View itemView, int viewType) {
                super(itemView);
                this.viewType = viewType;
            }
        }
    }
}

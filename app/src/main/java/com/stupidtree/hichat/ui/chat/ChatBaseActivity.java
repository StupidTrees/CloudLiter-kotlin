package com.stupidtree.hichat.ui.chat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.stupidtree.hichat.R;
import com.stupidtree.hichat.data.model.ChatMessage;
import com.stupidtree.hichat.data.model.Conversation;
import com.stupidtree.hichat.ui.base.BaseActivityWithReceiver;
import com.stupidtree.hichat.ui.base.BaseListAdapter;
import com.stupidtree.hichat.ui.base.BaseViewHolder;
import com.stupidtree.hichat.ui.base.DataState;
import com.stupidtree.hichat.utils.ImageUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;

import static com.stupidtree.hichat.service.SocketIOClientService.ACTION_RECEIVE_MESSAGE;

public class ChatBaseActivity extends BaseActivityWithReceiver<ChatViewModel> {
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

    /**
     * 适配器
     */
    CAdapter listAdapter;


    @NonNull
    @Override
    protected BroadcastReceiver initReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e("ChatActivity:received", String.valueOf(intent));
                ChatMessage cm = new Gson().fromJson(intent.getStringExtra("message"), ChatMessage.class);
                Log.e("message", String.valueOf(cm));
                if (Objects.equals(cm.getFromId(), viewModel.getFriendId())) {
                    viewModel.receiveMessage(cm);
                }
            }
        };
    }

    @Override
    protected IntentFilter getIntentFilter() {
        IntentFilter IF = new IntentFilter();
        IF.addAction(ACTION_RECEIVE_MESSAGE);
        return IF;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true, true, false);
        if (getIntent().getExtras() != null) {
            viewModel.setConversationData((Conversation) getIntent().getExtras().getSerializable("conversation"));
        }
        viewModel.bindService(this);
        initReceiver();
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
        viewModel.getConversationMutableLiveData().observe(this, (Conversation conversation) -> titleText.setText(conversation.getFriendNickname()));
        viewModel.getListData().observe(this, listDataState -> {
            if (listDataState.getState() == DataState.STATE.SUCCESS) {
                if (listDataState.getListAction() == DataState.LIST_ACTION.APPEND) {
                    listAdapter.notifyItemAppended(listDataState.getData());
                } else {
                    listAdapter.notifyItemChangedSmooth(listDataState.getData(), false);
                }
                if (listAdapter.getItemCount() > 0) {
                    list.smoothScrollToPosition(listAdapter.getItemCount() - 1);
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
            viewModel.sendMessage(inputEditText.getText().toString());
            inputEditText.setText("");
        });

    }


    //隐藏键盘
    public static void hideSoftInput(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("ChatActivity", "onResume");
        viewModel.fetchHistoryData();
        viewModel.markAllRead();
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

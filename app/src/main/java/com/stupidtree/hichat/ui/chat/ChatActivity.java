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

import com.stupidtree.hichat.R;
import com.stupidtree.hichat.data.model.ChatMessage;
import com.stupidtree.hichat.data.model.Conversation;
import com.stupidtree.hichat.data.model.Yunmoji;
import com.stupidtree.hichat.ui.base.BaseActivity;
import com.stupidtree.hichat.ui.base.BaseListAdapter;
import com.stupidtree.hichat.ui.base.DataState;
import com.stupidtree.hichat.ui.chat.detail.PopUpImageMessageDetail;
import com.stupidtree.hichat.ui.chat.detail.PopUpTextMessageDetail;
import com.stupidtree.hichat.ui.widgets.EmoticonsEditText;
import com.stupidtree.hichat.utils.ActivityUtils;
import com.stupidtree.hichat.utils.AnimationUtils;
import com.stupidtree.hichat.utils.FileProviderUtils;
import com.stupidtree.hichat.utils.GalleryPicker;
import com.stupidtree.hichat.utils.ImageUtils;
import com.stupidtree.hichat.utils.TextUtils;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;

import static com.stupidtree.hichat.ui.myprofile.MyProfileActivity.RC_CHOOSE_PHOTO;

/**
 * 对话窗口
 */
@SuppressLint("NonConstantResourceId")
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


    /**
     * 常规操作区
     */
    @Override
    protected int getLayoutId() {
        return R.layout.activity_chat;
    }

    @Override
    protected Class<ChatViewModel> getViewModelClass() {
        return ChatViewModel.class;
    }

    @NonNull
    ChatViewModel getViewModel() {
        return viewModel;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true, true, false);
    }


    /**
     * 生命周期事件区
     */
    //启动时，绑定服务
    @Override
    protected void onStart() {
        super.onStart();
        viewModel.bindService(this);
    }


    //更新Intent时（更换聊天对象），刷新列表
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


    //每次回到页面时，声明进入对话
    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent().getExtras() != null && getIntent().getExtras().getSerializable("conversation") != null) {
            Conversation conversation = (Conversation) getIntent().getExtras().getSerializable("conversation");
            if (conversation != null) {
                if (viewModel.getConversationId() == null) {
                    viewModel.setConversation(conversation);
                    viewModel.fetchHistoryData(); //初次进入时，重新加载聊天记录
                }else{
                    viewModel.fetchNewData();//非第一次进入
                }
            }
        }
        viewModel.getIntoConversation(this);
        viewModel.markAllRead(this);
    }


    //每次页面失去焦点时，退出对话，解绑服务
    @Override
    protected void onStop() {
        super.onStop();
        Log.e("ChatActivity", "onStop");
        viewModel.leftConversation(this);
        viewModel.unbindService(this);
    }


    /**
     * 初始化View区
     */
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
        listAdapter.setOnItemLongClickListener((cm, view, position) -> {
            if (cm.getType() == ChatMessage.TYPE.TXT && !cm.isTimeStamp()) {
                new PopUpTextMessageDetail().setChatMessage(cm)
                        .show(getSupportFragmentManager(), "detail");
            } else if (cm.getType() == ChatMessage.TYPE.IMG && !cm.isTimeStamp()) {
                new PopUpImageMessageDetail().setChatMessage(cm)
                        .show(getSupportFragmentManager(), "detail");
            }
            return true;
        });

        listAdapter.setOnItemClickListener((cm, card, position) -> {
            if (cm.getType() == ChatMessage.TYPE.IMG && !cm.isTimeStamp()) {
                List<String> urls = listAdapter.getImageUrls();
                ActivityUtils.showMultipleImages(getThis(), urls, urls.indexOf(ImageUtils.getChatMessageImageUrl(cm.getContent()))
                );
                // ActivityUtils.showOneImage(getThis(), ImageUtils.getChatMessageImageUrl(cm.getContent()));
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


    /**
     * 动作区
     */

    //设置聊天信息显示
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

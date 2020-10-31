package com.stupidtree.hichat.ui.conversation;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.stupidtree.hichat.R;
import com.stupidtree.hichat.data.model.Conversation;
import com.stupidtree.hichat.ui.base.BaseActivity;
import com.stupidtree.hichat.ui.base.DataState;
import com.stupidtree.hichat.ui.widgets.WordsCloudView;
import com.stupidtree.hichat.utils.ActivityUtils;
import com.stupidtree.hichat.utils.ImageUtils;
import com.stupidtree.hichat.utils.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;

public class ConversationActivity extends BaseActivity<ConversationViewModel> {


    @BindView(R.id.avatar)
    ImageView friendAvatarImage;

    @BindView(R.id.remark)
    TextView friendRemarkText;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.user_layout)
    ViewGroup userLayout;

    @BindView(R.id.word_cloud)
    WordsCloudView wordsCloudView;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_conversation;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolbarActionBack(toolbar);
        setWindowParams(true, true, false);
    }

    @Override
    protected Class<ConversationViewModel> getViewModelClass() {
        return ConversationViewModel.class;
    }

    @Override
    protected void initViews() {
        viewModel.getConversationLiveData().observe(this, conversationDataState -> {
            if (conversationDataState.getState() == DataState.STATE.SUCCESS) {
                setUpPage(conversationDataState.getData());
            }
        });
        wordsCloudView.setData(Collections.singletonList(getString(R.string.no_word_cloud_yet)));
        viewModel.getWordCloudLiveData().observe(this, hashMapDataState -> {
            if (hashMapDataState.getState() == DataState.STATE.SUCCESS) {
                ArrayList<String> tag = new ArrayList<>();
                for (Map.Entry<String, Float> wordData : hashMapDataState.getData().entrySet()) {
                    tag.add(wordData.getKey());
                }
                wordsCloudView.setData(tag);
            }

        });
        userLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = getIntent().getStringExtra("friendId");
                if (!TextUtils.isEmpty(id)) {
                    ActivityUtils.startProfileActivity(getThis(), id);
                }

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent().hasExtra("friendId")) {
            viewModel.startRefresh(getIntent().getStringExtra("friendId"));
        }
    }

    private void setUpPage(Conversation conversation) {
        ImageUtils.loadAvatarNoCacheInto(this, conversation.getFriendAvatar(), friendAvatarImage);
        if (TextUtils.isEmpty(conversation.getFriendRemark())) {
            friendRemarkText.setText(conversation.getFriendNickname());
        } else {
            friendRemarkText.setText(conversation.getFriendRemark());
        }
    }

}
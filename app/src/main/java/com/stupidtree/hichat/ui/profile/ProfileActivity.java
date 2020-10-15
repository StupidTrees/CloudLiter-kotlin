package com.stupidtree.hichat.ui.profile;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.Observer;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.stupidtree.hichat.R;
import com.stupidtree.hichat.data.model.UserLocal;
import com.stupidtree.hichat.data.model.UserProfile;
import com.stupidtree.hichat.data.model.UserRelation;
import com.stupidtree.hichat.ui.base.BaseActivity;
import com.stupidtree.hichat.ui.base.DataState;
import com.stupidtree.hichat.ui.widgets.PopUpEditText;
import com.stupidtree.hichat.ui.widgets.PopUpText;
import com.stupidtree.hichat.utils.ActivityUtils;
import com.stupidtree.hichat.utils.ColorUtils;
import com.stupidtree.hichat.utils.ImageUtils;
import com.stupidtree.hichat.utils.TextUtils;

import java.util.Objects;

import butterknife.BindView;


/**
 * 其他用户（好友、搜索结果等）的资料页面Activity
 */
public class ProfileActivity extends BaseActivity<ProfileViewModel> {

    /**
     * View绑定区
     */
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.text_username)
    TextView usernameTextView;

    @BindView(R.id.text_nickname)
    TextView nicknameTextView;

    @BindView(R.id.text_signature)
    TextView signatureTextView;

    @BindView(R.id.icon_gender)
    ImageView genderIcon;

    @BindView(R.id.fab)
    ExtendedFloatingActionButton button;

    @BindView(R.id.avatar)
    ImageView avatarImageView;

    @BindView(R.id.icon_color)
    CardView colorIcon;
    @BindView(R.id.icon_color_inner)
    CardView colorIconInner;

    @BindView(R.id.remark_layout)
    ViewGroup remarkLayout;

    @BindView(R.id.delete_layout)
    ViewGroup deleteLayout;

    @BindView(R.id.remark)
    TextView remarkText;

    @BindView(R.id.relation_card)
    ViewGroup relationCard;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setWindowParams(true, true, false);
        super.onCreate(savedInstanceState);
        setToolbarActionBack(toolbar);
    }

    @Override
    protected Class<ProfileViewModel> getViewModelClass() {
        return ProfileViewModel.class;
    }

    @Override
    protected void initViews() {
        //为ViewModel中的各种数据设置监听
        viewModel.getUserProfileLiveData().observe(this, userProfileDataState -> {
            if (userProfileDataState.getState() == DataState.STATE.SUCCESS) {
                //状态为成功，设置ui
                setProfileView(userProfileDataState.getData());
            } else {
                //状态为失败，弹出错误
                Toast.makeText(getThis(), "获取出错", Toast.LENGTH_SHORT).show();
            }

        });
        viewModel.getMakeFriendsResult().observe(this, booleanDataState -> {
            if (booleanDataState.getState() == DataState.STATE.SUCCESS) {
                //状态为成功
                Toast.makeText(getThis(), R.string.send_request_success, Toast.LENGTH_SHORT).show();
                //finish();
            } else {
                Toast.makeText(getThis(), booleanDataState.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        button.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.colorPrimary)));
        viewModel.getChangeRemarkResult().observe(this, stringDataState -> {
            if (stringDataState.getState() == DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(), R.string.avatar_change_success, Toast.LENGTH_SHORT).show();
                viewModel.startRefresh(getIntent().getStringExtra("id"));
            } else {
                Toast.makeText(getApplicationContext(), "失败", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getDeleteFriendResult().observe(this, dataState -> {
            if (dataState.getState() == DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(), R.string.delete_friend_success, Toast.LENGTH_SHORT).show();
                viewModel.startRefresh(getIntent().getStringExtra("id"));
            } else {
                Toast.makeText(getApplicationContext(), "失败", Toast.LENGTH_SHORT).show();
            }
        });
        viewModel.getRelationLiveData().observe(this, userRelationDataState -> {
            if (userRelationDataState.getState() == DataState.STATE.SUCCESS) {
                //是好友关系，则提供发消息入口
                relationCard.setVisibility(View.VISIBLE);
                button.setText(R.string.send_message);
                button.setIconResource(R.drawable.ic_baseline_message_24);
                button.setEnabled(true);
                button.setOnClickListener(view -> ActivityUtils.startChatActivity(getThis(),
                        Objects.requireNonNull(viewModel.getUserId())));
                remarkText.setText(userRelationDataState.getData().getRemark());
                remarkLayout.setOnClickListener(view -> {
                    UserRelation up = viewModel.getUserRelation();
                    if (up != null) {
                        new PopUpEditText()
                                .setTitle(R.string.prompt_set_remark)
                                .setText(up.getRemark())
                                .setOnConfirmListener(text -> {
                                    //控制viewModel发起更改昵称请求
                                    viewModel.startChangeRemark(text);
                                })
                                .show(getSupportFragmentManager(), "edit");
                    }

                });
                deleteLayout.setOnClickListener(view -> new PopUpText()///.setText(getString(R.string.attention_please))
                        .setTitle(R.string.attention_delete_friend)
                        .setOnConfirmListener(() -> {
                            viewModel.startDeletingFriend(getIntent().getStringExtra("id"));
                        }).show(getSupportFragmentManager(), "attention"));
            } else if (userRelationDataState.getState() == DataState.STATE.NOT_EXIST) {
                //不是好友关系，则显示”添加好友“
                relationCard.setVisibility(View.GONE);
                button.setText(R.string.make_friends);
                button.setEnabled(true);
                button.setIconResource(R.drawable.ic_baseline_person_add_24);
                button.setOnClickListener(view -> {
                    //通知viewModel进行添加好友请求
                    viewModel.startMakingFriends(getIntent().getStringExtra("id"));
                });
                remarkLayout.setOnClickListener(null);
            }else if(userRelationDataState.getState()== DataState.STATE.SPECIAL){
                //是自己
                relationCard.setVisibility(View.GONE);
                button.setText(R.string.edit_my_profile);
                button.setEnabled(true);
                button.setIconResource(R.drawable.ic_baseline_edit_24);
                button.setOnClickListener(view -> {
                    ActivityUtils.startMyProfileActivity(getThis());
                });
                remarkLayout.setOnClickListener(null);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        //用户id是Intent传来的
        String id = getIntent().getStringExtra("id");
        if (id != null) {
            viewModel.startRefresh(id);
        }
    }

    /**
     * 根据用户资料Model设置UI
     *
     * @param userInfo 用户资料对象
     */
    private void setProfileView(UserProfile userInfo) {
        if (userInfo != null) {
            ImageUtils.loadAvatarNoCacheInto(getThis(), userInfo.getAvatar(), avatarImageView);
            usernameTextView.setText(userInfo.getUsername());
            nicknameTextView.setText(userInfo.getNickname());
            genderIcon.setVisibility(View.VISIBLE);
            colorIcon.setVisibility(View.VISIBLE);
            colorIconInner.setVisibility(View.VISIBLE);
            if (TextUtils.isEmpty(userInfo.getSignature())) {
                signatureTextView.setText(R.string.place_holder_no_signature);
            } else {
                signatureTextView.setText(userInfo.getSignature());
            }
            colorIcon.setCardBackgroundColor(ColorUtils.getColorByEnum(getThis(), userInfo.getColor()));
            colorIconInner.setCardBackgroundColor(ColorUtils.getColorByEnum(getThis(), userInfo.getColor()));

            if (userInfo.getGender() == UserLocal.GENDER.MALE) {
                genderIcon.setImageResource(R.drawable.ic_male_blue_24);
            } else {
                genderIcon.setImageResource(R.drawable.ic_female_pink_24);
            }
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_profile;
    }
}
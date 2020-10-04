package com.stupidtree.hichat.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;

import com.stupidtree.hichat.R;
import com.stupidtree.hichat.data.model.UserLocal;
import com.stupidtree.hichat.data.model.UserProfile;
import com.stupidtree.hichat.ui.base.BaseActivity;
import com.stupidtree.hichat.ui.base.DataState;
import com.stupidtree.hichat.utils.ActivityUtils;
import com.stupidtree.hichat.utils.ImageUtils;

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

    @BindView(R.id.button)
    Button button;

    @BindView(R.id.avatar)
    ImageView avatarImageView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setWindowParams(true,true,false);
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
            if(userProfileDataState.getState()== DataState.STATE.SUCCESS){
                //状态为成功，设置ui
                setProfileView(userProfileDataState.getData());
            }else{
                //状态为失败，弹出错误
                Toast.makeText(getThis(),"获取出错",Toast.LENGTH_SHORT).show();
            }

        });
        viewModel.getMakeFriendsResult().observe(this, booleanDataState -> {
            if(booleanDataState.getState()== DataState.STATE.SUCCESS){
                //状态为成功，退出
                Toast.makeText(getThis(), R.string.make_friends_success,Toast.LENGTH_SHORT).show();
                finish();
            }else{
                Toast.makeText(getThis(), R.string.fail,Toast.LENGTH_SHORT).show();
            }
        });
        viewModel.getRelationLiveData().observe(this, booleanDataState -> {
            if(booleanDataState.getState()== DataState.STATE.SUCCESS){
                if(booleanDataState.getData()){
                    //是好友关系，则提供发消息入口
                    button.setText(R.string.send_message);
                    button.setEnabled(true);
                    button.setOnClickListener(view -> ActivityUtils.startChatActivity(getThis(),
                            viewModel.getUserId()));
                }else{
                    //不是好友关系，则显示”添加好友“
                    button.setText(R.string.make_friends);
                    button.setEnabled(true);
                    button.setOnClickListener(view -> {
                        //通知viewModel进行添加好友请求
                        viewModel.startMakingFriends(getIntent().getStringExtra("id"));
                    });
                }
            }else{
                button.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //用户id是Intent传来的
        String id = getIntent().getStringExtra("id");
        if(id!=null){
            viewModel.startRefresh(id);
        }
    }

    /**
     * 根据用户资料Model设置UI
     * @param userInfo 用户资料对象
     */
    private void setProfileView(UserProfile userInfo){
        if(userInfo!=null){
            ImageUtils.loadAvatarInto(getThis(),userInfo.getAvatar(),avatarImageView);
            usernameTextView.setText(userInfo.getUsername());
            nicknameTextView.setText(userInfo.getNickname());
            signatureTextView.setText(userInfo.getSignature());
            genderIcon.setVisibility(View.VISIBLE);
            if(userInfo.getGender()== UserLocal.GENDER.MALE){
                genderIcon.setImageResource(R.drawable.ic_male_blue_24);
            }else{
                genderIcon.setImageResource(R.drawable.ic_female_pink_24);
            }
        }
    }
    @Override
    protected int getLayoutId() {
        return R.layout.activity_profile;
    }
}
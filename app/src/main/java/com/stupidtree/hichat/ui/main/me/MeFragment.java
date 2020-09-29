package com.stupidtree.hichat.ui.main.me;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.stupidtree.hichat.R;
import com.stupidtree.hichat.data.model.UserLocal;
import com.stupidtree.hichat.ui.base.BaseFragment;
import com.stupidtree.hichat.utils.ActivityUtils;
import com.stupidtree.hichat.utils.ImageUtils;

import butterknife.BindView;


/**
 * ”我的“页面的fragment
 */
public class MeFragment extends BaseFragment<MeViewModel> {

    @BindView(R.id.text_username)
    TextView usernameTextView; //用户名

    @BindView(R.id.text_nickname)
    TextView nicknameTextView; //昵称

    @BindView(R.id.icon_gender)
    ImageView genderIcon; //性别的ImageView

    @BindView(R.id.profile)
    ViewGroup profileViewGroup; //用户信息那一整块View

    @BindView(R.id.avatar)
    ImageView avatarImageView; //头像的ImageView


    public MeFragment() {
    }

    public static MeFragment newInstance() {
        return new MeFragment();
    }

    @Override
    protected Class<MeViewModel> getViewModelClass() {
        return MeViewModel.class;
    }

    @Override
    protected void initViews(View view) {
        viewModel.getLocalUser().observe(this, MeFragment.this::setLocalUserInfo);

    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.triggerRefreshLocalUser();
    }


    /**
     * 根据本地用户Model设置控件
     *
     * @param userLocalInfo 本地用户对象
     */
    private void setLocalUserInfo(UserLocal userLocalInfo) {
        if (userLocalInfo.isValid()) { //如果已登录
            //装载头像
            ImageUtils.loadLocalAvatarInto(requireContext(), userLocalInfo.getAvatar(), avatarImageView);
            //设置各种文字
            usernameTextView.setText(userLocalInfo.getUsername());
            nicknameTextView.setText(userLocalInfo.getNickname());
            genderIcon.setVisibility(View.VISIBLE);
            //根据性别
            if (userLocalInfo.getGender() == UserLocal.GENDER.MALE) {
                genderIcon.setImageResource(R.drawable.ic_male_blue_24);
            } else {
                genderIcon.setImageResource(R.drawable.ic_female_pink_24);
            }
            profileViewGroup.setOnClickListener(view -> ActivityUtils.startMyProfileActivity(requireActivity()));
        } else {
            //未登录的信息显示
            usernameTextView.setText(R.string.not_logged_in);
            nicknameTextView.setText(R.string.please_log_in);
            genderIcon.setVisibility(View.GONE);
            avatarImageView.setImageResource(R.drawable.ic_baseline_emoji_emotions_24);
            profileViewGroup.setOnClickListener(view -> ActivityUtils.startLoginActivity(requireActivity()));

        }

    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_me;
    }


}
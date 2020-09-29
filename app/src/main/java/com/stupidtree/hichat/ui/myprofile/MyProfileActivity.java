package com.stupidtree.hichat.ui.myprofile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.ViewGroup;
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
import com.stupidtree.hichat.ui.widgets.PopUpEditText;
import com.stupidtree.hichat.ui.widgets.PopUpSelectableList;
import com.stupidtree.hichat.utils.FileProviderUtils;
import com.stupidtree.hichat.utils.GalleryPicker;
import com.stupidtree.hichat.utils.ImageUtils;

import java.util.Arrays;

import butterknife.BindView;

/**
 * ”我的个人资料“ Activity
 */
public class MyProfileActivity extends BaseActivity<MyProfileViewModel> {
    /**
     * 这些是调用系统相册选择、裁剪图片要用到的状态码
     */
    public static final int RC_CHOOSE_PHOTO = 10;
    public static final int RC_TAKE_PHOTO = 11;
    public static final int RC_CROP_PHOTO = 12;

    /**
     * View绑定区
     */
    @BindView(R.id.toolbar)
    Toolbar toolbar; //toolbar

    @BindView(R.id.avatar_layout)
    ViewGroup avatarLayout;//头像那一栏

    @BindView(R.id.nickname_layout)
    ViewGroup nicknameLayout;//昵称那一栏

    @BindView(R.id.signature_layout)
    ViewGroup signatureLayout;//签名那一栏

    @BindView(R.id.nickname)
    TextView nicknameText;//昵称文本

    @BindView(R.id.signature)
    TextView signatureText;//签名文本

    @BindView(R.id.avatar)
    ImageView avatarImage;//头像图片

    @BindView(R.id.logout)
    Button logoutButton;//登出按钮

    @BindView(R.id.username)
    TextView usernameText;//用户名文本

    @BindView(R.id.gender_layout)
    ViewGroup genderLayout;//性别那一栏

    @BindView(R.id.gender)
    TextView genderText;//性别文本

    @Override
    protected Class<MyProfileViewModel> getViewModelClass() {
        return MyProfileViewModel.class;
    }


    Uri cropImgUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true, true, false);
        setToolbarActionBack(toolbar);
        cropImgUri = Uri.parse("file:///" + Environment.getExternalStorageDirectory() + "/avatar_cropped.jpg");
    }

    @Override
    protected void initViews() {
        //点击头像那一栏，调用系统相册选择图片
        avatarLayout.setOnClickListener(view -> GalleryPicker.choosePhoto(getThis(), false));

        //当viewModel的UserProfile数据发生变更时，通知UI更新
        viewModel.getUserProfileLiveData().observe(this, userProfileDataState -> {
            if (userProfileDataState.getState() == DataState.STATE.SUCCESS) {
                setUserProfile(userProfileDataState.getData());
            } else {
                Toast.makeText(getThis(), "加载失败", Toast.LENGTH_SHORT).show();
            }

        });
        viewModel.getChangeAvatarResult().observe(this, new Observer<DataState<String>>() {
            @Override
            public void onChanged(DataState<String> stringDataState) {
                if (stringDataState.getState() == DataState.STATE.SUCCESS) {
                    Toast.makeText(getThis(), R.string.avatar_change_success, Toast.LENGTH_SHORT).show();
                    viewModel.startRefresh();
                } else {
                    Toast.makeText(getApplicationContext(), "失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
        viewModel.getChangeNicknameResult().observe(this, stringDataState -> {
            if (stringDataState.getState() == DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(), R.string.avatar_change_success, Toast.LENGTH_SHORT).show();
                viewModel.startRefresh();
            } else {
                Toast.makeText(getApplicationContext(), "失败", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getChangeGenderResult().observe(this, stringDataState -> {
            if (stringDataState.getState() == DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(), R.string.avatar_change_success, Toast.LENGTH_SHORT).show();
                viewModel.startRefresh();
            } else {
                Toast.makeText(getApplicationContext(), "失败", Toast.LENGTH_SHORT).show();
            }
        });

        nicknameLayout.setOnClickListener(view -> {
            DataState<UserProfile> up = viewModel.getUserProfileLiveData().getValue();
            if (up != null && up.getState() == DataState.STATE.SUCCESS) {
                new PopUpEditText()
                        .setTitle(R.string.set_nickname)
                        .setText(up.getData().getNickname())
                        .setOnConfirmListener(text -> {
                            //控制viewModel发起更改昵称请求
                            viewModel.startChangeNickname(text);
                        })
                        .show(getSupportFragmentManager(), "edit");
            }

        });

        genderLayout.setOnClickListener(view -> {
            DataState<UserProfile> up = viewModel.getUserProfileLiveData().getValue();
            if (up != null && up.getState() == DataState.STATE.SUCCESS) {
                new PopUpSelectableList<UserLocal.GENDER>()
                        .setTitle(R.string.choose_gender)
                        .setInitValue(up.getData().getGender())
                        .setListData(
                                Arrays.asList(getString(R.string.male), getString(R.string.female)),
                                Arrays.asList(UserLocal.GENDER.MALE, UserLocal.GENDER.FEMALE)
                        ).setOnConfirmListener((title, key) -> {
                    viewModel.startChangeGender(key);
                }).show(getSupportFragmentManager(), "select");
            }

        });
        logoutButton.setOnClickListener(view1 -> {
            //通知ViewModel登出
            viewModel.logout();
            finish();
        });
    }

    /**
     * 根据用户资料Model，设置UI组件
     *
     * @param profile 用户资料对象
     */
    private void setUserProfile(UserProfile profile) {
        //设置头像
        ImageUtils.loadLocalAvatarInto(getThis(), profile.getAvatar(), avatarImage);
        //设置各种文本信息
        nicknameText.setText(profile.getNickname());
        signatureText.setText(profile.getSignature());
        usernameText.setText(profile.getUsername());
        genderText.setText(profile.getGender() == UserLocal.GENDER.MALE ? R.string.male : R.string.female);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Activity启动时，就通知viewModel进行刷新UI上的用户资料
        viewModel.startRefresh();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_my_profile;
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
        switch (requestCode) {
            case RC_CHOOSE_PHOTO:
                //选择图片返回，要跳转到图片裁剪
                if (null == data) {
                    Toast.makeText(this, R.string.no_image_selected, Toast.LENGTH_SHORT).show();
                    return;
                }
                Uri uri = data.getData();
                if (null == uri) { //如果单个Uri为空，则可能是1:多个数据 2:没有数据
                    Toast.makeText(this, R.string.no_image_selected, Toast.LENGTH_SHORT).show();
                    return;
                }
                // 剪裁图片
                GalleryPicker.cropPhoto(getThis(), FileProviderUtils.getFilePathByUri(getThis(), uri), cropImgUri, 200);
                break;
            case RC_CROP_PHOTO:
                //裁剪图片返回，此时通知viewModel请求更改头像
                if (cropImgUri != null) {
                    String path = FileProviderUtils.getFilePathByUri(this, cropImgUri);
                    // create RequestBody instance from file
                    viewModel.startChangeAvatar(path);
                }

                break;
        }
    }

}
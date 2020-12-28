package com.stupidtree.cloudliter.ui.myprofile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.UserLocal.GENDER
import com.stupidtree.cloudliter.data.model.UserProfile
import com.stupidtree.cloudliter.data.model.UserProfile.COLOR
import com.stupidtree.cloudliter.databinding.ActivityMyProfileBinding
import com.stupidtree.cloudliter.ui.base.BaseActivity
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.widgets.PopUpEditText
import com.stupidtree.cloudliter.ui.widgets.PopUpSelectableList
import com.stupidtree.cloudliter.utils.*

/**
 * ”我的个人资料“ Activity
*/
class MyProfileActivity : BaseActivity<MyProfileViewModel, ActivityMyProfileBinding>() {

    override fun getViewModelClass(): Class<MyProfileViewModel> {
        return MyProfileViewModel::class.java
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setWindowParams(
                statusBar = true,
                darkColor = true,
                navi = false
        )
        setToolbarActionBack(binding.toolbar)

        // cropImgUri = Uri.parse("file:///" + Environment.getExternalStorageDirectory() + "/avatar_cropped.jpg")
    }

    override fun initViews() {
        //点击头像那一栏，调用系统相册选择图片
        binding.avatarLayout.setOnClickListener { GalleryPicker.choosePhoto(getThis(), false) }

        //当viewModel的UserProfile数据发生变更时，通知UI更新
        viewModel.userProfileLiveData?.observe(this, { userProfileDataState: DataState<UserProfile?> ->
            if (userProfileDataState.state === DataState.STATE.SUCCESS) {
                setUserProfile(userProfileDataState.data)
            } else {
                Toast.makeText(getThis(), "加载失败", Toast.LENGTH_SHORT).show()
            }
        })
        viewModel.changeAvatarResult?.observe(this, { stringDataState ->
            if (stringDataState.state === DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(), R.string.avatar_change_success, Toast.LENGTH_SHORT).show()
                viewModel.startRefresh()
            } else {
                Toast.makeText(applicationContext, R.string.fail, Toast.LENGTH_SHORT).show()
            }
        })
        viewModel.changeNicknameResult?.observe(this, { stringDataState: DataState<String?> ->
            if (stringDataState.state === DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(), R.string.avatar_change_success, Toast.LENGTH_SHORT).show()
                viewModel.startRefresh()
            } else {
                Toast.makeText(applicationContext, R.string.fail, Toast.LENGTH_SHORT).show()
            }
        })
        viewModel.changeGenderResult?.observe(this, { stringDataState: DataState<String?> ->
            if (stringDataState.state === DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(), R.string.avatar_change_success, Toast.LENGTH_SHORT).show()
                viewModel.startRefresh()
            } else {
                Toast.makeText(applicationContext, R.string.fail, Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.setWCAccessibilityResult?.observe(this, { stringDataState: DataState<String?> ->
            if (stringDataState.state === DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(), R.string.set_success, Toast.LENGTH_SHORT).show()
                viewModel.startRefresh()
            } else {
                Toast.makeText(applicationContext, R.string.fail, Toast.LENGTH_SHORT).show()
            }
        })
        viewModel.changeColorResult?.observe(this, { stringDataState: DataState<String?> ->
            if (stringDataState.state === DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(), R.string.avatar_change_success, Toast.LENGTH_SHORT).show()
                viewModel.startRefresh()
            } else {
                Toast.makeText(applicationContext, R.string.fail, Toast.LENGTH_SHORT).show()
            }
        })
        viewModel.changeSignatureResult?.observe(this, { stringDataState: DataState<String?> ->
            if (stringDataState.state === DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(), R.string.avatar_change_success, Toast.LENGTH_SHORT).show()
                viewModel.startRefresh()
            } else {
                Toast.makeText(applicationContext, R.string.fail, Toast.LENGTH_SHORT).show()
            }
        })
        binding.nicknameLayout.setOnClickListener {
            val up = viewModel.userProfileLiveData?.value
            if (up != null && up.state === DataState.STATE.SUCCESS) {
                PopUpEditText()
                        .setTitle(R.string.set_nickname)
                        .setText(up.data!!.nickname)
                        .setOnConfirmListener(object : PopUpEditText.OnConfirmListener {
                            override fun OnConfirm(text: String) {
                                //控制viewModel发起更改昵称请求
                                viewModel.startChangeNickname(text)
                            }
                        })
                        .show(supportFragmentManager, "edit")
            }
        }

        //点击更改性别，弹出选择框
        binding.genderLayout.setOnClickListener {
            val up = viewModel.userProfileLiveData?.value
            if (up != null && up.state === DataState.STATE.SUCCESS) {
                PopUpSelectableList<GENDER>()
                        .setTitle(R.string.choose_gender)
                        .setInitValue(up.data!!.gender)
                        .setListData(
                                listOf(getString(R.string.male), getString(R.string.female)),
                                listOf(GENDER.MALE, GENDER.FEMALE)
                        ).setOnConfirmListener(object : PopUpSelectableList.OnConfirmListener<GENDER> {

                            override fun onConfirm(title: String?, key: GENDER) {
                                viewModel.startChangeGender(key)
                            }
                        })
                        .show(supportFragmentManager, "select")
            }
        }

        //点击更改颜色，弹出选择框
        binding.colorLayout.setOnClickListener {
            val up = viewModel.userProfileLiveData?.value
            if (up != null && up.state === DataState.STATE.SUCCESS) {
                PopUpSelectableList<COLOR>()
                        .setTitle(R.string.choose_color)
                        .setInitValue(up.data!!.color)
                        .setListData(
                                listOf(getString(R.string.red),
                                        getString(R.string.orange),
                                        getString(R.string.yellow),
                                        getString(R.string.green),
                                        getString(R.string.cyan),
                                        getString(R.string.blue),
                                        getString(R.string.purple)),
                                listOf(COLOR.RED,
                                        COLOR.ORANGE,
                                        COLOR.YELLOW,
                                        COLOR.GREEN,
                                        COLOR.CYAN,
                                        COLOR.BLUE,
                                        COLOR.PURPLE)
                        ).setOnConfirmListener(object : PopUpSelectableList.OnConfirmListener<COLOR> {

                            override fun onConfirm(title: String?, key: COLOR) {
                                viewModel.startChangeColor(key)
                            }
                        }).show(supportFragmentManager, "select")
            }
        }
        binding.signatureLayout.setOnClickListener {
            val up = viewModel.userProfileLiveData?.value
            if (up != null && up.state === DataState.STATE.SUCCESS) {
                PopUpEditText()
                        .setTitle(R.string.choose_signature)
                        .setText(up.data!!.signature)
                        .setOnConfirmListener(object : PopUpEditText.OnConfirmListener {
                            override fun OnConfirm(text: String) {
                                //控制viewModel发起更改签名请求
                                viewModel.startChangeSignature(text)
                            }
                        })
                        .show(supportFragmentManager, "edit")
            }
        }

        binding.wordcloudLayout.setOnClickListener {
            viewModel.userProfileLiveData!!.value?.let {
                if (it.data != null) {
                    PopUpSelectableList<Boolean>()
                            .setTitle(R.string.choose_color)
                            .setInitValue(it.data!!.wordCloudPrivate)
                            .setListData(
                                    listOf(getString(R.string.word_cloud_public),
                                            getString(R.string.word_cloud_private)),
                                    listOf(false, true))
                            .setOnConfirmListener(object : PopUpSelectableList.OnConfirmListener<Boolean> {
                                override fun onConfirm(title: String?, key: Boolean) {
                                    viewModel.startChangeWCAccessibility(key)
                                }
                            }).show(supportFragmentManager, "select")
                }
            }


        }
    }

    /**
     * 根据用户资料Model，设置UI组件
     *
     * @param profile 用户资料对象
     */
    private fun setUserProfile(profile: UserProfile?) {
        //设置头像
        ImageUtils.loadLocalAvatarInto(getThis(), profile!!.avatar, binding.avatar)
        //设置各种文本信息
        binding.color.setText(profile.colorName)
        binding.nickname.text = profile.nickname
        binding.wordcloudAccessibility.setText(if (profile.wordCloudPrivate) R.string.word_cloud_private else R.string.word_cloud_public)
        binding.iconColor.setCardBackgroundColor(ColorUtils.getColorByEnum(getThis(), profile.color))
        binding.iconColorInner.setCardBackgroundColor(ColorUtils.getColorByEnum(getThis(), profile.color))
        if (!TextUtils.isEmpty(profile.signature)) {
            binding.signature.text = profile.signature
        } else {
            binding.signature.setText(R.string.place_holder_no_signature)
        }
        binding.username.text = profile.username
        binding.gender.setText(if (profile.gender == GENDER.MALE) R.string.male else R.string.female)
    }

    override fun onStart() {
        super.onStart()
        //Activity启动时，就通知viewModel进行刷新UI上的用户资料
        viewModel.startRefresh()
    }


    /**
     * 当用户通过系统相册选择图片返回时，将调用本函数
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        when (requestCode) {
            RC_CHOOSE_PHOTO -> {
                //选择图片返回，要跳转到图片裁剪
                if (null == data) {
                    Toast.makeText(this, R.string.no_image_selected, Toast.LENGTH_SHORT).show()
                    return
                }
                val uri = data.data
                if (null == uri) { //如果单个Uri为空，则可能是1:多个数据 2:没有数据
                    Toast.makeText(this, R.string.no_image_selected, Toast.LENGTH_SHORT).show()
                    return
                }
                // 剪裁图片
                GalleryPicker.cropPhoto(getThis(), FileProviderUtils.getFilePathByUri(getThis(), uri), 200)
            }
            RC_CROP_PHOTO -> {                //裁剪图片返回，此时通知viewModel请求更改头像
                val path = GalleryPicker.getCroppedCacheDir(this)?.let { FileProviderUtils.getFilePathByUri(this, it) }
                // create RequestBody instance from file
                path?.let { viewModel.startChangeAvatar(it) }
            }
        }
    }

    companion object {
        /**
         * 这些是调用系统相册选择、裁剪图片要用到的状态码
         */
        const val RC_CHOOSE_PHOTO = 10
        const val RC_TAKE_PHOTO = 11
        const val RC_CROP_PHOTO = 12
    }

    override fun initViewBinding(): ActivityMyProfileBinding {
        return ActivityMyProfileBinding.inflate(layoutInflater)
    }
}
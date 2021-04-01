package com.stupidtree.cloudliter.ui.myprofile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.UserLocal
import com.stupidtree.cloudliter.data.model.UserLocal.GENDER
import com.stupidtree.cloudliter.data.model.UserProfile
import com.stupidtree.cloudliter.data.model.UserProfile.Companion.HEARING
import com.stupidtree.cloudliter.data.model.UserProfile.Companion.LIMB
import com.stupidtree.cloudliter.data.model.UserProfile.Companion.VISUAL
import com.stupidtree.cloudliter.databinding.ActivityMyProfileBinding
import com.stupidtree.style.base.BaseActivity
import com.stupidtree.component.data.DataState
import com.stupidtree.style.widgets.PopUpEditText
import com.stupidtree.style.widgets.PopUpMultipleCheckableList
import com.stupidtree.style.widgets.PopUpSelectableList
import com.stupidtree.style.picker.FileProviderUtils
import com.stupidtree.style.picker.GalleryPicker
import com.stupidtree.cloudliter.utils.ImageUtils
import com.stupidtree.cloudliter.utils.TextUtils
import com.stupidtree.style.picker.GalleryPicker.RC_CHOOSE_PHOTO
import com.stupidtree.style.picker.GalleryPicker.RC_CROP_PHOTO

/**
 * ”我的个人资料“ Activity
*/
class MyProfileActivity : BaseActivity<MyProfileViewModel, ActivityMyProfileBinding>() {

    override fun getViewModelClass(): Class<MyProfileViewModel> {
        return MyProfileViewModel::class.java
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolbarActionBack(binding.toolbar)

        // cropImgUri = Uri.parse("file:///" + Environment.getExternalStorageDirectory() + "/avatar_cropped.jpg")
    }

    override fun initViews() {
        //点击头像那一栏，调用系统相册选择图片
        binding.avatarLayout.setOnClickListener { GalleryPicker.choosePhoto(getThis(), false) }

        //当viewModel的UserProfile数据发生变更时，通知UI更新
        viewModel.userProfileLiveData.observe(this, { userProfileDataState: DataState<UserProfile?> ->
            if (userProfileDataState.state === DataState.STATE.SUCCESS) {
                setUserProfile(userProfileDataState.data!!)
            } else {
                Toast.makeText(getThis(), R.string.connection_failed, Toast.LENGTH_SHORT).show()
            }
        })
        viewModel.changeAvatarResult.observe(this, { stringDataState ->
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
        viewModel.changeAccessibilityResult?.observe(this, { stringDataState: DataState<String?> ->
            if (stringDataState.state === DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(), R.string.avatar_change_success, Toast.LENGTH_SHORT).show()
                viewModel.startRefresh()
            } else {
                Toast.makeText(applicationContext, R.string.fail, Toast.LENGTH_SHORT).show()
            }
        })
        viewModel.changeTypeResult?.observe(this, { stringDataState: DataState<String?> ->
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
            val up = viewModel.userProfileLiveData.value
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
            val up = viewModel.userProfileLiveData.value
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

        // 点击更换无障碍隐私类型，弹出选择框
        binding.typePermissionLayout.setOnClickListener {
            val up = viewModel.userProfileLiveData.value
            if (up != null && up.state === DataState.STATE.SUCCESS) {
                PopUpSelectableList<UserLocal.TYPEPERMISSION>()
                        .setTitle(R.string.type_permission)
                        .setInitValue(up.data!!.typePermission)
                        .setListData(
                                listOf(getString(R.string.type_permission_public),
                                        getString(R.string.type_permission_protected),
                                        getString(R.string.type_permission_private)),
                                listOf(UserLocal.TYPEPERMISSION.PUBLIC,
                                        UserLocal.TYPEPERMISSION.PROTECTED,
                                        UserLocal.TYPEPERMISSION.PRIVATE)
                        ).setOnConfirmListener(object : PopUpSelectableList.OnConfirmListener<UserLocal.TYPEPERMISSION> {
                            override fun onConfirm(title: String?, key: UserLocal.TYPEPERMISSION) {
                                viewModel.startChangeType(up.data!!.type, up.data!!.subType, key)
                            }
                        }).show(supportFragmentManager, "select")
            }
        }

        //点击更改用户类型，弹出多选框
        binding.typeLayout.setOnClickListener {
            viewModel.userProfileLiveData.value?.data?.let {
                    PopUpMultipleCheckableList<Int>(R.string.type, 0, 0)
                            .setInitValues(it.getTypeList(it.type))
                            .setListData(
                                    listOf(getString(R.string.type_visual),
                                            getString(R.string.type_hearing),
                                            getString(R.string.type_limb)),
                                    listOf(VISUAL,
                                            HEARING,
                                            LIMB)
                            ).setOnConfirmListener(object : PopUpMultipleCheckableList.OnConfirmListener<Int> {
                                override fun onConfirm(titles: List<String?>, data: List<Int>) {
                                    var key = 0
                                    for (d in data) {
                                        key = d xor key
                                    }
                                    viewModel.startChangeType(key, it.subType, it.typePermission)
                                }
                            }).show(supportFragmentManager, "select")
            }
        }
        binding.signatureLayout.setOnClickListener {
            val up = viewModel.userProfileLiveData.value
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
            viewModel.userProfileLiveData.value?.let {
                if (it.data != null) {
                    PopUpSelectableList<Boolean>()
                            .setTitle(R.string.choose_word_cloud_permission)
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
    private fun setUserProfile(profile: UserProfile) {
        //设置头像
        ImageUtils.loadAvatarInto(getThis(), profile.avatar, binding.avatar)
        //设置各种文本信息
        binding.typePermission.setText(profile.getTypePermissionName())
        binding.type.setText(profile.getTypeName())
        binding.nickname.text = profile.nickname
        binding.wordcloudAccessibility.setText(if (profile.wordCloudPrivate) R.string.word_cloud_private else R.string.word_cloud_public)
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



    override fun initViewBinding(): ActivityMyProfileBinding {
        return ActivityMyProfileBinding.inflate(layoutInflater)
    }
}
package com.stupidtree.cloudliter.ui.myprofile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import butterknife.BindView
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.UserLocal.GENDER
import com.stupidtree.cloudliter.data.model.UserProfile
import com.stupidtree.cloudliter.data.model.UserProfile.COLOR
import com.stupidtree.cloudliter.ui.base.BaseActivity
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.widgets.PopUpEditText
import com.stupidtree.cloudliter.ui.widgets.PopUpSelectableList
import com.stupidtree.cloudliter.utils.*
import java.util.*

/**
 * ”我的个人资料“ Activity
 */
class MyProfileActivity : BaseActivity<MyProfileViewModel>() {
    /**
     * View绑定区
     */
    @JvmField
    @BindView(R.id.toolbar)
    var toolbar //toolbar
            : Toolbar? = null

    @JvmField
    @BindView(R.id.avatar_layout)
    var avatarLayout //头像那一栏
            : ViewGroup? = null

    @JvmField
    @BindView(R.id.nickname_layout)
    var nicknameLayout //昵称那一栏
            : ViewGroup? = null

    @JvmField
    @BindView(R.id.signature_layout)
    var signatureLayout //签名那一栏
            : ViewGroup? = null

    @JvmField
    @BindView(R.id.color_layout)
    var colorLayout //颜色那一栏
            : ViewGroup? = null

    @JvmField
    @BindView(R.id.nickname)
    var nicknameText //昵称文本
            : TextView? = null

    @JvmField
    @BindView(R.id.signature)
    var signatureText //签名文本
            : TextView? = null

    @JvmField
    @BindView(R.id.avatar)
    var avatarImage //头像图片
            : ImageView? = null

    @JvmField
    @BindView(R.id.color)
    var colorText //颜色文本
            : TextView? = null

    @JvmField
    @BindView(R.id.username)
    var usernameText //用户名文本
            : TextView? = null

    @JvmField
    @BindView(R.id.gender_layout)
    var genderLayout //性别那一栏
            : ViewGroup? = null

    @JvmField
    @BindView(R.id.gender)
    var genderText //性别文本
            : TextView? = null

    @JvmField
    @BindView(R.id.icon_color)
    var colorIcon: CardView? = null

    @JvmField
    @BindView(R.id.icon_color_inner)
    var colorIconInner: CardView? = null
    override fun getViewModelClass(): Class<MyProfileViewModel>? {
        return MyProfileViewModel::class.java
    }

    var cropImgUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setWindowParams(true, true, false)
        setToolbarActionBack(toolbar!!)
        cropImgUri = Uri.parse("file:///" + Environment.getExternalStorageDirectory() + "/avatar_cropped.jpg")
    }

    override fun initViews() {
        //点击头像那一栏，调用系统相册选择图片
        avatarLayout!!.setOnClickListener { view: View? -> GalleryPicker.choosePhoto(getThis(), false) }

        //当viewModel的UserProfile数据发生变更时，通知UI更新
        viewModel!!.userProfileLiveData?.observe(this, Observer { userProfileDataState: DataState<UserProfile?> ->
            if (userProfileDataState.state === DataState.STATE.SUCCESS) {
                setUserProfile(userProfileDataState.data)
            } else {
                Toast.makeText(getThis(), "加载失败", Toast.LENGTH_SHORT).show()
            }
        })
        viewModel!!.changeAvatarResult?.observe(this, Observer { stringDataState ->
            if (stringDataState.state === DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(), R.string.avatar_change_success, Toast.LENGTH_SHORT).show()
                viewModel!!.startRefresh()
            } else {
                Toast.makeText(applicationContext, "失败", Toast.LENGTH_SHORT).show()
            }
        })
        viewModel!!.changeNicknameResult?.observe(this, Observer { stringDataState: DataState<String?> ->
            if (stringDataState.state === DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(), R.string.avatar_change_success, Toast.LENGTH_SHORT).show()
                viewModel!!.startRefresh()
            } else {
                Toast.makeText(applicationContext, "失败", Toast.LENGTH_SHORT).show()
            }
        })
        viewModel!!.changeGenderResult?.observe(this, Observer { stringDataState: DataState<String?> ->
            if (stringDataState.state === DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(), R.string.avatar_change_success, Toast.LENGTH_SHORT).show()
                viewModel!!.startRefresh()
            } else {
                Toast.makeText(applicationContext, "失败", Toast.LENGTH_SHORT).show()
            }
        })
        viewModel!!.changeColorResult?.observe(this, Observer { stringDataState: DataState<String?> ->
            if (stringDataState.state === DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(), R.string.avatar_change_success, Toast.LENGTH_SHORT).show()
                viewModel!!.startRefresh()
            } else {
                Toast.makeText(applicationContext, "失败", Toast.LENGTH_SHORT).show()
            }
        })
        viewModel!!.changeSignatureResult?.observe(this, Observer { stringDataState: DataState<String?> ->
            if (stringDataState.state === DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(), R.string.avatar_change_success, Toast.LENGTH_SHORT).show()
                viewModel!!.startRefresh()
            } else {
                Toast.makeText(applicationContext, "失败", Toast.LENGTH_SHORT).show()
            }
        })
        nicknameLayout!!.setOnClickListener { view: View? ->
            val up = viewModel!!.userProfileLiveData?.value
            if (up != null && up.state === DataState.STATE.SUCCESS) {
                PopUpEditText()
                        .setTitle(R.string.set_nickname)
                        .setText(up.data!!.nickname)
                        .setOnConfirmListener(object :PopUpEditText.OnConfirmListener{
                            override fun OnConfirm(text: String) {
                                //控制viewModel发起更改昵称请求
                                viewModel!!.startChangeNickname(text)
                            }
                        })
                        .show(supportFragmentManager, "edit")
            }
        }

        //点击更改性别，弹出选择框
        genderLayout!!.setOnClickListener { view: View? ->
            val up = viewModel!!.userProfileLiveData?.value
            if (up != null && up.state === DataState.STATE.SUCCESS) {
                PopUpSelectableList<GENDER>()
                        .setTitle(R.string.choose_gender)
                        .setInitValue(up.data!!.gender)
                        .setListData(
                                Arrays.asList(getString(R.string.male), getString(R.string.female)),
                                Arrays.asList(GENDER.MALE, GENDER.FEMALE)
                        ).setOnConfirmListener { title: String?, key: GENDER? -> viewModel!!.startChangeGender(key!!) }.show(supportFragmentManager, "select")
            }
        }

        //点击更改颜色，弹出选择框
        colorLayout!!.setOnClickListener { view: View? ->
            val up = viewModel!!.userProfileLiveData?.value
            if (up != null && up.state === DataState.STATE.SUCCESS) {
                PopUpSelectableList<COLOR>()
                        .setTitle(R.string.choose_color)
                        .setInitValue(up.data!!.color)
                        .setListData(
                                Arrays.asList(getString(R.string.red),
                                        getString(R.string.orange),
                                        getString(R.string.yellow),
                                        getString(R.string.green),
                                        getString(R.string.cyan),
                                        getString(R.string.blue),
                                        getString(R.string.purple)),
                                Arrays.asList(COLOR.RED,
                                        COLOR.ORANGE,
                                        COLOR.YELLOW,
                                        COLOR.GREEN,
                                        COLOR.CYAN,
                                        COLOR.BLUE,
                                        COLOR.PURPLE)
                        ).setOnConfirmListener { title: String?, key: COLOR? -> viewModel!!.startChangeColor(key!!) }.show(supportFragmentManager, "select")
            }
        }
        signatureLayout!!.setOnClickListener { view: View? ->
            val up = viewModel!!.userProfileLiveData?.value
            if (up != null && up.state === DataState.STATE.SUCCESS) {
                PopUpEditText()
                        .setTitle(R.string.choose_signature)
                        .setText(up.data!!.signature)
                        .setOnConfirmListener(object:PopUpEditText.OnConfirmListener{
                            override fun OnConfirm(text: String) {
                                //控制viewModel发起更改签名请求
                                viewModel!!.startChangeSignature(text)
                            }
                        })
                        .show(supportFragmentManager, "edit")
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
        ImageUtils.loadLocalAvatarInto(getThis(), profile!!.avatar, avatarImage!!)
        //设置各种文本信息
        colorText!!.setText(profile.colorName)
        nicknameText!!.text = profile.nickname
        colorIcon!!.setCardBackgroundColor(ColorUtils.getColorByEnum(getThis(), profile.color))
        colorIconInner!!.setCardBackgroundColor(ColorUtils.getColorByEnum(getThis(), profile.color))
        if (!TextUtils.isEmpty(profile.signature)) {
            signatureText!!.text = profile.signature
        } else {
            signatureText!!.setText(R.string.place_holder_no_signature)
        }
        usernameText!!.text = profile.username
        genderText!!.setText(if (profile.gender == GENDER.MALE) R.string.male else R.string.female)
    }

    override fun onStart() {
        super.onStart()
        //Activity启动时，就通知viewModel进行刷新UI上的用户资料
        viewModel!!.startRefresh()
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_my_profile
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
                GalleryPicker.cropPhoto(getThis(), FileProviderUtils.getFilePathByUri(getThis(), uri), cropImgUri, 200)
            }
            RC_CROP_PHOTO ->                 //裁剪图片返回，此时通知viewModel请求更改头像
                if (cropImgUri != null) {
                    val path = FileProviderUtils.getFilePathByUri(this, cropImgUri)
                    // create RequestBody instance from file
                    viewModel!!.startChangeAvatar(path)
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
}
package com.stupidtree.cloudliter.ui.profile

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import butterknife.BindView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.RelationGroup
import com.stupidtree.cloudliter.data.model.UserLocal
import com.stupidtree.cloudliter.data.model.UserProfile
import com.stupidtree.cloudliter.ui.base.BaseActivity
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.group.pick.PickGroupDialog
import com.stupidtree.cloudliter.ui.widgets.PopUpEditText
import com.stupidtree.cloudliter.ui.widgets.PopUpText
import com.stupidtree.cloudliter.ui.widgets.WordsCloudView
import com.stupidtree.cloudliter.utils.ActivityUtils
import com.stupidtree.cloudliter.utils.ColorUtils
import com.stupidtree.cloudliter.utils.ImageUtils
import com.stupidtree.cloudliter.utils.TextUtils
import java.util.*

/**
 * 其他用户（好友、搜索结果等）的资料页面Activity
 */
@SuppressLint("NonConstantResourceId")
class ProfileActivity : BaseActivity<ProfileViewModel>() {
    /**
     * View绑定区
     */
    @JvmField
    @BindView(R.id.toolbar)
    var toolbar: Toolbar? = null

    @JvmField
    @BindView(R.id.text_username)
    var usernameTextView: TextView? = null

    @BindView(R.id.text_nickname)
    lateinit var nicknameTextView: TextView

    @BindView(R.id.text_signature)
    lateinit var signatureTextView: TextView

    @BindView(R.id.icon_gender)
    lateinit var genderIcon: ImageView

    @JvmField
    @BindView(R.id.fab)
    var button: ExtendedFloatingActionButton? = null

    @JvmField
    @BindView(R.id.avatar)
    var avatarImageView: ImageView? = null

    @BindView(R.id.avatar_card)
    lateinit var avatarCardView: CardView

    @BindView(R.id.appbar)
    lateinit var appbarLayout:AppBarLayout

    @JvmField
    @BindView(R.id.icon_color)
    var colorIcon: CardView? = null

    @JvmField
    @BindView(R.id.icon_color_inner)
    var colorIconInner: CardView? = null

    @JvmField
    @BindView(R.id.remark_layout)
    var remarkLayout: ViewGroup? = null

    @JvmField
    @BindView(R.id.group_layout)
    var groupLayout: ViewGroup? = null

    @JvmField
    @BindView(R.id.delete_layout)
    var deleteLayout: ViewGroup? = null

    @JvmField
    @BindView(R.id.remark)
    var remarkText: TextView? = null

    @JvmField
    @BindView(R.id.groupName)
    var groupName: TextView? = null

    @JvmField
    @BindView(R.id.relation_card)
    var relationCard: ViewGroup? = null

    @BindView(R.id.logout)
    lateinit var logoutButton //登出按钮
            : View

    @JvmField
    @BindView(R.id.wordstag_layout)
    var wordsCloudView: WordsCloudView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        setWindowParams(statusBar = true, darkColor = true, navi = false)
        super.onCreate(savedInstanceState)
        setToolbarActionBack(toolbar!!)
    }

    override fun getViewModelClass(): Class<ProfileViewModel> {
        return ProfileViewModel::class.java
    }

    override fun initViews() {
        val initElevation = avatarCardView.cardElevation
       // avatarCardView.pivotX = 64f
        appbarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val percentage = -verticalOffset.toFloat()/ appBarLayout!!.totalScrollRange.toFloat()
            avatarCardView.pivotY = avatarCardView.height.toFloat()*0.5f
            avatarCardView.pivotX = avatarCardView.width.toFloat()*0.3f
            avatarCardView.scaleX = 1- percentage
            avatarCardView.scaleY = 1- percentage
            nicknameTextView.alpha = 1-2.5f*percentage
            signatureTextView.alpha = 1-2.5f*percentage
            genderIcon.alpha = 1-2.5f*percentage
            avatarCardView.cardElevation = initElevation*(1-percentage)
            Log.e("percentage",percentage.toString())
        })

        //为ViewModel中的各种数据设置监听
        viewModel!!.userProfileLiveData?.observe(this, Observer { userProfileDataState ->
            if (userProfileDataState?.state === DataState.STATE.SUCCESS) {
                //状态为成功，设置ui
                setProfileView(userProfileDataState.data)
            } else {
                //状态为失败，弹出错误
                Toast.makeText(getThis(), "获取出错", Toast.LENGTH_SHORT).show()
            }
        })
        viewModel!!.makeFriendsResult?.observe(this, Observer { booleanDataState ->
            if (booleanDataState?.state === DataState.STATE.SUCCESS) {
                //状态为成功
                Toast.makeText(getThis(), R.string.send_request_success, Toast.LENGTH_SHORT).show()
                //finish();
            } else {
                Toast.makeText(getThis(), booleanDataState?.message, Toast.LENGTH_SHORT).show()
            }
        })
        button!!.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(getThis(), R.color.colorPrimary))
        viewModel!!.changeRemarkResult?.observe(this, Observer { stringDataState ->
            if (stringDataState!!.state === DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(), R.string.avatar_change_success, Toast.LENGTH_SHORT).show()
                intent.getStringExtra("id")?.let { viewModel!!.startRefresh(it) }
            } else {
                Toast.makeText(applicationContext, "失败", Toast.LENGTH_SHORT).show()
            }
        })
        viewModel!!.deleteFriendResult?.observe(this, Observer { dataState: DataState<*> ->
            if (dataState.state === DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(), R.string.delete_friend_success, Toast.LENGTH_SHORT).show()
                intent.getStringExtra("id")?.let { viewModel!!.startRefresh(it) }
            } else {
                Toast.makeText(applicationContext, "失败", Toast.LENGTH_SHORT).show()
            }
        })
        viewModel!!.assignGroupResult?.observe(this, Observer { dataState: DataState<*> ->
            if (dataState.state === DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(), R.string.assign_group_success, Toast.LENGTH_SHORT).show()
                intent.getStringExtra("id")?.let { viewModel!!.startRefresh(it) }
            } else {
                Toast.makeText(applicationContext, R.string.fail, Toast.LENGTH_SHORT).show()
            }
        })
        viewModel!!.relationLiveData?.observe(this, { userRelationDataState ->
            when {
                userRelationDataState!!.state == DataState.STATE.SUCCESS -> {
                    //是好友关系，则提供发消息入口
                    relationCard!!.visibility = View.VISIBLE
                    logoutButton.visibility = View.GONE
                    button!!.setText(R.string.send_message)
                    button!!.setIconResource(R.drawable.ic_baseline_message_24)
                    button!!.isEnabled = true
                    button!!.setOnClickListener {
                        if (viewModel!!.getUserRelation() != null && viewModel!!.getUserProfile() != null && viewModel!!.getUserLocal() != null) {
                            ActivityUtils.startChatActivity(getThis(), viewModel!!.getUserProfile()!!, viewModel!!.getUserRelation()!!, viewModel!!.getUserLocal()!!)
                        }
                    }
                    remarkText!!.text = userRelationDataState.data!!.remark
                    remarkLayout!!.setOnClickListener {
                        val up = viewModel!!.getUserRelation()
                        if (up != null) {
                            PopUpEditText()
                                    .setTitle(R.string.prompt_set_remark)
                                    .setText(up.remark)
                                    .setOnConfirmListener(object : PopUpEditText.OnConfirmListener {
                                        override fun OnConfirm(text: String) {
                                            //控制viewModel发起更改昵称请求
                                            viewModel!!.startChangeRemark(text)
                                        }
                                    })
                                    .show(supportFragmentManager, "edit")
                        }
                    }
                    groupName!!.text = userRelationDataState.data!!.groupName
                    groupLayout!!.setOnClickListener {
                        if (viewModel!!.getUserRelation() != null) {
                            //Log.e("relation", String.valueOf(viewModel.getUserRelation()));
                            PickGroupDialog()
                                    .setInitGroupId(viewModel!!.getUserRelation()!!.groupId)
                                    .setOnConfirmListener(object : PickGroupDialog.OnConfirmListener {
                                        override fun OnConfirmed(group: RelationGroup?) {
                                            group?.let { viewModel!!.startAssignGroup(it) }
                                        }
                                    }).show(supportFragmentManager, "pick_group")
                        }
                    }
                    deleteLayout!!.setOnClickListener {
                        PopUpText() ///.setText(getString(R.string.attention_please))
                                .setTitle(R.string.attention_delete_friend)
                                .setOnConfirmListener (
                                        object:PopUpText.OnConfirmListener{
                                            override fun OnConfirm() {
                                                intent.getStringExtra("id")?.let { viewModel!!.startDeletingFriend(it) }
                                            }
                                        }
                                ).show(supportFragmentManager, "attention")
                    }
                }
                userRelationDataState.state === DataState.STATE.NOT_EXIST -> {
                    //不是好友关系，则显示”添加好友“
                    relationCard!!.visibility = View.GONE
                    button!!.setText(R.string.make_friends)
                    logoutButton.visibility = View.GONE
                    button!!.isEnabled = true
                    button!!.setIconResource(R.drawable.ic_baseline_person_add_24)
                    button!!.setOnClickListener {
                        //通知viewModel进行添加好友请求
                        intent.getStringExtra("id")?.let { viewModel!!.startMakingFriends(it) }
                    }
                    remarkLayout!!.setOnClickListener(null)
                }
                userRelationDataState.state === DataState.STATE.SPECIAL -> {
                    //是自己
                    if(intent.getBooleanExtra("showLogout",true)){
                        logoutButton.visibility = View.VISIBLE
                    }else {
                        logoutButton.visibility = View.GONE
                    }

                    relationCard!!.visibility = View.GONE
                    button!!.setText(R.string.edit_my_profile)
                    button!!.isEnabled = true
                    button!!.setIconResource(R.drawable.ic_baseline_edit_24)
                    button!!.setOnClickListener { ActivityUtils.startMyProfileActivity(getThis()) }
                    remarkLayout!!.setOnClickListener(null)
                }
            }
        })
        viewModel!!.wordCloudLiveData?.observe(this, Observer { listDataState ->
            if (listDataState.state === DataState.STATE.SUCCESS) {
                val tag = ArrayList<String>()
                for ((key) in listDataState.data!!) {
                    tag.add(key)
                }
                wordsCloudView!!.setTags(tag)
            }
        })
        logoutButton.setOnClickListener {
            //通知ViewModel登出
            PopUpText().setTitle(R.string.logout_hint).setOnConfirmListener(
                    object :PopUpText.OnConfirmListener{
                        override fun OnConfirm() {
                            viewModel!!.logout(getThis())
                            finish()
                        }

                    }
            ).show(supportFragmentManager,"logout")

        }
    }

    override fun onResume() {
        super.onResume()
        //用户id是Intent传来的
        val id = intent.getStringExtra("id")
        if (id != null) {
            viewModel!!.startRefresh(id)
        }
    }

    /**
     * 根据用户资料Model设置UI
     *
     * @param userInfo 用户资料对象
     */
    private fun setProfileView(userInfo: UserProfile?) {
        if (userInfo != null) {
            ImageUtils.loadAvatarNoCacheInto(getThis(), userInfo.avatar, avatarImageView!!)
            usernameTextView!!.text = userInfo.username
            nicknameTextView.text = userInfo.nickname
            genderIcon.visibility = View.VISIBLE
            colorIcon!!.visibility = View.VISIBLE
            colorIconInner!!.visibility = View.VISIBLE
            if (TextUtils.isEmpty(userInfo.signature)) {
                signatureTextView.setText(R.string.place_holder_no_signature)
            } else {
                signatureTextView.text = userInfo.signature
            }
            colorIcon!!.setCardBackgroundColor(ColorUtils.getColorByEnum(getThis(), userInfo.color))
            colorIconInner!!.setCardBackgroundColor(ColorUtils.getColorByEnum(getThis(), userInfo.color))
            if (userInfo.gender == UserLocal.GENDER.MALE) {
                genderIcon.setImageResource(R.drawable.ic_male_blue_24)
            } else {
                genderIcon.setImageResource(R.drawable.ic_female_pink_24)
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_profile
    }
}
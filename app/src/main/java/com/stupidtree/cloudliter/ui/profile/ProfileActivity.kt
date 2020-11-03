package com.stupidtree.cloudliter.ui.profile

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import butterknife.BindView
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

    @JvmField
    @BindView(R.id.text_nickname)
    var nicknameTextView: TextView? = null

    @JvmField
    @BindView(R.id.text_signature)
    var signatureTextView: TextView? = null

    @JvmField
    @BindView(R.id.icon_gender)
    var genderIcon: ImageView? = null

    @JvmField
    @BindView(R.id.fab)
    var button: ExtendedFloatingActionButton? = null

    @JvmField
    @BindView(R.id.avatar)
    var avatarImageView: ImageView? = null

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

    @JvmField
    @BindView(R.id.logout)
    var logoutButton //登出按钮
            : Button? = null

    @JvmField
    @BindView(R.id.wordstag_layout)
    var wordsCloudView: WordsCloudView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        setWindowParams(true, true, false)
        super.onCreate(savedInstanceState)
        setToolbarActionBack(toolbar!!)
    }

    override fun getViewModelClass(): Class<ProfileViewModel> {
        return ProfileViewModel::class.java
    }

    override fun initViews() {
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
        viewModel!!.makeFriendsResult?.observe(this, Observer { booleanDataState->
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
                viewModel!!.startRefresh(intent.getStringExtra("id"))
            } else {
                Toast.makeText(applicationContext, "失败", Toast.LENGTH_SHORT).show()
            }
        })
        viewModel!!.deleteFriendResult?.observe(this, Observer { dataState: DataState<*> ->
            if (dataState.state === DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(), R.string.delete_friend_success, Toast.LENGTH_SHORT).show()
                viewModel!!.startRefresh(intent.getStringExtra("id"))
            } else {
                Toast.makeText(applicationContext, "失败", Toast.LENGTH_SHORT).show()
            }
        })
        viewModel!!.assignGroupResult?.observe(this, Observer { dataState: DataState<*> ->
            if (dataState.state === DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(), R.string.assign_group_success, Toast.LENGTH_SHORT).show()
                viewModel!!.startRefresh(intent.getStringExtra("id"))
            } else {
                Toast.makeText(applicationContext, R.string.fail, Toast.LENGTH_SHORT).show()
            }
        })
        viewModel!!.relationLiveData?.observe(this, Observer { userRelationDataState ->
            if (userRelationDataState!!.state== DataState.STATE.SUCCESS) {
                //是好友关系，则提供发消息入口
                relationCard!!.visibility = View.VISIBLE
                logoutButton!!.visibility = View.GONE
                button!!.setText(R.string.send_message)
                button!!.setIconResource(R.drawable.ic_baseline_message_24)
                button!!.isEnabled = true
                button!!.setOnClickListener { view: View? ->
                    if (viewModel!!.userRelation != null && viewModel!!.userProfile != null && viewModel!!.userLocal != null) {
                        ActivityUtils.startChatActivity(getThis(), viewModel!!.userProfile!!, viewModel!!.userRelation!!, viewModel!!.userLocal!!)
                    }
                }
                remarkText!!.text = userRelationDataState.data!!.remark
                remarkLayout!!.setOnClickListener { view: View? ->
                    val up = viewModel!!.userRelation
                    if (up != null) {
                        PopUpEditText()
                                .setTitle(R.string.prompt_set_remark)
                                .setText(up.remark)
                                .setOnConfirmListener (object:PopUpEditText.OnConfirmListener{
                                    override fun OnConfirm(text: String) {
                                        //控制viewModel发起更改昵称请求
                                        viewModel!!.startChangeRemark(text)
                                    }
                                })
                                .show(supportFragmentManager, "edit")
                    }
                }
                groupName!!.text = userRelationDataState.data!!.groupName
                groupLayout!!.setOnClickListener { view: View? ->
                    if (viewModel!!.userRelation != null) {
                        //Log.e("relation", String.valueOf(viewModel.getUserRelation()));
                        PickGroupDialog()
                                .setInitGroupId(viewModel!!.userRelation!!.groupId)
                                .setOnConfirmListener(object : PickGroupDialog.OnConfirmListener {
                                    override fun OnConfirmed(group: RelationGroup?) {
                                        group?.let { viewModel!!.startAssignGroup(it) }
                                    }
                                }).show(supportFragmentManager, "pick_group")
                    }
                }
                deleteLayout!!.setOnClickListener { view: View? ->
                    PopUpText() ///.setText(getString(R.string.attention_please))
                            .setTitle(R.string.attention_delete_friend)
                            .setOnConfirmListener { viewModel!!.startDeletingFriend(intent.getStringExtra("id")) }.show(supportFragmentManager, "attention")
                }
            } else if (userRelationDataState.state === DataState.STATE.NOT_EXIST) {
                //不是好友关系，则显示”添加好友“
                relationCard!!.visibility = View.GONE
                button!!.setText(R.string.make_friends)
                logoutButton!!.visibility = View.GONE
                button!!.isEnabled = true
                button!!.setIconResource(R.drawable.ic_baseline_person_add_24)
                button!!.setOnClickListener { view: View? ->
                    //通知viewModel进行添加好友请求
                    viewModel!!.startMakingFriends(intent.getStringExtra("id"))
                }
                remarkLayout!!.setOnClickListener(null)
            } else if (userRelationDataState.state === DataState.STATE.SPECIAL) {
                //是自己
                logoutButton!!.visibility = View.VISIBLE
                relationCard!!.visibility = View.GONE
                button!!.setText(R.string.edit_my_profile)
                button!!.isEnabled = true
                button!!.setIconResource(R.drawable.ic_baseline_edit_24)
                button!!.setOnClickListener { view: View? -> ActivityUtils.startMyProfileActivity(getThis()) }
                remarkLayout!!.setOnClickListener(null)
            }
        })
        wordsCloudView!!.setData(listOf(getString(R.string.no_word_cloud_yet)))
        viewModel!!.wordCloudLiveData?.observe(this, Observer { listDataState->
            if (listDataState.state === DataState.STATE.SUCCESS) {
                val tag = ArrayList<String>()
                for ((key) in listDataState.data!!) {
                    tag.add(key)
                }
                wordsCloudView!!.setData(tag)
            }
        })
        logoutButton!!.setOnClickListener { view1: View? ->
            //通知ViewModel登出
            viewModel!!.logout(this)
            finish()
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
            nicknameTextView!!.text = userInfo.nickname
            genderIcon!!.visibility = View.VISIBLE
            colorIcon!!.visibility = View.VISIBLE
            colorIconInner!!.visibility = View.VISIBLE
            if (TextUtils.isEmpty(userInfo.signature)) {
                signatureTextView!!.setText(R.string.place_holder_no_signature)
            } else {
                signatureTextView!!.text = userInfo.signature
            }
            colorIcon!!.setCardBackgroundColor(ColorUtils.getColorByEnum(getThis(), userInfo.color))
            colorIconInner!!.setCardBackgroundColor(ColorUtils.getColorByEnum(getThis(), userInfo.color))
            if (userInfo.gender == UserLocal.GENDER.MALE) {
                genderIcon!!.setImageResource(R.drawable.ic_male_blue_24)
            } else {
                genderIcon!!.setImageResource(R.drawable.ic_female_pink_24)
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_profile
    }
}
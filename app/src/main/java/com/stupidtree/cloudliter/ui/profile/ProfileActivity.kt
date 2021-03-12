package com.stupidtree.cloudliter.ui.profile

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.AppBarLayout
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.RelationGroup
import com.stupidtree.cloudliter.data.model.UserLocal
import com.stupidtree.cloudliter.data.model.UserProfile
import com.stupidtree.cloudliter.databinding.ActivityProfileBinding
import com.stupidtree.cloudliter.service.socket.SocketIOClientService
import com.stupidtree.cloudliter.ui.base.BaseActivityWithReceiver
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.group.pick.PickGroupDialog
import com.stupidtree.cloudliter.ui.widgets.PopUpEditText
import com.stupidtree.cloudliter.ui.widgets.PopUpText
import com.stupidtree.cloudliter.utils.ActivityUtils
import com.stupidtree.cloudliter.utils.ImageUtils
import com.stupidtree.cloudliter.utils.TextUtils
import java.util.*

/**
 * 其他用户（好友、搜索结果等）的资料页面Activity
 */
class ProfileActivity : BaseActivityWithReceiver<ProfileViewModel, ActivityProfileBinding>() {

    /**
     * 广播区
     */
    override var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.e("好友事件", intent.toString())
            startRefresh()
        }
    }

    override fun getIntentFilter(): IntentFilter {
        return IntentFilter(SocketIOClientService.RECEIVE_RELATION_EVENT)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setWindowParams(statusBar = true, darkColor = true, navi = false)
        super.onCreate(savedInstanceState)
        setToolbarActionBack(binding.toolbar)
    }


    override fun getViewModelClass(): Class<ProfileViewModel> {
        return ProfileViewModel::class.java
    }

    override fun initViews() {
        setUpLiveData()
        val initElevation = binding.avatarCard.cardElevation
        binding.appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            val percentage = -verticalOffset.toFloat() / binding.appbar.totalScrollRange.toFloat()
            binding.avatarCard.pivotY = binding.avatarCard.height.toFloat() * 0.5f
            binding.avatarCard.pivotX = binding.avatarCard.width.toFloat() * 0.3f
            binding.avatarCard.scaleX = 1 - percentage
            binding.avatarCard.scaleY = 1 - percentage
            binding.textNickname.alpha = 1 - 2.5f * percentage
            binding.textSignature.alpha = 1 - 2.5f * percentage
            binding.iconGender.alpha = 1 - 2.5f * percentage
            binding.avatarCard.cardElevation = initElevation * (1 - percentage)
        })
        binding.refresh.setColorSchemeColors(getColorPrimary())
        binding.refresh.setOnRefreshListener {
            startRefresh()
        }
        binding.logout.setOnClickListener {
            //通知ViewModel登出
            PopUpText().setTitle(R.string.logout_hint).setOnConfirmListener(
                    object : PopUpText.OnConfirmListener {
                        override fun OnConfirm() {
                            viewModel.logout(getThis())
                            finish()
                        }

                    }
            ).show(supportFragmentManager, "logout")
//            PopUpMultipleCheckableList<Int>(R.string.search_hint,R.string.logout,1)
//                    .setInitValues(listOf(0x1))
//                    .setListData(listOf("健全","视力障碍","听觉障碍","肢体障碍"),
//                    listOf(0,0x1,0x2,0x4))
//                    .setOnConfirmListener(object :PopUpMultipleCheckableList.OnConfirmListener<Int>{
//                        override fun onConfirm(titles: List<String?>, data: List<Int>) {
//                            Toast.makeText(applicationContext, "checked:$titles",Toast.LENGTH_SHORT).show()
//                        }
//
//                    }).show(supportFragmentManager,"")
        }
        binding.fab.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(getThis(), R.color.colorPrimary))


    }

    private fun setUpLiveData() {
        //为ViewModel中的各种数据设置监听
        viewModel.userProfileLiveData?.observe(this, { userProfileDataState ->
            binding.refresh.isRefreshing = false
            if (userProfileDataState?.state === DataState.STATE.SUCCESS) {
                //状态为成功，设置ui
                setProfileView(userProfileDataState.data)
            } else {
                //状态为失败，弹出错误
                Toast.makeText(getThis(), "获取出错", Toast.LENGTH_SHORT).show()
            }
        })
        viewModel.makeFriendsResult?.observe(this, { booleanDataState ->
            if (booleanDataState?.state === DataState.STATE.SUCCESS) {
                //状态为成功
                Toast.makeText(getThis(), R.string.send_request_success, Toast.LENGTH_SHORT).show()
                //finish();
            } else {
                Toast.makeText(getThis(), booleanDataState?.message, Toast.LENGTH_SHORT).show()
            }
        })
        viewModel.changeRemarkResult?.observe(this, { stringDataState ->
            if (stringDataState!!.state === DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(), R.string.avatar_change_success, Toast.LENGTH_SHORT).show()
                intent.getStringExtra("id")?.let { viewModel.startRefresh(it) }
            } else {
                Toast.makeText(applicationContext, "失败", Toast.LENGTH_SHORT).show()
            }
        })
        viewModel.deleteFriendResult?.observe(this, { dataState ->
            if (dataState.state === DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(), R.string.delete_friend_success, Toast.LENGTH_SHORT).show()
                intent.getStringExtra("id")?.let { viewModel.startRefresh(it) }
            } else {
                Toast.makeText(applicationContext, "失败", Toast.LENGTH_SHORT).show()
            }
        })
        viewModel.assignGroupResult?.observe(this, { dataState: DataState<*> ->
            if (dataState.state === DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(), R.string.assign_group_success, Toast.LENGTH_SHORT).show()
                intent.getStringExtra("id")?.let { viewModel.startRefresh(it) }
            } else {
                Toast.makeText(applicationContext, R.string.fail, Toast.LENGTH_SHORT).show()
            }
        })
        viewModel.relationLiveData?.observe(this, { userRelationDataState ->
            when {
                userRelationDataState!!.state == DataState.STATE.SUCCESS -> {
                    //是好友关系，则提供发消息入口
                    binding.fab
                    binding.relationCard.visibility = View.VISIBLE
                    binding.logout.visibility = View.GONE
                    binding.fab.setText(R.string.send_message)
                    binding.fab.setIconResource(R.drawable.ic_baseline_message_24)
                    binding.fab.isEnabled = true
                    binding.fab.setOnClickListener {
                        if (viewModel.getUserRelation() != null && viewModel.getUserProfile() != null && viewModel.getUserLocal() != null) {
                            ActivityUtils.startChatActivity(getThis(), viewModel.getUserProfile()!!, viewModel.getUserRelation()!!, viewModel.getUserLocal()!!)
                        }
                    }
                    binding.remark.text = userRelationDataState.data!!.remark
                    binding.remarkLayout.setOnClickListener {
                        val up = viewModel.getUserRelation()
                        if (up != null) {
                            PopUpEditText()
                                    .setTitle(R.string.prompt_set_remark)
                                    .setText(up.remark)
                                    .setOnConfirmListener(object : PopUpEditText.OnConfirmListener {
                                        override fun OnConfirm(text: String) {
                                            //控制viewModel发起更改昵称请求
                                            viewModel.startChangeRemark(text)
                                        }
                                    })
                                    .show(supportFragmentManager, "edit")
                        }
                    }
                    binding.groupName.text = userRelationDataState.data!!.groupName
                    binding.groupLayout.setOnClickListener {
                        if (viewModel.getUserRelation() != null) {
                            //Log.e("relation", String.valueOf(viewModel.getUserRelation()));
                            PickGroupDialog()
                                    .setInitGroupId(viewModel.getUserRelation()!!.groupId)
                                    .setOnConfirmListener(object : PickGroupDialog.OnConfirmListener {
                                        override fun onConfirmed(group: RelationGroup?) {
                                            group?.let { viewModel.startAssignGroup(it) }
                                        }
                                    }).show(supportFragmentManager, "pick_group")
                        }
                    }
                    binding.deleteLayout.setOnClickListener {
                        PopUpText() ///.setText(getString(R.string.attention_please))
                                .setTitle(R.string.attention_delete_friend)
                                .setOnConfirmListener(
                                        object : PopUpText.OnConfirmListener {
                                            override fun OnConfirm() {
                                                intent.getStringExtra("id")?.let { viewModel.startDeletingFriend(it) }
                                            }
                                        }
                                ).show(supportFragmentManager, "attention")
                    }
                }
                userRelationDataState.state === DataState.STATE.NOT_EXIST -> {
                    //不是好友关系，则显示”添加好友“
                    binding.relationCard.visibility = View.GONE
                    binding.fab.setText(R.string.make_friends)
                    binding.logout.visibility = View.GONE
                    binding.fab.isEnabled = true
                    binding.fab.setIconResource(R.drawable.ic_baseline_person_add_24)
                    binding.fab.setOnClickListener {
                        //通知viewModel进行添加好友请求
                        intent.getStringExtra("id")?.let { viewModel.startMakingFriends(it) }
                    }
                    binding.remarkLayout.setOnClickListener(null)
                }
                userRelationDataState.state === DataState.STATE.SPECIAL -> {
                    //是自己
                    if (intent.getBooleanExtra("showLogout", true)) {
                        binding.logout.visibility = View.VISIBLE
                    } else {
                        binding.logout.visibility = View.GONE
                    }
                    binding.wordstagLayout.setOnClickListener {
                        ActivityUtils.startWordCloudActivity(getThis())
                    }
                    binding.relationCard.visibility = View.GONE
                    binding.fab.setText(R.string.edit_my_profile)
                    binding.fab.isEnabled = true
                    binding.fab.setIconResource(R.drawable.ic_baseline_edit_24)
                    binding.fab.setOnClickListener { ActivityUtils.startMyProfileActivity(getThis()) }
                    binding.remarkLayout.setOnClickListener(null)
                }
            }
        })
        viewModel.wordCloudLiveData?.observe(this, { listDataState ->
            if (listDataState.state === DataState.STATE.SUCCESS) {//成功
                val tag = ArrayList<String>()
                if (listDataState.data!!.isEmpty()) {//没有词云
                    for (i in 0 until 5) tag.add(getString(R.string.no_word_cloud_yet))
                    binding.wordstagLayout.alpha = 0.3f
                } else {
                    for ((key) in listDataState.data!!) {
                        tag.add(key)
                    }
                    binding.wordstagLayout.alpha = 1f
                }
                binding.wordstagLayout.setTags(tag)
                binding.wordstagLayout.contentDescription = tag.toString()
            } else if (listDataState.state == DataState.STATE.SPECIAL) {//词云为私密
                val tag = ArrayList<String>()
                for (i in 0 until 5) tag.add(getString(R.string.private_word_cloud))
                binding.wordstagLayout.setTags(tag)
                binding.wordstagLayout.alpha = 0.3f
                binding.wordstagLayout.contentDescription = getString(R.string.word_cloud_accessibility)
            }
        })
    }

    override fun onStart() {
        super.onStart()
        startRefresh()
    }

    private fun startRefresh() {
        val id = intent.getStringExtra("id")
        if (id != null) {
            viewModel.startRefresh(id)
            binding.refresh.isRefreshing = true
        }
    }

    /**
     * 根据用户资料Model设置UI
     *
     * @param userInfo 用户资料对象
     */
    private fun setProfileView(userInfo: UserProfile?) {
        if (userInfo != null) {
            ImageUtils.loadAvatarNoCacheInto(getThis(), userInfo.avatar,binding.avatar)
            binding.textUsername.text = userInfo.username
            binding.textNickname.text = userInfo.nickname
            binding.iconGender.visibility = View.VISIBLE
            if (TextUtils.isEmpty(userInfo.signature)) {
                binding.textSignature.setText(R.string.place_holder_no_signature)
            } else {
                binding.textSignature.text = userInfo.signature
            }
            if (userInfo.gender == UserLocal.GENDER.MALE) {
                binding.iconGender.setImageResource(R.drawable.ic_male_blue_24)
                binding.iconGender.contentDescription = getString(R.string.male)
            } else {
                binding.iconGender.setImageResource(R.drawable.ic_female_pink_24)
                binding.iconGender.contentDescription = getString(R.string.female)
            }
            if (userInfo.wordCloudPrivate) {
                binding.lock.visibility = View.VISIBLE
            } else {
                binding.lock.visibility = View.GONE
            }
        }
    }


    override fun initViewBinding(): ActivityProfileBinding {
        return ActivityProfileBinding.inflate(layoutInflater)
    }
}
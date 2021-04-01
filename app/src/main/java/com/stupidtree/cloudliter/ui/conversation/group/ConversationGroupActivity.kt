package com.stupidtree.cloudliter.ui.conversation.group

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.Conversation
import com.stupidtree.cloudliter.databinding.ActivityConversationGroupBinding
import com.stupidtree.cloudliter.utils.ActivityUtils
import com.stupidtree.cloudliter.utils.ImageUtils
import com.stupidtree.component.data.DataState
import com.stupidtree.style.base.BaseActivity
import com.stupidtree.style.base.BaseListAdapter
import com.stupidtree.style.picker.FileProviderUtils
import com.stupidtree.style.picker.GalleryPicker
import com.stupidtree.style.widgets.PopUpEditText
import com.stupidtree.style.widgets.PopUpText
import java.text.SimpleDateFormat
import java.util.*

class ConversationGroupActivity : BaseActivity<ConversationGroupViewModel, ActivityConversationGroupBinding>() {

    lateinit var listAdapter: GroupMemberAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolbarActionBack(binding.toolbar)

    }

    override fun getViewModelClass(): Class<ConversationGroupViewModel> {
        return ConversationGroupViewModel::class.java
    }

    override fun initViews() {
        setUpLiveData()
        listAdapter = GroupMemberAdapter(this, mutableListOf())
        listAdapter.setOnItemClickListener(object : BaseListAdapter.OnItemClickListener<GroupMemberEntity> {
            override fun onItemClick(data: GroupMemberEntity, card: View?, position: Int) {
                ActivityUtils.startProfileActivity(getThis(), data.userId)
            }
        })

        binding.membersList.adapter = listAdapter
        binding.membersList.layoutManager = GridLayoutManager(this, 4)
        binding.refresh.setColorSchemeColors(getColorPrimary())
        binding.refresh.setOnRefreshListener {
            startRefresh()
        }
        binding.groupNameLayout.setOnClickListener {
            viewModel.groupInfoLiveData.value?.data?.let {
                PopUpEditText().setTitle(R.string.edit_group_name).setText(it.name)
                        .setOnConfirmListener(object : PopUpEditText.OnConfirmListener {
                            override fun OnConfirm(text: String) {
                                viewModel.startRenameGroup(text)
                            }

                        }).show(supportFragmentManager, "rename")
            }

        }
        binding.avatarLayout.setOnClickListener { GalleryPicker.choosePhoto(getThis(), false) }
        binding.optionLayout.setOnClickListener {
            if(viewModel.isMeTheMaster()){
                PopUpText()
                        .setTitle(R.string.sure_to_destroy_group)
                        .setOnConfirmListener(object:PopUpText.OnConfirmListener{
                            override fun OnConfirm() {
                               viewModel.startDestroyGroup()
                            }

                        }).show(supportFragmentManager,"confirm")
            }else{
                PopUpText()
                        .setTitle(R.string.sure_to_quit_group)
                        .setOnConfirmListener(object:PopUpText.OnConfirmListener{
                            override fun OnConfirm() {
                                viewModel.startQuitGroup()
                            }

                        }).show(supportFragmentManager,"confirm")
            }
        }
    }

    private fun setUpLiveData() {
        viewModel.conversationLiveData.observe(this, { conversationDataState: DataState<Conversation?> ->
            if (conversationDataState.state === DataState.STATE.SUCCESS) {
                conversationDataState.data?.let { conversation ->
                    binding.metDate.text = SimpleDateFormat(getString(R.string.date_format_3), Locale.getDefault()).format(conversation.createdAt?.time)
                }
            }
        })
        viewModel.wordCloudLiveData.observe(this, { listDataState ->
            binding.refresh.isRefreshing = false
            if (listDataState.state === DataState.STATE.SUCCESS) {
                val tag = ArrayList<String>()
                if (listDataState.data!!.isEmpty()) {//没有词云
                    for (i in 0 until 5) tag.add(getString(R.string.no_word_cloud_yet))
                    binding.wordCloud.alpha = 0.3f
                } else {
                    for ((key) in listDataState.data!!) {
                        tag.add(key)
                    }
                    binding.wordCloud.alpha = 1f
                }
                binding.wordCloud.setTags(tag)
                binding.wordCloud.contentDescription = tag.toString()
            }
        })
        viewModel.groupMembersLiveData.observe(this) {
            if (it.state == DataState.STATE.SUCCESS) {
                it.data?.let { it1 -> listAdapter.notifyItemChangedSmooth(it1,false) }
            }
        }
        viewModel.renameGroupResult.observe(this) {
            if (it.state == DataState.STATE.SUCCESS) {
                startRefresh()
            }
        }
        viewModel.quitGroupResult.observe(this){
            if(it.state==DataState.STATE.SUCCESS){
                finish()
            }else{
                Toast.makeText(getThis(),R.string.fail,Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.destroyGroupResult.observe(this){
            if(it.state==DataState.STATE.SUCCESS){
                finish()
            }else{
                Toast.makeText(getThis(),R.string.fail,Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.changeAvatarResult.observe(this) {
            if (it.state == DataState.STATE.SUCCESS) {
                startRefresh()
            }
        }
        viewModel.groupInfoLiveData.observe(this) {
            if (it.state == DataState.STATE.SUCCESS) {
                it.data?.let {
                    if(viewModel.isMeTheMaster()){
                        binding.optionText.setText(R.string.destroy_group_chat)
                    }else{
                        binding.optionText.setText(R.string.quit_group_chat)
                    }
                    binding.groupName.text = it.name
                    ImageUtils.loadAvatarInto(getThis(), it.avatar, binding.avatar)
                }
            }
        }
    }

    private fun startRefresh() {
        intent.getStringExtra("conversationId")?.let {
            intent.getStringExtra("groupId")?.let { groupId ->
                viewModel.startRefresh(it, groupId = groupId)
            }
        }
        binding.refresh.isRefreshing = true
    }

    override fun onStart() {
        super.onStart()
        startRefresh()
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

            GalleryPicker.RC_CHOOSE_PHOTO -> {
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
            GalleryPicker.RC_CROP_PHOTO -> {                //裁剪图片返回，此时通知viewModel请求更改头像
                val path = GalleryPicker.getCroppedCacheDir(this)?.let { FileProviderUtils.getFilePathByUri(this, it) }
                path?.let { viewModel.startChangeAvatar(it) }
            }
        }
    }

    override fun initViewBinding(): ActivityConversationGroupBinding {
        return ActivityConversationGroupBinding.inflate(layoutInflater)
    }
}
package com.stupidtree.cloudliter.ui.conversation

import android.os.Bundle
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.Conversation
import com.stupidtree.cloudliter.databinding.ActivityConversationBinding
import com.stupidtree.cloudliter.ui.base.BaseActivity
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.utils.ActivityUtils
import com.stupidtree.cloudliter.utils.ImageUtils
import com.stupidtree.cloudliter.utils.TextUtils
import java.text.SimpleDateFormat
import java.util.*

class ConversationActivity : BaseActivity<ConversationViewModel,ActivityConversationBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolbarActionBack(binding.toolbar)
        setWindowParams(statusBar = true, darkColor = true, navi = false)
    }

    override fun getViewModelClass(): Class<ConversationViewModel> {
        return ConversationViewModel::class.java
    }

    override fun initViews() {
        setUpLiveData()
        binding.userLayout.setOnClickListener {
            val id = intent.getStringExtra("friendId")
            if (!TextUtils.isEmpty(id)) {
                ActivityUtils.startProfileActivity(getThis(), id!!)
            }
        }
        binding.refresh.setColorSchemeColors(getColorPrimary())
        binding.refresh.setOnRefreshListener {
            startRefresh()
        }
    }

    private fun setUpLiveData(){
        viewModel.conversationLiveData?.observe(this, { conversationDataState: DataState<Conversation?> ->
            if (conversationDataState.state === DataState.STATE.SUCCESS) {
                conversationDataState.data?.let { setUpPage(it) }
            }
        })
        viewModel.wordCloudLiveData?.observe(this, { listDataState ->
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
    }

    private fun startRefresh(){
        intent.getStringExtra("friendId")?.let { viewModel.startRefresh(it) }
        binding.refresh.isRefreshing = true
    }

    override fun onStart() {
        super.onStart()
        startRefresh()
    }

    private fun setUpPage(conversation: Conversation) {
        conversation.friendAvatar?.let { ImageUtils.loadAvatarNoCacheInto(this, it, binding.avatar) }
        if (TextUtils.isEmpty(conversation.friendRemark)) {
            binding.remark.text = conversation.friendNickname
        } else {
            binding.remark.text = conversation.friendRemark
        }
        binding.metDate.text = SimpleDateFormat(getString(R.string.date_format_3), Locale.getDefault()).format(conversation.createdAt?.time)
        binding.lastChatDate.text = SimpleDateFormat(getString(R.string.date_format_3), Locale.getDefault()).format(conversation.updatedAt?.time)
        binding.sinceMet.text = ((System.currentTimeMillis()- (conversation.createdAt?.time ?:System.currentTimeMillis() ))/(1000*60*60*24)).toString()
    }

    override fun initViewBinding(): ActivityConversationBinding {
       return ActivityConversationBinding.inflate(layoutInflater)
    }
}
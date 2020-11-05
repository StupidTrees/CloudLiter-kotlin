package com.stupidtree.cloudliter.ui.conversation

import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import butterknife.BindView
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.Conversation
import com.stupidtree.cloudliter.ui.base.BaseActivity
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.widgets.WordsCloudView
import com.stupidtree.cloudliter.utils.ActivityUtils
import com.stupidtree.cloudliter.utils.ImageUtils
import com.stupidtree.cloudliter.utils.TextUtils
import java.util.*

class ConversationActivity : BaseActivity<ConversationViewModel>() {
    @JvmField
    @BindView(R.id.avatar)
    var friendAvatarImage: ImageView? = null

    @JvmField
    @BindView(R.id.remark)
    var friendRemarkText: TextView? = null

    @JvmField
    @BindView(R.id.toolbar)
    var toolbar: Toolbar? = null

    @JvmField
    @BindView(R.id.user_layout)
    var userLayout: ViewGroup? = null

    @JvmField
    @BindView(R.id.word_cloud)
    var wordsCloudView: WordsCloudView? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_conversation
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolbarActionBack(toolbar!!)
        setWindowParams(true, true, false)
    }

    override fun getViewModelClass(): Class<ConversationViewModel> {
        return ConversationViewModel::class.java
    }

    override fun initViews() {
        viewModel!!.conversationLiveData?.observe(this, Observer { conversationDataState: DataState<Conversation?> ->
            if (conversationDataState.state === DataState.STATE.SUCCESS) {
                conversationDataState.data?.let { setUpPage(it) }
            }
        })
        wordsCloudView!!.setData(listOf(getString(R.string.no_word_cloud_yet)))
        viewModel!!.wordCloudLiveData?.observe(this, Observer { hashMapDataState ->
            if (hashMapDataState.state === DataState.STATE.SUCCESS) {
                val tag = ArrayList<String>()
                for ((key) in hashMapDataState.data!!) {
                    tag.add(key)
                }
                wordsCloudView!!.setData(tag)
            }
        })
        userLayout!!.setOnClickListener {
            val id = intent.getStringExtra("friendId")
            if (!TextUtils.isEmpty(id)) {
                ActivityUtils.startProfileActivity(getThis(), id!!)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        intent.getStringExtra("friendId")?.let { viewModel!!.startRefresh(it) }

    }

    private fun setUpPage(conversation: Conversation) {
        conversation.friendAvatar?.let { ImageUtils.loadAvatarNoCacheInto(this, it, friendAvatarImage!!) }
        if (TextUtils.isEmpty(conversation.friendRemark)) {
            friendRemarkText!!.text = conversation.friendNickname
        } else {
            friendRemarkText!!.text = conversation.friendRemark
        }
    }
}
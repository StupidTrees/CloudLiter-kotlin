package com.stupidtree.cloudliter.ui.chat

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.ChatMessage
import com.stupidtree.cloudliter.data.model.Conversation
import com.stupidtree.cloudliter.data.model.UserLocal
import com.stupidtree.cloudliter.data.model.Yunmoji
import com.stupidtree.cloudliter.databinding.ActivityChatBinding
import com.stupidtree.cloudliter.ui.base.BaseActivity
import com.stupidtree.cloudliter.ui.base.BaseListAdapter
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.chat.detail.PopUpTextMessageDetail
import com.stupidtree.cloudliter.ui.imagedetect.ImageDetectBottomFragment
import com.stupidtree.cloudliter.ui.myprofile.MyProfileActivity
import com.stupidtree.cloudliter.ui.widgets.PopUpText
import com.stupidtree.cloudliter.utils.*
import java.util.*

/**
 * 对话窗口
 */
@SuppressLint("NonConstantResourceId")
class ChatActivity : BaseActivity<ChatViewModel, ActivityChatBinding>() {

    //输入状态：语音或文字
    private var textInput: Boolean = true

    enum class PANEL { EMOTION, VOICE, COLLAPSE }

    //底部展开栏状态
    var bottomPanelState: PANEL = PANEL.COLLAPSE

    /**
     * 适配器
     */
    private lateinit var listAdapter: ChatListAdapter
    private lateinit var yunmojiListAdapter: YunmojiListAdapter //表情列表适配器


    private val mHandler = Handler(Looper.getMainLooper())
    private val heartBeatRunnable: Runnable by lazy {
        object : Runnable {
            override fun run() {
                viewModel.getIntoConversation(getThis())
                viewModel.markAllRead(getThis())
                //定时对长连接进行心跳检测
                Log.e("对话：心跳声明", "--")
                mHandler.postDelayed(this, 5 * 1000)
            }
        }
    }

    /**
     * 语音控制
     */
    lateinit var voiceHelper: AudioRecordHelper
    lateinit var audioPlayHelper: AudioPlayHelper


    override fun getViewModelClass(): Class<ChatViewModel> {
        return ChatViewModel::class.java
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setWindowParams(statusBar = true, darkColor = true, navi = false)
    }


    /**
     * 生命周期事件区
     */
    //启动时，绑定服务
    override fun onStart() {
        super.onStart()
        viewModel.bindService(this)
        refreshInputLayout()
        if (intent.extras != null && intent.extras!!.getSerializable("conversation") != null) {
            val conversation = intent.extras!!.getSerializable("conversation") as Conversation?
            if (conversation != null) {
                if (viewModel.conversationId == null) {
                    viewModel.setConversation(conversation)
                    viewModel.markAllRead(getThis())
                    viewModel.fetchHistoryData() //初次进入时，重新加载聊天记录
                } else {
                    viewModel.fetchNewData() //非第一次进入
                }
            }
        }
        viewModel.getIntoConversation(this)
        mHandler.postDelayed(heartBeatRunnable, 5 * 1000)
        viewModel.refreshConversation()
    }

    //更新Intent时（更换聊天对象），刷新列表
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.extras != null && intent.extras!!.getSerializable("conversation") != null) {
            val conversation = intent.extras!!.getSerializable("conversation") as Conversation?
            if (conversation != null) {
                if (viewModel.conversationId != conversation.id) {
                    viewModel.setConversation(conversation)
                    listAdapter.clear()
                    viewModel.fetchHistoryData() //变更对话时，重新加载聊天记录
                }
                viewModel.setConversation(conversation)
            }
        }
    }

    //每次页面失去焦点时，退出对话，解绑服务
    override fun onStop() {
        super.onStop()
        Log.e("ChatActivity", "onStop")
        viewModel.leftConversation(this)
        viewModel.unbindService(this)
        mHandler.removeCallbacks(heartBeatRunnable)
    }


    // 初始化语音输入与播放
    private fun setUpAudio() {
        voiceHelper = AudioRecordHelper.getInstance(this, object : AudioRecordHelper.OnRecordListener {
            override fun onRecordStart(exception: Exception?) {
                if (exception == null) {
                    voiceRecordingStarted()
                } else {
                    refreshVoiceBubble()
                    //Toast.makeText(getThis(), "录音失败", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onRecordStop(path: String?, seconds: Int, exception: Exception?) {
                if (exception == null) {
                    voiceMessageReady()
                } else {
                    refreshVoiceBubble()
                    // Toast.makeText(getThis(), "录音结束失败", Toast.LENGTH_SHORT).show()
                }

            }

            override fun onRecordTimeTick(count: Int, exception: Exception?) {
                // exception?.printStackTrace()
                binding.recorfingText.text = TextUtils.getVoiceTimeText(getThis(), count)
            }
        })
        audioPlayHelper = AudioPlayHelper(this, object : AudioPlayHelper.VoicePlayListener {
            override fun onStartPlaying(playingId: String) {
                listAdapter.changeAudioState(binding.list, playingId, ChatMessage.VOICE_STATE.PLAYING)
            }

            override fun onToggle(playingId: String, toStart: Boolean) {
                listAdapter.changeAudioState(binding.list, playingId, if (toStart) ChatMessage.VOICE_STATE.PLAYING else ChatMessage.VOICE_STATE.PAUSED)
            }

            override fun onPlayingFinished(playingId: String) {
                listAdapter.changeAudioState(binding.list, playingId, ChatMessage.VOICE_STATE.STOPPED)
            }

        })
    }

    /**
     * 初始化View区
     */
    override fun initViews() {
        setUpAudio()
        setUpToolbar()
        setUpMessageList()
        setUpYunmojiList()
        setUpButtons()
        viewModel.conversation.observe(this, { conversation: Conversation? ->
            conversation?.let { setConversationViews(it) }
        })
        viewModel.listData.observe(this, { listDataState: DataState<List<ChatMessage>?> ->
            binding.refresh.isRefreshing = false
            //Log.e("ChatActivity列表变动", listDataState.listAction.toString() + "》》" + listDataState.data!!.size)
            if (listDataState.state === DataState.STATE.SUCCESS) {
                //添加一条消息
                if (listDataState.listAction === DataState.LIST_ACTION.APPEND_ONE) {
                    val theOne = listDataState.data!![0]
                    //对方发出的，标为已读
                    if (viewModel.myId != theOne.fromId) {
                        viewModel.markRead(this, theOne)
                    }
                    listAdapter.notifyItemsAppended(listDataState.data!!)
                    if (listAdapter.itemCount > 0) {
                        binding.list.smoothScrollToPosition(listAdapter.itemCount - 1)
                    }
                } else if (listDataState.listAction === DataState.LIST_ACTION.APPEND) {
                    listAdapter.notifyItemsAppended(listDataState.data!!)
                    if (listAdapter.itemCount > 0) {
                        binding.list.smoothScrollToPosition(listAdapter.itemCount - 1)
                    }
                } else if (listDataState.listAction === DataState.LIST_ACTION.PUSH_HEAD) {
                    //下拉加载更多
                    if (!listDataState.isRetry) { //第一次获取，本地数据
                        listAdapter.notifyItemsPushHead(listDataState.data!!)
                        if (listDataState.data!!.isNotEmpty()) {
                            binding.list.smoothScrollBy(0, -150)
                        }
                    } else { //获取到网络数据，刷新对应项
                        listDataState.data.let {
                            listAdapter.notifyHeadItemsUpdated(it!!)
                        }
                    }
                } else {
                    listAdapter.notifyItemChangedSmooth(listDataState.data!!)
                    if (listAdapter.itemCount > 0) {
                        binding.list.smoothScrollToPosition(listAdapter.itemCount - 1)
                    }
                }
            }
        })
        viewModel.friendStateLiveData.observe(this, { friendStateDataState: DataState<FriendState> ->
            if (friendStateDataState.state === DataState.STATE.SUCCESS) {
                when (friendStateDataState.data!!.state) {
                    FriendState.STATE.ONLINE -> {
                        binding.state.setText(R.string.online)
                        binding.state.setTextColor(getColorPrimary())
                        binding.stateIcon.setImageResource(R.drawable.element_round_primary)
                        binding.stateBar.setBackgroundResource(R.drawable.element_rounded_bar_primary)
                    }
                    FriendState.STATE.YOU -> {
                        binding.state.setText(R.string.with_you)
                        binding.state.setTextColor(getColorPrimary())
                        binding.stateIcon.setImageResource(R.drawable.element_round_primary)
                        binding.stateBar.setBackgroundResource(R.drawable.element_rounded_bar_primary)
                    }
                    FriendState.STATE.OTHER -> {
                        binding.state.setText(R.string.with_other)
                        binding.state.setTextColor(getColorPrimary())
                        binding.stateIcon.setImageResource(R.drawable.element_round_primary)
                        binding.stateBar.setBackgroundResource(R.drawable.element_rounded_bar_primary)
                    }
                    FriendState.STATE.OFFLINE -> {
                        binding.state.setText(R.string.offline)
                        binding.state.setTextColor(getTextColorSecondary())
                        binding.stateIcon.setImageResource(R.drawable.element_round_grey)
                        binding.stateBar.setBackgroundResource(R.drawable.element_rounded_bar_grey)
                    }
                }
            }
        })
        viewModel.getImageSentResult().observe(this, { })
        viewModel.getVoiceSentResult().observe(this, { })
        //消息成功发送后反馈给列表
        viewModel.messageSentState.observe(this, { chatMessageDataState ->
            if (chatMessageDataState!!.state === DataState.STATE.SUCCESS) {
                binding.list.let { listAdapter.messageSent(it, chatMessageDataState!!.data!!) }
            }
        })
        //消息被对方读取后反馈给列表
        viewModel.messageReadState.observe(this, { messageReadNotificationDataState ->
            Log.e("messageRead", messageReadNotificationDataState!!.data.toString())
            if (messageReadNotificationDataState.state === DataState.STATE.SUCCESS) {
                binding.list.let { listAdapter.messageRead(it, messageReadNotificationDataState.data!!) }
            }
        })
        viewModel.ttsResultLiveData.observe(this) {
            listAdapter.changeTTSState(binding.list, it.data, if (it.state == DataState.STATE.SUCCESS)
                ChatMessage.TTS_STATE.SUCCESS else ChatMessage.TTS_STATE.FAILED)

        }
    }

    override fun onBackPressed() {
        if (bottomPanelState != PANEL.COLLAPSE) {
            collapseBottomPanel(collapseKeyboard = true, animate = true)
        } else {
            super.onBackPressed()
        }
    }

    //设置toolbar
    private fun setUpToolbar() {
        binding.back.setOnClickListener { onBackPressed() }
        binding.menu.setOnClickListener { ActivityUtils.startConversationActivity(getThis(), viewModel.friendId!!) }
    }

    //初始化聊天列表
    @SuppressLint("ClickableViewAccessibility")
    private fun setUpMessageList() {
        listAdapter = ChatListAdapter(this, LinkedList())
        binding.list.adapter = listAdapter
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true //从底部往上堆
        binding.list.layoutManager = layoutManager
        //触摸recyclerView的监听
        binding.list.setOnTouchListener { _: View?, _: MotionEvent? ->
            collapseBottomPanel(collapseKeyboard = true, animate = true)
            false
        }
        binding.refresh.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent)
        binding.refresh.setOnRefreshListener { viewModel.loadMore() }
        listAdapter.setOnItemLongClickListener(object : BaseListAdapter.OnItemLongClickListener<ChatMessage> {
            override fun onItemLongClick(data: ChatMessage, view: View?, position: Int): Boolean {
                if (data.getTypeEnum() == ChatMessage.TYPE.TXT && !data.isTimeStamp) {
                    PopUpTextMessageDetail().setChatMessage(data)
                            .show(supportFragmentManager, "detail")
                } else if (data.getTypeEnum() == ChatMessage.TYPE.IMG && !data.isTimeStamp) {
                    data.content?.let {
                        ActivityUtils.startImageDetectionActivity(getThis(), ImageUtils.getChatMessageImageUrl(it), data)
                    }
                }
                return true
            }
        })
        listAdapter.setOnItemClickListener(object : BaseListAdapter.OnItemClickListener<ChatMessage> {
            override fun onItemClick(data: ChatMessage, card: View?, position: Int) {
                if (data.getTypeEnum() == ChatMessage.TYPE.IMG && !data.isTimeStamp) {
                    val urls = listAdapter.imageUrls
                    ActivityUtils.showMultipleImages(getThis(), urls, urls.indexOf(data.content?.let { ImageUtils.getChatMessageImageUrl(it) })
                    )
                } else if (data.getTypeEnum() == ChatMessage.TYPE.VOICE && !data.isTimeStamp) {
                    if (audioPlayHelper.playingId != null) {
                        if (audioPlayHelper.playingId != data.id) {//有其他消息正在播放
                            listAdapter.changeAudioState(binding.list, audioPlayHelper.playingId!!, ChatMessage.VOICE_STATE.STOPPED)
                            data.content?.let { audioPlayHelper.play(data.id, data.content!!) }
                        } else {//正是在下在播放
                            audioPlayHelper.toggle()
                        }
                    } else {
                        data.content?.let { audioPlayHelper.play(data.id, data.content!!) }
                    }

                }
            }

        })
        listAdapter.onTTSButtonClickListener = object : ChatListAdapter.OnTTSButtonClickListener {
            override fun onClick(v: View, data: ChatMessage, position: Int) {
                listAdapter.changeTTSState(binding.list,data,ChatMessage.TTS_STATE.PROCESSING)
                viewModel.startTTS(data)
            }

        }
    }

    //初始化各种按钮
    @SuppressLint("ClickableViewAccessibility")
    private fun setUpButtons() {
        //点击发送
        binding.send.setOnClickListener {
            if (voiceHelper.state == AudioRecordHelper.STATE.DONE && !textInput) {
                voiceMessageSend()
            } else {
                if (!TextUtils.isEmpty(binding.input.text.toString())) {
                    viewModel.sendMessage(binding.input.text.toString())
                    binding.input.setText("")
                }

            }
        }
        binding.emotion.setOnClickListener {
            if (bottomPanelState == PANEL.EMOTION) {
                collapseBottomPanel(collapseKeyboard = true, animate = true)
            } else {
                expandBottomPanel(PANEL.EMOTION)
            }
        }


        binding.image.setOnClickListener { GalleryPicker.choosePhoto(getThis(), false) }

        binding.switchButton.setOnClickListener {
            textInput = !textInput
            refreshInputLayout()
        }
        binding.voiceButton.setOnTouchListener { _, p1 ->
            if (p1 != null) {
                //Log.e("action",p1.action.toString())
                if (p1.action == MotionEvent.ACTION_DOWN) {
                    voiceHelper.startRecord()
                    binding.voiceButtonHint.setText(R.string.voice_button_hint_pressed)
                } else if (p1.action == MotionEvent.ACTION_UP || p1.action == 3) {
                    voiceHelper.stopRecord()
                    binding.voiceButtonHint.setText(R.string.voice_button_hint_press)
                }
            }
            false
        }

        binding.voiceCancel.setOnClickListener {
            cancelVoiceMessage()
        }

        binding.input.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                if (bottomPanelState != PANEL.COLLAPSE) {
                    lockContentHeight()
                    collapseBottomPanel(collapseKeyboard = false, animate = true)
                    unlockContentHeight()
                }

            }
            false
        }
    }


    //初始化表情列表
    private fun setUpYunmojiList() {
        val ymList = ArrayList<Yunmoji>()
        val desc = resources.getStringArray(R.array.yunmoji_descriptions)
        for (i in 1..20) {
            val item = Yunmoji(resources.getIdentifier(String.format(Locale.getDefault(), "yunmoji_y%03d", i), "drawable", packageName), desc[i - 1])
            ymList.add(item)
        }

        binding.yunmojiList.layoutManager = GridLayoutManager(this, 6)
        yunmojiListAdapter = YunmojiListAdapter(this, ymList)
        binding.yunmojiList.adapter = yunmojiListAdapter
        val yunmojiOnItemClickListener: BaseListAdapter.OnItemClickListener<Yunmoji> =
                object : BaseListAdapter.OnItemClickListener<Yunmoji> {
                    override fun onItemClick(data: Yunmoji, card: View?, position: Int) {
                        binding.input.append(data.getLastName(position))
                    }
                }
        yunmojiListAdapter.setOnItemClickListener(yunmojiOnItemClickListener)
    }

    /**
     * 动作区
     */
    //设置聊天信息显示
    private fun setConversationViews(conversation: Conversation) {
        if (TextUtils.isEmpty(conversation.friendRemark)) {
            binding.title.text = conversation.friendNickname
        } else {
            binding.title.text = conversation.friendRemark
        }

        binding.accessibilityIcon.visibility = View.GONE
        binding.accessibilityIcon2.visibility = View.GONE
        binding.accessibilityIcon3.visibility = View.GONE
        if (conversation.friendType == 0) {
            binding.typeIcon.visibility = View.GONE
        } else {
            binding.typeIcon.visibility = View.VISIBLE
            when (conversation.friendType) {
                1 -> {
                    binding.accessibilityIcon.visibility = View.VISIBLE
                }
                2 -> {
                    binding.accessibilityIcon2.visibility = View.VISIBLE
                }
                3 -> {
                    binding.accessibilityIcon.visibility = View.VISIBLE
                    binding.accessibilityIcon2.visibility = View.VISIBLE
                }
                4 -> {
                    binding.accessibilityIcon3.visibility = View.VISIBLE
                }
                5 -> {
                    binding.accessibilityIcon.visibility = View.VISIBLE
                    binding.accessibilityIcon3.visibility = View.VISIBLE
                }
                6 -> {
                    binding.accessibilityIcon2.visibility = View.VISIBLE
                    binding.accessibilityIcon3.visibility = View.VISIBLE
                }
                7 -> {
                    binding.accessibilityIcon.visibility = View.VISIBLE
                    binding.accessibilityIcon2.visibility = View.VISIBLE
                    binding.accessibilityIcon3.visibility = View.VISIBLE
                }
                else -> {
                    binding.typeIcon.visibility = View.GONE
                }
            }
        }
    }


    //刷新输入区状态
    private fun refreshInputLayout() {
        if (textInput) {
            collapseBottomPanel(collapseKeyboard = true, animate = true)
            refreshVoiceBubble()
            binding.switchButton.contentDescription = getString(R.string.switch_to_voice)
            binding.switchIcon.setImageResource(R.drawable.ic_voice_wave)
            binding.input.visibility = View.VISIBLE
        } else {
            expandBottomPanel(PANEL.VOICE)
            binding.switchButton.contentDescription = getString(R.string.switch_to_text)
            binding.voiceButtonHint.setText(R.string.voice_button_hint_press)
            binding.switchIcon.setImageResource(R.drawable.ic_baseline_keyboard_24)
            refreshVoiceBubble()
        }
    }


    //语音消息已经开始录制
    private fun voiceRecordingStarted() {
        binding.voiceBubble.visibility = View.VISIBLE
        binding.voiceCancel.visibility = View.GONE
        binding.input.visibility = View.GONE
    }

    //语音消息录制完成，准备发送
    private fun voiceMessageReady() {
        if (!TextUtils.isEmpty(voiceHelper.filePath)) {
            binding.voiceCancel.visibility = View.VISIBLE
        } else {
            refreshVoiceBubble()
        }

    }

    //发送语音消息
    private fun voiceMessageSend() {
        if (!TextUtils.isEmpty(voiceHelper.filePath)) {
//            viewModel.sendVoiceMessage(voiceHelper.filePath, voiceHelper.timeCount)
            voiceHelper.let {
                viewModel.conversation.value?.let { conversation ->
                    if (conversation.friendType == 2 || conversation.friendType == 3 || conversation.friendType == 6 || conversation.friendType == 7) {
                        //TODO
                        viewModel.sendVoiceMessage(it.filePath, it.timeCount)
                    } else {
                        viewModel.sendVoiceMessage(it.filePath, it.timeCount)
                    }
                }
            }
        }
        voiceHelper.sendRecord()
        binding.voiceCancel.visibility = View.GONE
        refreshVoiceBubble()
    }

    //取消发送语音消息
    private fun cancelVoiceMessage() {
        if (voiceHelper.state == AudioRecordHelper.STATE.DONE) {
            voiceHelper.cancelRecord()
            refreshVoiceBubble()
        }
    }

    //重置语音输入气泡状态
    private fun refreshVoiceBubble() {
//
        binding.recorfingText.text = TextUtils.getVoiceTimeText(this, voiceHelper.timeCount)
        if (voiceHelper.state == AudioRecordHelper.STATE.DONE && voiceHelper.timeCount > 0) {
            binding.voiceCancel.visibility = View.VISIBLE
            if (textInput) {
                binding.voiceBubble.visibility = View.GONE
            } else {
                binding.voiceBubble.visibility = View.VISIBLE
                binding.input.visibility = View.GONE
            }
        } else {
            binding.voiceBubble.visibility = View.GONE
            binding.voiceCancel.visibility = View.GONE
            binding.input.visibility = View.VISIBLE
        }
    }


    //收起底部
    private fun collapseBottomPanel(collapseKeyboard: Boolean, animate: Boolean) {
        if (!textInput) {
            textInput = true
            refreshInputLayout()
            return
        }
        //隐藏键盘
        if (collapseKeyboard) {
            hideSoftInput(getThis(), binding.input)
        }
        if (binding.expand.isExpanded) {
            if (bottomPanelState == PANEL.EMOTION) {
                AnimationUtils.rotateTo(binding.emotion, false)
            }
            binding.expand.collapse(animate)
        }
        bottomPanelState = PANEL.COLLAPSE

    }

    //展开底部
    private fun expandBottomPanel(panel: PANEL) {
        // 判断键盘状态
        if (isSoftInputShown()) {
            lockContentHeight()
            hideSoftInput(applicationContext, binding.input)
            unlockContentHeight()
        }
        //修改底部栏高度 = 输入法高度
        binding.expand.layoutParams.height = getSupportSoftInputHeight()
        if (getSupportSoftInputHeight() == 0) {
            binding.expand.layoutParams.height = keyBoardHeight
        }
        binding.expand.expand()
        if (panel == PANEL.EMOTION) {
            AnimationUtils.rotateTo(binding.emotion, true)
            textInput = true
            binding.switchIcon.setImageResource(R.drawable.ic_voice_wave)
            binding.yunmojiList.visibility = View.VISIBLE
            binding.voiceBubble.visibility = View.GONE
            binding.voiceLayout.visibility = View.GONE
            binding.input.visibility = View.VISIBLE
        } else if (panel == PANEL.VOICE) {
            if (bottomPanelState == PANEL.EMOTION) {
                AnimationUtils.rotateTo(binding.emotion, false)
            }
            refreshVoiceBubble()
            binding.voiceLayout.visibility = View.VISIBLE
            binding.yunmojiList.visibility = View.GONE
        }
        bottomPanelState = panel
    }

    //锁定内容高度
    private fun lockContentHeight() {
        val params = binding.refresh.layoutParams as LinearLayout.LayoutParams
        params.height = binding.refresh.height
        params.weight = 0.0f
    }

    //释放被锁定内容高度
    private fun unlockContentHeight() {
        binding.refresh
        binding.input.postDelayed({ (binding.refresh.layoutParams as LinearLayout.LayoutParams).weight = 1.0f }, 280L)
    }

    //获取输入法高度
    private fun getSupportSoftInputHeight(): Int {
        val rect = Rect()
        window.decorView.getWindowVisibleDisplayFrame(rect)
        val screenHeight = window.decorView.rootView.height
        return screenHeight - rect.bottom
    }


    //输入法是否显示
    private fun isSoftInputShown(): Boolean {
        return getSupportSoftInputHeight() != 0
    }


    //当未获取键盘高度时，设定表情包高度787（貌似也没啥用）
    private
    val keyBoardHeight: Int
        get() = 787


    /**
     * 当用户通过系统相册选择图片返回时，将调用本函数
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        if (requestCode == MyProfileActivity.RC_CHOOSE_PHOTO) { //选择图片返回
            if (null == data) {
                Toast.makeText(this, R.string.no_image_selected, Toast.LENGTH_SHORT).show()
                return
            }
            val uri = data.data
            if (null == uri) { //如果单个Uri为空，则可能是1:多个数据 2:没有数据
                Toast.makeText(this, R.string.no_image_selected, Toast.LENGTH_SHORT).show()
                return
            }
            val filePath = FileProviderUtils.getFilePathByUri(getThis(), uri)
            filePath?.let {
                viewModel.conversation.value?.let { conversation ->
                    if (conversation.friendType == 1 || conversation.friendType == 3 || conversation.friendType == 5 || conversation.friendType == 7) {
                        ImageDetectBottomFragment().setMessage(null)
                                .setTitle(getString(R.string.hint_accessibility_sure_to_send_title))
                                .setSubtitle(getString(R.string.hint_type_visual_sure_to_send_subtitle))
                                .setUrl(filePath)
                                .setOnConfirmListener(object : ImageDetectBottomFragment.OnConfirmListener {
                                    override fun onConfirm(url: String) {
                                        viewModel.sendImageMessage(it)
                                    }
                                })
                                .show(supportFragmentManager, "xx")
                    } else {
                        viewModel.sendImageMessage(it)
                    }
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        audioPlayHelper.destroy()
        voiceHelper.destroy()
    }

    //隐藏键盘
    private fun hideSoftInput(context: Context, view: View?) {
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view!!.windowToken, 0)
    }

    override fun initViewBinding(): ActivityChatBinding {
        return ActivityChatBinding.inflate(layoutInflater)
    }


}
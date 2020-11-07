package com.stupidtree.cloudliter.ui.chat

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.ChatMessage
import com.stupidtree.cloudliter.data.model.Conversation
import com.stupidtree.cloudliter.data.model.Yunmoji
import com.stupidtree.cloudliter.ui.base.BaseActivity
import com.stupidtree.cloudliter.ui.base.BaseListAdapter
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.chat.detail.PopUpImageMessageDetail
import com.stupidtree.cloudliter.ui.chat.detail.PopUpTextMessageDetail
import com.stupidtree.cloudliter.ui.myprofile.MyProfileActivity
import com.stupidtree.cloudliter.ui.widgets.EmoticonsEditText
import com.stupidtree.cloudliter.utils.*
import net.cachapa.expandablelayout.ExpandableLayout
import java.util.*

/**
 * 对话窗口
 */
@SuppressLint("NonConstantResourceId")
class ChatActivity : BaseActivity<ChatViewModel>() {
    /**
     * View绑定区
     */
    @JvmField
    @BindView(R.id.back)
    var back: View? = null

    @JvmField
    @BindView(R.id.menu)
    var menu: View? = null

    @BindView(R.id.input)
    lateinit var inputEditText: EmoticonsEditText

    @JvmField
    @BindView(R.id.title)
    var titleText: TextView? = null


    @BindView(R.id.list)
    lateinit var list: RecyclerView

    @JvmField
    @BindView(R.id.send)
    var send: View? = null

    @JvmField
    @BindView(R.id.state)
    var stateText: TextView? = null

    @JvmField
    @BindView(R.id.state_icon)
    var stateIcon: ImageView? = null

    @JvmField
    @BindView(R.id.state_bar)
    var stateBar: ViewGroup? = null

    @BindView(R.id.refresh)
    lateinit var refreshLayout: SwipeRefreshLayout

    @BindView(R.id.add)
    lateinit var emotion: View

    @BindView(R.id.image)
    lateinit var imageButton: View

    @BindView(R.id.expand)
    lateinit var expandableLayout: ExpandableLayout

    @BindView(R.id.yunmoji_list)
    lateinit var yunmojiList //表情列表
            : RecyclerView

    @BindView(R.id.voice_button)
    lateinit var voiceButton: View

    @BindView(R.id.voice_button_hint)
    lateinit var voiceButtonHint: TextView

    @BindView(R.id.voice_layout)
    lateinit var voiceLayout: ViewGroup

    @BindView(R.id.switch_button)
    lateinit var switchButton: View

    @BindView(R.id.switch_icon)
    lateinit var switchIcon: ImageView

    @BindView(R.id.voice_bubble)
    lateinit var voiceBubble: View

    @BindView(R.id.voice_cancel)
    lateinit var voiceCancel: View

    @BindView(R.id.recorfing_text)
    lateinit var recordingText: TextView

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


    /**
     * 语音控制
     */
    lateinit var voiceHelper: AudioRecordHelper
    lateinit var audioPlayHelper: AudioPlayHelper

    /**
     * 常规操作区
     */
    override fun getLayoutId(): Int {
        return R.layout.activity_chat
    }

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
        viewModel!!.bindService(this)
        refreshInputLayout()
    }

    //更新Intent时（更换聊天对象），刷新列表
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.extras != null && intent.extras!!.getSerializable("conversation") != null) {
            val conversation = intent.extras!!.getSerializable("conversation") as Conversation?
            Log.e("变更Intent", conversation.toString())
            if (conversation != null) {
                if (viewModel?.conversationId != conversation.id) {
                    viewModel?.setConversation(conversation)
                    listAdapter.clear()
                    viewModel?.fetchHistoryData() //变更对话时，重新加载聊天记录
                }
                viewModel?.setConversation(conversation)
            }
        }
    }

    //每次回到页面时，声明进入对话
    override fun onResume() {
        super.onResume()
        if (intent.extras != null && intent.extras!!.getSerializable("conversation") != null) {
            val conversation = intent.extras!!.getSerializable("conversation") as Conversation?
            if (conversation != null) {
                if (viewModel?.conversationId == null) {
                    viewModel?.setConversation(conversation)
                    viewModel?.fetchHistoryData() //初次进入时，重新加载聊天记录
                } else {
                    viewModel!!.fetchNewData() //非第一次进入
                }
            }
        }
        viewModel?.getIntoConversation(this)
    }

    //每次页面失去焦点时，退出对话，解绑服务
    override fun onStop() {
        super.onStop()
        Log.e("ChatActivity", "onStop")
        viewModel?.leftConversation(this)
        viewModel?.unbindService(this)
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
                recordingText.text = TextUtils.getVoiceTimeText(getThis(), count)
            }
        })
        audioPlayHelper = AudioPlayHelper(this, object : AudioPlayHelper.VoicePlayListener {
            override fun onStartPlaying(playingId: String) {
                listAdapter.changeAudioState(list, playingId, ChatMessage.VOICE_STATE.PLAYING)
            }

            override fun onToggle(playingId: String, toStart: Boolean) {
                listAdapter.changeAudioState(list, playingId, if (toStart) ChatMessage.VOICE_STATE.PLAYING else ChatMessage.VOICE_STATE.PAUSED)
            }

            override fun onPlayingFinished(playingId: String) {
                listAdapter.changeAudioState(list, playingId, ChatMessage.VOICE_STATE.STOPPED)
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
        viewModel?.conversation!!.observe(this, { conversation: Conversation? ->
            conversation?.let { setConversationViews(it) }
        })
        viewModel?.getListData(this)!!.observe(this, { listDataState: DataState<List<ChatMessage>?> ->
            refreshLayout.isRefreshing = false
            Log.e("ChatActivity列表变动", listDataState.listAction.toString() + "》》" + listDataState.data!!.size)
            if (listDataState.state === DataState.STATE.SUCCESS) {
                //添加一条消息
                if (listDataState.listAction === DataState.LIST_ACTION.APPEND_ONE) {
                    val theOne = listDataState.data!![0]
                    //对方发出的，标为已读
                    if (viewModel!!.myId != theOne.fromId) {
                        viewModel!!.markRead(this, theOne)
                    }
                    listAdapter.notifyItemsAppended(listDataState.data!!)
                    if (listAdapter.itemCount > 0) {
                        list.smoothScrollToPosition(listAdapter.itemCount - 1)
                    }
                } else if (listDataState.listAction === DataState.LIST_ACTION.APPEND) {
                    listAdapter.notifyItemsAppended(listDataState.data!!)
                    if (listAdapter.itemCount > 0) {
                        list.smoothScrollToPosition(listAdapter.itemCount - 1)
                    }
                } else if (listDataState.listAction === DataState.LIST_ACTION.PUSH_HEAD) {
                    //下拉加载更多
                    if (!listDataState.isRetry) { //第一次获取，本地数据
                        listAdapter.notifyItemsPushHead(listDataState.data!!)
                        if (listDataState.data!!.isNotEmpty()) {
                            list.smoothScrollBy(0, -150)
                        }
                    } else { //获取到网络数据，刷新对应项
                        listDataState.data.let {
                            listAdapter.notifyHeadItemsUpdated(it!!)
                        }
                    }
                } else {
                    listAdapter.notifyItemChangedSmooth(listDataState.data!!)
                    if (listAdapter.itemCount > 0) {
                        list.smoothScrollToPosition(listAdapter.itemCount - 1)
                    }
                }
            }
        })
        viewModel!!.friendStateLiveData!!.observe(this, { friendStateDataState: DataState<FriendState> ->
            if (friendStateDataState.state === DataState.STATE.SUCCESS) {
                when (friendStateDataState.data!!.state) {
                    FriendState.STATE.ONLINE -> {
                        stateText!!.setText(R.string.online)
                        stateText!!.setTextColor(getColorPrimary())
                        stateIcon!!.setImageResource(R.drawable.element_round_primary)
                        stateBar!!.setBackgroundResource(R.drawable.element_rounded_bar_primary)
                    }
                    FriendState.STATE.YOU -> {
                        stateText!!.setText(R.string.with_you)
                        stateText!!.setTextColor(getColorPrimary())
                        stateIcon!!.setImageResource(R.drawable.element_round_primary)
                        stateBar!!.setBackgroundResource(R.drawable.element_rounded_bar_primary)
                    }
                    FriendState.STATE.OTHER -> {
                        stateText!!.setText(R.string.with_other)
                        stateText!!.setTextColor(getColorPrimary())
                        stateIcon!!.setImageResource(R.drawable.element_round_primary)
                        stateBar!!.setBackgroundResource(R.drawable.element_rounded_bar_primary)
                    }
                    FriendState.STATE.OFFLINE -> {
                        stateText!!.setText(R.string.offline)
                        stateText!!.setTextColor(getTextColorSecondary())
                        stateIcon!!.setImageResource(R.drawable.element_round_grey)
                        stateBar!!.setBackgroundResource(R.drawable.element_rounded_bar_grey)
                    }
                }
            }
        })
        viewModel!!.getImageSentResult().observe(this, { })
        viewModel!!.getVoiceSentResult().observe(this, { })
        //消息成功发送后反馈给列表
        viewModel!!.messageSentState!!.observe(this, { chatMessageDataState ->
            if (chatMessageDataState!!.state === DataState.STATE.SUCCESS) {
                list.let { listAdapter.messageSent(it, chatMessageDataState!!.data!!) }
            }
        })
        //消息被对方读取后反馈给列表
        viewModel!!.messageReadState!!.observe(this, { messageReadNotificationDataState ->
            Log.e("messageRead", messageReadNotificationDataState!!.data.toString())
            if (messageReadNotificationDataState.state === DataState.STATE.SUCCESS) {
                list.let { listAdapter.messageRead(it, messageReadNotificationDataState.data!!) }
            }
        })
    }


    //设置toolbar
    private fun setUpToolbar() {
        back!!.setOnClickListener { onBackPressed() }
        menu!!.setOnClickListener { ActivityUtils.startConversationActivity(getThis(), viewModel!!.friendId!!) }
    }

    //初始化聊天列表
    @SuppressLint("ClickableViewAccessibility")
    private fun setUpMessageList() {
        listAdapter = ChatListAdapter(this, LinkedList())
        list.adapter = listAdapter
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true //从底部往上堆
        list.layoutManager = layoutManager
        //触摸recyclerView的监听
        list.setOnTouchListener { _: View?, _: MotionEvent? ->
            collapseBottomPanel(collapseKeyboard = true, animate = true)
            false
        }
        refreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent)
        refreshLayout.setOnRefreshListener { viewModel?.loadMore() }
        listAdapter.setOnItemLongClickListener(object : BaseListAdapter.OnItemLongClickListener<ChatMessage> {
            override fun onItemLongClick(data: ChatMessage, view: View?, position: Int): Boolean {
                if (data.getType() == ChatMessage.TYPE.TXT && !data.isTimeStamp) {
                    PopUpTextMessageDetail().setChatMessage(data)
                            .show(supportFragmentManager, "detail")
                } else if (data.getType() == ChatMessage.TYPE.IMG && !data.isTimeStamp) {
                    PopUpImageMessageDetail().setChatMessage(data)
                            .show(supportFragmentManager, "detail")
                }
                return true
            }
        })
        listAdapter.setOnItemClickListener(object : BaseListAdapter.OnItemClickListener<ChatMessage> {
            override fun onItemClick(data: ChatMessage, card: View?, position: Int) {
                if (data.getType() == ChatMessage.TYPE.IMG && !data.isTimeStamp) {
                    val urls = listAdapter.imageUrls
                    ActivityUtils.showMultipleImages(getThis(), urls, urls.indexOf(data.content?.let { ImageUtils.getChatMessageImageUrl(it) })
                    )
                } else if (data.getType() == ChatMessage.TYPE.VOICE && !data.isTimeStamp) {
                    if (audioPlayHelper.playingId != null) {
                        if (audioPlayHelper.playingId != data.id) {//有其他消息正在播放
                            listAdapter.changeAudioState(list, audioPlayHelper.playingId!!, ChatMessage.VOICE_STATE.STOPPED)
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
    }

    //初始化各种按钮
    @SuppressLint("ClickableViewAccessibility")
    private fun setUpButtons() {
        //点击发送
        send!!.setOnClickListener {
            if (voiceHelper.state == AudioRecordHelper.STATE.DONE && !textInput) {
                voiceMessageSend()
            } else {
                if (!TextUtils.isEmpty(inputEditText.text.toString())) {
                    viewModel?.sendMessage(inputEditText.text.toString())
                    inputEditText.setText("")
                }

            }
        }
        emotion.setOnClickListener {
            if (bottomPanelState == PANEL.EMOTION) {
                collapseBottomPanel(true, true)
            } else {
                expandBottomPanel(PANEL.EMOTION)
            }
        }


        imageButton.setOnClickListener { GalleryPicker.choosePhoto(getThis(), false) }

        switchButton.setOnClickListener {
            textInput = !textInput
            refreshInputLayout()
        }
        voiceButton.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                if (p1 != null) {
                    //Log.e("action",p1.action.toString())
                    if (p1.action == MotionEvent.ACTION_DOWN) {
                        voiceHelper.startRecord()
                        voiceButtonHint.setText(R.string.voice_button_hint_pressed)
                    } else if (p1.action == MotionEvent.ACTION_UP || p1.action == 3) {
                        voiceHelper.stopRecord()
                        voiceButtonHint.setText(R.string.voice_button_hint_press)
                    }
                }
                return false

            }

        })

        voiceCancel.setOnClickListener {
            cancelVoiceMessage()
        }

        inputEditText.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                if (bottomPanelState != PANEL.COLLAPSE) {
                    lockContentHeight()
                    collapseBottomPanel(collapseKeyboard = false, animate = true)
                    unlockContentHeight()
                }

            }
            false
        }
//        inputEditText.setOnClickListener {
//            lockContentHeight()
//            collapseBottomPanel()
//            unlockContentHeight()
//        }
    }


    //初始化表情列表
    private fun setUpYunmojiList() {
        val ymList = ArrayList<Yunmoji>()
        for (i in 1..20) {
            val item = Yunmoji(resources.getIdentifier(String.format(Locale.getDefault(), "yunmoji_y%03d", i), "drawable", packageName))
            ymList.add(item)
        }
        yunmojiList.layoutManager = GridLayoutManager(this, 6)
        yunmojiListAdapter = YunmojiListAdapter(this, ymList)
        yunmojiList.adapter = yunmojiListAdapter
        val yunmojiOnItemClickListener: BaseListAdapter.OnItemClickListener<Yunmoji> =
                object : BaseListAdapter.OnItemClickListener<Yunmoji> {
                    override fun onItemClick(data: Yunmoji, card: View?, position: Int) {
                        inputEditText.append(data.getLastName(position))
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
            titleText!!.text = conversation.friendNickname
        } else {
            titleText!!.text = conversation.friendRemark
        }
    }


    //刷新输入区状态
    private fun refreshInputLayout() {
        if (textInput) {
            collapseBottomPanel(true, true)
            refreshVoiceBubble()
            switchIcon.setImageResource(R.drawable.ic_voice_wave)
            inputEditText.visibility = View.VISIBLE
        } else {
            expandBottomPanel(PANEL.VOICE)
            voiceButtonHint.setText(R.string.voice_button_hint_press)
            switchIcon.setImageResource(R.drawable.ic_baseline_keyboard_24)
            refreshVoiceBubble()
        }
    }


    //语音消息已经开始录制
    private fun voiceRecordingStarted() {
        voiceBubble.visibility = View.VISIBLE
        voiceCancel.visibility = View.GONE
        inputEditText.visibility = View.GONE
    }

    //语音消息录制完成，准备发送
    private fun voiceMessageReady() {
        if (!TextUtils.isEmpty(voiceHelper.filePath)) {
            voiceCancel.visibility = View.VISIBLE
        } else {
            refreshVoiceBubble()
        }

    }

    //发送语音消息
    private fun voiceMessageSend() {
        if (!TextUtils.isEmpty(voiceHelper.filePath)) {
            viewModel?.sendVoiceMessage(voiceHelper.filePath, voiceHelper.timeCount)
        }
        voiceHelper.sendRecord()
        voiceCancel.visibility = View.GONE
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
        recordingText.text = TextUtils.getVoiceTimeText(this, voiceHelper.timeCount)
        if (voiceHelper.state == AudioRecordHelper.STATE.DONE && voiceHelper.timeCount > 0) {
            voiceCancel.visibility = View.VISIBLE
            if (textInput) {
                voiceBubble.visibility = View.GONE
            } else {
                voiceBubble.visibility = View.VISIBLE
                inputEditText.visibility = View.GONE
            }
        } else {
            voiceBubble.visibility = View.GONE
            voiceCancel.visibility = View.GONE
            inputEditText.visibility = View.VISIBLE
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
            hideSoftInput(getThis(), inputEditText)
        }
        if (expandableLayout.isExpanded) {
            if (bottomPanelState == PANEL.EMOTION) {
                AnimationUtils.rotateTo(emotion, false)
            }
            expandableLayout.collapse(animate)
        }
        bottomPanelState = PANEL.COLLAPSE

    }

    //展开底部
    private fun expandBottomPanel(panel: PANEL) {
        // 判断键盘状态
        if (isSoftInputShown()) {
            lockContentHeight()
            hideSoftInput(applicationContext, inputEditText)
            unlockContentHeight()
        }
        //修改底部栏高度 = 输入法高度
        expandableLayout.layoutParams.height = getSupportSoftInputHeight()
        if (getSupportSoftInputHeight() == 0) {
            expandableLayout.layoutParams.height = keyBoardHeight
        }
        expandableLayout.expand()
        if (panel == PANEL.EMOTION) {
            AnimationUtils.rotateTo(emotion, true)
            textInput = true
            switchIcon.setImageResource(R.drawable.ic_voice_wave)
            yunmojiList.visibility = View.VISIBLE
            voiceBubble.visibility = View.GONE
            voiceLayout.visibility = View.GONE
            inputEditText.visibility = View.VISIBLE
        } else if (panel == PANEL.VOICE) {
            if (bottomPanelState == PANEL.EMOTION) {
                AnimationUtils.rotateTo(emotion, false)
            }
            refreshVoiceBubble()
            voiceLayout.visibility = View.VISIBLE
            yunmojiList.visibility = View.GONE
        }
        bottomPanelState = panel
    }

    //锁定内容高度
    private fun lockContentHeight() {
        val params = refreshLayout.layoutParams as LinearLayout.LayoutParams
        params.height = refreshLayout.height
        params.weight = 0.0f
    }

    //释放被锁定内容高度
    private fun unlockContentHeight() {
        inputEditText.postDelayed({ (refreshLayout.layoutParams as LinearLayout.LayoutParams).weight = 1.0f }, 280L)
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
            filePath?.let { viewModel?.sendImageMessage(it) }
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


}
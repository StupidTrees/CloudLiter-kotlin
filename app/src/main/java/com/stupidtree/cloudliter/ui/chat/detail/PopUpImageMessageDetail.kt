package com.stupidtree.cloudliter.ui.chat.detail

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import butterknife.BindView
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.ChatMessage
import com.stupidtree.cloudliter.ui.widgets.TransparentBottomSheetDialog
import java.text.DecimalFormat

/**
 * 圆角的文本框底部弹窗
 */
@SuppressLint("NonConstantResourceId")
class PopUpImageMessageDetail : TransparentBottomSheetDialog() {
    /**
     * View绑定区
     */
    @JvmField
    @BindView(R.id.neutral)
    var neutral: TextView? = null

    @JvmField
    @BindView(R.id.drawing)
    var drawing: TextView? = null

    @JvmField
    @BindView(R.id.hentai)
    var hentai: TextView? = null

    @JvmField
    @BindView(R.id.porn)
    var porn: TextView? = null

    @JvmField
    @BindView(R.id.sexy)
    var sexy: TextView? = null

    /**
     * 不得已放在UI里的数据
     */
    private lateinit var chatMessage: ChatMessage
    fun setChatMessage(chatMessage: ChatMessage): PopUpImageMessageDetail {
        this.chatMessage = chatMessage
        return this
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_chat_popup_image_message_detail
    }

    override fun onStart() {
        super.onStart()
    }

    override fun initViews(v: View) {
        val df = DecimalFormat("#.####")
        val map = chatMessage.getExtraAsImageAnalyse()
        neutral!!.text = df.format(map["Neutral"])
        drawing!!.text = df.format(map["Drawing"])
        porn!!.text = df.format(map["Porn"])
        hentai!!.text = df.format(map["Hentai"])
        sexy!!.text = df.format(map["Sexy"])
    }
}
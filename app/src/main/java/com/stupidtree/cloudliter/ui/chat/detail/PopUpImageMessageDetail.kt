package com.stupidtree.cloudliter.ui.chat.detail

import android.annotation.SuppressLint
import android.view.View
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.ChatMessage
import com.stupidtree.cloudliter.databinding.ActivityChatPopupImageMessageDetailBinding
import com.stupidtree.cloudliter.ui.widgets.TransparentBottomSheetDialog
import java.text.DecimalFormat

/**
 * 圆角的文本框底部弹窗
 */
@SuppressLint("NonConstantResourceId")
class PopUpImageMessageDetail : TransparentBottomSheetDialog<ActivityChatPopupImageMessageDetailBinding>() {


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

    override fun initViews(v: View) {
        val df = DecimalFormat("#.####")
        val map = chatMessage.getExtraAsImageAnalyse()
        binding.neutral.text = df.format(map["Neutral"])
        binding.drawing.text = df.format(map["Drawing"])
        binding.porn.text = df.format(map["Porn"])
        binding.hentai.text = df.format(map["Hentai"])
        binding.sexy.text = df.format(map["Sexy"])
    }

    override fun initViewBinding(v: View): ActivityChatPopupImageMessageDetailBinding {
        return ActivityChatPopupImageMessageDetailBinding.bind(v)
    }
}
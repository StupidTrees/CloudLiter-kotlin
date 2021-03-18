package com.stupidtree.cloudliter.ui.chat.detail

import android.annotation.SuppressLint
import android.view.View
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.ImageEntity
import com.stupidtree.cloudliter.databinding.ActivityChatPopupImageMessageDetailBinding
import com.stupidtree.style.widgets.TransparentBottomSheetDialog
import java.text.DecimalFormat

/**
 * 圆角的文本框底部弹窗
 */
@SuppressLint("NonConstantResourceId")
class PopUpImageMessageDetail : TransparentBottomSheetDialog<ActivityChatPopupImageMessageDetailBinding>() {


    /**
     * 不得已放在UI里的数据
     */
    private lateinit var imageEntity: ImageEntity
    fun setChatMessage(imageEntity:ImageEntity): PopUpImageMessageDetail {
        this.imageEntity = imageEntity
        return this
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_chat_popup_image_message_detail
    }

    override fun initViews(v: View) {
        val df = DecimalFormat("#.####")
        val map = imageEntity.getExtraAsImageAnalyse()
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
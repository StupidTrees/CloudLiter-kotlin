package com.stupidtree.cloudliter.ui.image

import android.graphics.Bitmap
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.ChatMessage
import com.stupidtree.cloudliter.databinding.ActivityImageDetailBinding
import com.stupidtree.cloudliter.ui.base.BaseActivity
import com.stupidtree.cloudliter.ui.chat.detail.PopUpImageMessageDetail
import com.stupidtree.cloudliter.utils.ImageUtils

class ImageDetectActivity : BaseActivity<ImageDetectViewModel, ActivityImageDetailBinding>() {

    var listAdapter:DetectResultAdapter?=null

    override fun initViewBinding(): ActivityImageDetailBinding {
        return ActivityImageDetailBinding.inflate(layoutInflater)
    }

    override fun getViewModelClass(): Class<ImageDetectViewModel> {
        return ImageDetectViewModel::class.java
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setWindowParams(statusBar = true, darkColor = true, navi = false)
        setToolbarActionBack(binding.toolbar)
    }

    override fun initViews() {
        viewModel.imageUrl.observe(this) {
            Thread {
                val myBitmap: Bitmap = Glide.with(getThis())
                        .asBitmap()
                        .load(it).submit().get()
                viewModel.imageLiveData.postValue(myBitmap)
            }.start()
        }
        viewModel.imageLiveData.observe(this) {
            binding.labeledImageView.setImage(it)
        }
        viewModel.detectionResult.observe(this) {
            if (it.data != null && viewModel.imageLiveData.value != null) {
                binding.labeledImageView.setLabels(it.data)
                val bitmap = viewModel.imageLiveData.value
                var announce = if (!it.data.isNullOrEmpty()) {
                    getString(R.string.description_image_total_detected, it.data!!.size)
                }else{
                    getString(R.string.description_image_none_detected)
                }
                listAdapter?.notifyItemChangedSmooth(it.data!!)
                bitmap?.let{ it1 ->
                    announce += if((it1.width.toFloat()/it1.height.toFloat()) > 1.3f){
                        getString(R.string.hint_horizontal_screen)
                    }else{
                        getString(R.string.hint_vertical_screen)
                    }
                }
                binding.labeledImageView.announceForAccessibility(announce)
            }
        }
        viewModel.chatMessageLiveData.observe(this){
            val map = it.getExtraAsImageAnalyse()
            val per =  1f- (map["Neutral"]!! + map["Drawing"]!!)/(
                    map["Porn"]!! +map["Hentai"]!!+map["Sexy"]!!+map["Neutral"]!! + map["Drawing"]!!
                    )
            binding.sensitiveText.text = getThis().getString(R.string.sensitive_result,per*100)
        }
        listAdapter = DetectResultAdapter(this, mutableListOf())
        binding.list.adapter = listAdapter
        binding.list.layoutManager = LinearLayoutManager(this)
        binding.sensitiveCard.setOnClickListener {
            viewModel.chatMessageLiveData.value?.let {
                PopUpImageMessageDetail().setChatMessage(it).show(supportFragmentManager,"sens")
            }
        }
        viewModel.chatMessageLiveData.value = intent.extras?.getSerializable("message") as ChatMessage?
    }


    override fun onResume() {
        super.onResume()
        viewModel.imageUrl.value = intent.getStringExtra("url")?.let {
            ImageUtils.getChatMessageImageUrl(it)
        }
    }


}
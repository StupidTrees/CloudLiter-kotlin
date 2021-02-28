package com.stupidtree.cloudliter.ui.imagedetect

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.ChatMessage
import com.stupidtree.cloudliter.databinding.FragmentImageDedectBinding
import com.stupidtree.cloudliter.ui.base.BaseFragment
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.chat.detail.PopUpImageMessageDetail
import kotlinx.android.synthetic.main.fragment_image_dedect.*

class ImageDetectFragment : BaseFragment<ImageDetectViewModel, FragmentImageDedectBinding>() {


    override fun getViewModelClass(): Class<ImageDetectViewModel> {
        return ImageDetectViewModel::class.java
    }

    override fun initViewBinding(): FragmentImageDedectBinding {
        return FragmentImageDedectBinding.inflate(layoutInflater)
    }

    var listAdapter: DetectResultAdapter? = null


    override fun onStart() {
        super.onStart()
        viewModel.imageUrl.value = arguments?.getString("url")
    }

    override fun initViews(view: View) {
        listAdapter = DetectResultAdapter(requireContext(), mutableListOf())
        binding?.list?.adapter = listAdapter
        binding?.list?.layoutManager = LinearLayoutManager(requireContext())
        binding?.sensitiveCard?.setOnClickListener {
            viewModel.chatMessageLiveData.value?.let {
                PopUpImageMessageDetail().setChatMessage(it).show(parentFragmentManager, "sens")
            }
        }
        viewModel.imageUrl.observe(this) {
            Thread {
                val myBitmap: Bitmap = Glide.with(requireContext())
                        .asBitmap()
                        .load(it).submit().get()
                viewModel.imageLiveData.postValue(myBitmap)
            }.start()
        }
        viewModel.imageLiveData.observe(this) {
            binding?.labeledImageView?.setImage(it)
        }
        viewModel.detectionResult.observe(this) {
            if (it.data != null && viewModel.imageLiveData.value != null) {
                binding?.labeledImageView?.setLabels(it.data)
                val bitmap = viewModel.imageLiveData.value
                var announce = if (!it.data.isNullOrEmpty()) {
                    getString(R.string.description_image_total_detected, it.data!!.size)
                } else {
                    getString(R.string.description_image_none_detected)
                }
                listAdapter?.notifyItemChangedSmooth(it.data!!)
                bitmap?.let { it1 ->
                    announce += if ((it1.width.toFloat() / it1.height.toFloat()) > 1.3f) {
                        getString(R.string.hint_horizontal_screen)
                    } else {
                        getString(R.string.hint_vertical_screen)
                    }
                }
                binding?.labeledImageView?.announceForAccessibility(announce)
            }
        }
        viewModel.chatMessageLiveData.observe(this) {
            if (it == null) {
                binding?.sensitiveCard?.visibility = View.GONE
            } else {
                binding?.sensitiveCard?.visibility = View.VISIBLE
                val map = it.getExtraAsImageAnalyse()
                val per = 1f - (map["Neutral"]!! + map["Drawing"]!!) / (
                        map["Porn"]!! + map["Hentai"]!! + map["Sexy"]!! + map["Neutral"]!! + map["Drawing"]!!
                        )
                binding?.sensitiveText?.text = getString(R.string.sensitive_result, per * 100)
            }
        }
        // 图片分类
        viewModel.imageClassifyResult.observe(this) {
            if (it.state!=DataState.STATE.SUCCESS) {
                binding?.classifyCard?.visibility = View.GONE
            } else {
                binding?.kindText?.text = getString(R.string.scene_classify_result_test,it.data?.get("class_cn")?.asString)
                binding?.classifyCard?.visibility = View.VISIBLE
            }
        }

        viewModel.chatMessageLiveData.value = arguments?.getSerializable("message") as ChatMessage?
    }

    companion object {
        fun newInstance(url: String, chatMessage: ChatMessage?): ImageDetectFragment {
            val args = Bundle()
            args.putString("url", url)
            args.putSerializable("message", chatMessage)
            val fragment = ImageDetectFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
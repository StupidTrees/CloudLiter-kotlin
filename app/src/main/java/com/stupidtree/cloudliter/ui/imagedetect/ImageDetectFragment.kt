package com.stupidtree.cloudliter.ui.imagedetect

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.ChatMessage
import com.stupidtree.cloudliter.databinding.FragmentImageDedectBinding
import com.stupidtree.cloudliter.ui.base.BaseFragment
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.chat.detail.PopUpImageMessageDetail
import com.stupidtree.cloudliter.utils.ImageUtils

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
        viewModel.imageIdLiveData.value = Pair(arguments?.getString("id")?:"",arguments?.getBoolean("local")?:false)

    }

    override fun initViews(view: View) {
        listAdapter = DetectResultAdapter(requireContext(), mutableListOf())
        binding?.list?.adapter = listAdapter
        binding?.list?.layoutManager = LinearLayoutManager(requireContext())
        binding?.sensitiveCard?.setOnClickListener {
            viewModel.imageEntityLiveData.value?.data?.let {
                PopUpImageMessageDetail().setChatMessage(it).show(parentFragmentManager, "sens")
            }
        }
        viewModel.imageIdLiveData.observe(this) {
                Thread {
                    try {
                        val myBitmap: Bitmap = Glide.with(requireContext())
                                .asBitmap()
                                .load(if(it.second)it.first else ImageUtils.getChatMessageImageUrl(it.first))
                                .submit().get()
                        viewModel.imageLiveData.postValue(myBitmap)
                    } catch (e: Exception) {
                    }
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
        // 图片分类
        viewModel.imageClassifyResult.observe(this) {
            if (it.state != DataState.STATE.SUCCESS) {
                binding?.classifyCard?.visibility = View.GONE
            } else {
                binding?.kindText?.text = getString(R.string.scene_classify_result_test, it.data?.get("class_cn")?.asString)
                binding?.classifyCard?.visibility = View.VISIBLE
            }
        }

        viewModel.imageEntityLiveData.observe(this) {
            if (it == null) {
                binding?.sensitiveCard?.visibility = View.GONE
            } else {
                binding?.sensitiveCard?.visibility = View.VISIBLE
                it.data?.let { image->
                    val map = image.getExtraAsImageAnalyse()
                    val per = 1f - (map["Neutral"]!! + map["Drawing"]!!) / (
                            map["Porn"]!! + map["Hentai"]!! + map["Sexy"]!! + map["Neutral"]!! + map["Drawing"]!!
                            )
                    binding?.sensitiveText?.text = getString(R.string.sensitive_result, per * 100)
                }
            }
        }
    }

    companion object {
        fun newInstance(url: String,local:Boolean): ImageDetectFragment {
            val args = Bundle()
            args.putString("id", url)
            args.putBoolean("local",local)
            val fragment = ImageDetectFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
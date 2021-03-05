package com.stupidtree.cloudliter.ui.imagedetect

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.databinding.FragmentImageDedectBinding
import com.stupidtree.cloudliter.ui.base.BaseFragment
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.chat.detail.PopUpImageMessageDetail
import com.stupidtree.cloudliter.utils.ImageUtils
import java.text.DecimalFormat

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
        refresh()
    }

    fun refresh(){
        if(arguments?.getBoolean("local") == true){
            binding?.sceneCard?.visibility = View.GONE
            binding?.sensitiveCard?.visibility = View.GONE
        }else{
            binding?.sceneCard?.visibility = View.VISIBLE
            binding?.sensitiveCard?.visibility = View.VISIBLE
        }
        binding?.loadingDetect?.visibility = View.VISIBLE
        binding?.detectTitle?.text = getString(R.string.detect_progressing)

        binding?.loadingScene?.visibility = View.VISIBLE
        binding?.sceneResult?.text = getString(R.string.scene_progressing)

        binding?.loadingSensitive?.visibility = View.VISIBLE
        binding?.sensitiveResult?.text = getString(R.string.sensitive_progressing)
        viewModel.imageIdLiveData.value = Pair(arguments?.getString("id")?:"",arguments?.getBoolean("local")?:false)

    }

    @SuppressLint("SetTextI18n")
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
            binding?.loadingDetect?.visibility = View.GONE
            if (it.data != null && viewModel.imageLiveData.value != null) {
                binding?.labeledImageView?.setLabels(it.data)
                val bitmap = viewModel.imageLiveData.value
                var announce = if (!it.data.isNullOrEmpty()) {
                    getString(R.string.description_image_total_detected, it.data!!.size)
                } else {
                    getString(R.string.description_image_none_detected)
                }
                binding?.detectTitle?.text = announce
                viewModel.imageLiveData.value?.let {bitmap->
                    listAdapter?.notifyItemChangedSmooth(it.data!!,bitmap)
                }

                bitmap?.let { it1 ->
                    announce += if ((it1.width.toFloat() / it1.height.toFloat()) > 1.3f) {
                        getString(R.string.hint_horizontal_screen)
                    } else {
                        getString(R.string.hint_vertical_screen)
                    }
                }
                binding?.labeledImageView?.announceForAccessibility(announce)
            }else{
                binding?.detectTitle?.text = getString(R.string.detect_failed)
            }

        }
        // 图片分类
        viewModel.imageClassifyResult.observe(this) {
            binding?.loadingScene?.visibility = View.GONE
            binding?.sceneResult?.visibility = View.VISIBLE
            if (it.state != DataState.STATE.SUCCESS) {
                binding?.sceneResult?.text =  getString(R.string.scene_classify_failed)
            } else {
                binding?.sceneResult?.text = it.data?.get("class_cn")?.asString
            }
        }

        viewModel.imageEntityLiveData.observe(this) {
            binding?.loadingSensitive?.visibility = View.GONE
            if (it == null) {
                binding?.sensitiveCard?.visibility = View.GONE
            } else {
                binding?.sensitiveCard?.visibility = View.VISIBLE
                it.data?.let { image->
                    val map = image.getExtraAsImageAnalyse()
                    val per = 1f - (map["Neutral"]!! + map["Drawing"]!!) / (
                            map["Porn"]!! + map["Hentai"]!! + map["Sexy"]!! + map["Neutral"]!! + map["Drawing"]!!
                            )
                    val df = DecimalFormat("#0.00")
                    binding?.sensitiveResult?.visibility = View.VISIBLE
                    binding?.sensitiveResult?.text = "${df.format(per*100)}%"
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
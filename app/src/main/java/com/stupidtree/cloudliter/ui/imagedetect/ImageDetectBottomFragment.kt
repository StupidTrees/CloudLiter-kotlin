package com.stupidtree.cloudliter.ui.imagedetect


import android.view.View
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.ChatMessage
import com.stupidtree.cloudliter.databinding.FragmentImageDetectPopupBinding
import com.stupidtree.style.widgets.TransparentBottomSheetDialog

class ImageDetectBottomFragment : TransparentBottomSheetDialog<FragmentImageDetectPopupBinding>() {

    private var message: ChatMessage? = null
    private var url: String? = null
    private  var title:String?=null
    private  var subtitle:String?=null
    private var onConfirmListener:OnConfirmListener? = null

    interface OnConfirmListener{
        fun onConfirm(url: String)
    }

    fun setUrl(url: String): ImageDetectBottomFragment {
        this.url = url
        return this
    }

    fun setTitle(title:String):ImageDetectBottomFragment{
        this.title = title
        return this
    }

    fun setSubtitle(subtitle:String):ImageDetectBottomFragment{
        this.subtitle = subtitle
        return this
    }
    fun setMessage(cm: ChatMessage?): ImageDetectBottomFragment {
        this.message = cm
        return this
    }

    fun setOnConfirmListener(l:OnConfirmListener): ImageDetectBottomFragment {
        this.onConfirmListener = l
        return this
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_image_detect_popup
    }

    override fun initViewBinding(v: View): FragmentImageDetectPopupBinding {
        return FragmentImageDetectPopupBinding.bind(v)
    }

    override fun initViews(v: View) {
        url?.let {
            childFragmentManager.beginTransaction().replace(R.id.fragment,
                    ImageDetectFragment.newInstance(it,true)).commit()
        }
        binding.title.text = title.toString()
        binding.subtitle.text = subtitle.toString()
        binding.cancel.setOnClickListener {
            dismiss()
        }
        binding.confirm.setOnClickListener {
            onConfirmListener?.let {
                url?.let { it1 -> it.onConfirm(it1) }
            }
            dismiss()
        }
    }
}
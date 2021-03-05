package com.stupidtree.cloudliter.ui.imagedetect

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.stupidtree.cloudliter.data.source.ai.yolo.Classifier
import com.stupidtree.cloudliter.databinding.ActivityImageDetailListItemBinding
import com.stupidtree.cloudliter.ui.base.BaseListAdapter
import com.stupidtree.cloudliter.ui.base.BaseViewHolder
import kotlinx.android.synthetic.main.activity_chat.view.*
import java.text.DecimalFormat

class DetectResultAdapter(mContext: Context, mBeans: MutableList<Classifier.Recognition>) : BaseListAdapter<Classifier.Recognition, DetectResultAdapter.DHolder>(mContext, mBeans) {
    var bitmap: Bitmap? = null

    class DHolder(viewBinding: ActivityImageDetailListItemBinding) : BaseViewHolder<ActivityImageDetailListItemBinding>(viewBinding)

    fun notifyItemChangedSmooth(newL: List<Classifier.Recognition>, bitmap: Bitmap) {
        this.bitmap = bitmap
        super.notifyItemChangedSmooth(newL)
    }

    override fun getViewBinding(parent: ViewGroup, viewType: Int): ViewBinding {
        return ActivityImageDetailListItemBinding.inflate(mInflater, parent, false)
    }

    override fun createViewHolder(viewBinding: ViewBinding, viewType: Int): DHolder {
        return DHolder(viewBinding as ActivityImageDetailListItemBinding)
    }

    @SuppressLint("SetTextI18n")
    override fun bindHolder(holder: DHolder, data: Classifier.Recognition?, position: Int) {
        holder.binding.name.text = data?.title
        holder.binding.percentage.text = DecimalFormat("00.00").format(100f * data?.confidence!!) + "%"
        val xF = (bitmap?.width ?: 0f).toFloat() / 416f
        val yF = (bitmap?.height ?: 0f).toFloat() / 416f
        data.let {
            val rec = it.location
            val cropped = bitmap?.let { bm ->
                Bitmap.createBitmap(bm, (xF * rec.left).toInt(), (yF * rec.top).toInt(), (rec.width() * xF).toInt(), (rec.height() * yF).toInt())
            }
            Glide.with(mContext).load(cropped)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .into(holder.binding.image)
        }

    }
}
package com.stupidtree.cloudliter.ui.imagedetect

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.stupidtree.cloudliter.data.model.FaceResult
import com.stupidtree.cloudliter.data.source.ai.detect.ObjectDetectSource.Companion.IMAGE_SIZE
import com.stupidtree.cloudliter.databinding.ActivityImageDetailListItemBinding
import com.stupidtree.cloudliter.ui.base.BaseListAdapter
import com.stupidtree.cloudliter.ui.base.BaseViewHolder
import java.text.DecimalFormat

class DetectResultAdapter(mContext: Context, mBeans: MutableList<DetectResult>) : BaseListAdapter<DetectResult, DetectResultAdapter.DHolder>(mContext, mBeans) {
    var bitmap: Bitmap? = null

    class DHolder(viewBinding: ActivityImageDetailListItemBinding) : BaseViewHolder<ActivityImageDetailListItemBinding>(viewBinding)

    fun setFriendInfo(list: List<FaceResult>) {
        val map = mutableMapOf<String?, FaceResult>()
        for (l in list) {
            map[l.rectId] = l
        }
        for (item in mBeans) {
            if (map.keys.contains(item.id)) {
                item.setFriendInfo(map[item.id]?.userId, map[item.id]?.userName)
            }
        }
        notifyItemChangedSmooth(newL = mBeans)
    }

    fun notifyItemChangedSmooth(newL: List<DetectResult>, bitmap: Bitmap) {
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
    override fun bindHolder(holder: DHolder, data: DetectResult?, position: Int) {
        holder.binding.name.text = data?.name
        holder.binding.percentage.text = DecimalFormat("00.00").format(100f * data?.confidence!!) + "%"
        val xF = (bitmap?.width ?: 0f).toFloat() / IMAGE_SIZE
        val yF = (bitmap?.height ?: 0f).toFloat() / IMAGE_SIZE
        data.let {
            val rec = it.rect
            val cropped = bitmap?.let { bm ->
                Bitmap.createBitmap(bm, (xF * rec.left).toInt(), (yF * rec.top).toInt(), (rec.width() * xF).toInt(), (rec.height() * yF).toInt())
            }
            Glide.with(mContext).load(cropped)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .into(holder.binding.image)
            if (it.isFriend) {
                holder.binding.userName.text = data.friendName
                holder.binding.userName.visibility = View.VISIBLE
            } else {
                holder.binding.userName.visibility = View.GONE
            }
        }
        holder.binding.item.setOnClickListener {
            mOnItemClickListener?.onItemClick(data, it, position)
        }

    }
}
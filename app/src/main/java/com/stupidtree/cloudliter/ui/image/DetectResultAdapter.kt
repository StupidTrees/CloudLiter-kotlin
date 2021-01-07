package com.stupidtree.cloudliter.ui.image

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.stupidtree.cloudliter.data.source.ai.yolo.Classifier
import com.stupidtree.cloudliter.databinding.ActivityImageDetailListItemBinding
import com.stupidtree.cloudliter.ui.base.BaseListAdapter
import com.stupidtree.cloudliter.ui.base.BaseViewHolder
import java.text.DecimalFormat

class DetectResultAdapter(mContext: Context, mBeans: MutableList<Classifier.Recognition>) : BaseListAdapter<Classifier.Recognition, DetectResultAdapter.DHolder>(mContext, mBeans) {


    class DHolder(viewBinding: ActivityImageDetailListItemBinding) : BaseViewHolder<ActivityImageDetailListItemBinding>(viewBinding)

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
    }
}
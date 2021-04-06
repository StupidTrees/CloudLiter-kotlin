package com.stupidtree.cloudliter.ui.accessibility.egine

import android.app.Application
import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.stupidtree.accessibility.ai.icon.IconClassifierSource
import com.stupidtree.cloudliter.databinding.ActivityLightEngineTestItemBinding
import com.stupidtree.component.data.DataState
import com.stupidtree.style.base.BaseListAdapter
import com.stupidtree.style.base.BaseViewHolder
import java.text.DecimalFormat

class IconTestListAdapter(val activity: AppCompatActivity, mBeans: MutableList<String>) : BaseListAdapter<String, IconTestListAdapter.IHolder>(activity, mBeans) {

    inner class IHolder(viewBinding: ActivityLightEngineTestItemBinding) :BaseViewHolder<ActivityLightEngineTestItemBinding>(viewBinding){
        val imageLiveData = MutableLiveData<Bitmap>()
        val format = DecimalFormat("##.00")
        private val iconClassifyResult = Transformations.switchMap(imageLiveData){
            return@switchMap IconClassifierSource.getInstance(mContext.applicationContext as Application)
                    .classifyIcon(it)
        }
        init {

            imageLiveData.observe(activity){
                binding.image.setImageBitmap(it)
            }
            iconClassifyResult.observe(activity){
                if(it.state==DataState.STATE.SUCCESS){
                    it.data?.let { data->
                        val top = data.sorted()[0]
                        binding.name.text = top.name
                        binding.percentage.text = format.format(top.confidence*100)+"%"
                    }
                }
            }
        }
    }

    override fun getViewBinding(parent: ViewGroup, viewType: Int): ViewBinding {
        return ActivityLightEngineTestItemBinding.inflate(mInflater,parent,false)
    }

    override fun createViewHolder(viewBinding: ViewBinding, viewType: Int): IHolder {
        return IHolder(viewBinding as ActivityLightEngineTestItemBinding)
    }

    override fun bindHolder(holder: IHolder, data: String?, position: Int) {
        Thread {
            try {
                val myBitmap: Bitmap = Glide.with(mContext)
                        .asBitmap()
                        .load(data)
                        .submit().get()
                holder.imageLiveData.postValue(myBitmap)
            } catch (e: Exception) {
            }
        }.start()

    }
}
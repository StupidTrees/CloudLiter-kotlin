package com.stupidtree.cloudliter.ui.widgets

import android.annotation.SuppressLint
import android.app.Application
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.AndroidViewModel
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.bm.library.PhotoView
import com.bumptech.glide.Glide
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.databinding.ActivityPhotoDetailBinding
import com.stupidtree.style.base.BaseActivity

class PhotoDetailActivity : BaseActivity<PhotoDetailActivity.PhotoViewModel, ActivityPhotoDetailBinding>() {

    var initIndex = 0
    var urls: List<String>? = null


    @SuppressLint("SetTextI18n")
    override fun initViews() {
        urls = mutableListOf()
        val data = intent.getStringArrayExtra("urls")
        initIndex = intent.getIntExtra("init_index", 0)
        if (data != null) {
            (urls as MutableList<String>).addAll(listOf(*data))
            binding.label.text = (initIndex + 1).toString() + "/" + (urls as MutableList<String>).size
            initPager()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setWindowParams(statusBar = true, darkColor = false, navi = true)
    }

    private fun initPager() {
        binding.pager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            @SuppressLint("SetTextI18n")
            override fun onPageSelected(position: Int) {
                binding.label.text = (position + 1).toString() + "/" + urls!!.size
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        binding.pager.adapter = object : PagerAdapter() {
            override fun getCount(): Int {
                return urls!!.size
            }

            override fun isViewFromObject(view: View, `object`: Any): Boolean {
                return view === `object`
            }

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                container.removeView(`object` as View)
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                val v = PhotoView(getThis())
                v.scaleType = ImageView.ScaleType.FIT_CENTER
                v.transitionName = "image"
                Glide.with(getThis()).load(urls!![position]).timeout(10000).into(v)
                v.adjustViewBounds = false
                v.contentDescription = getString(R.string.image)
                v.enable()
                container.addView(v)
                v.setOnClickListener { finish() }
                return v
            }
        }
        binding.pager.currentItem = initIndex
    }


    class PhotoViewModel(application: Application) : AndroidViewModel(application)

    override fun initViewBinding(): ActivityPhotoDetailBinding {
        return ActivityPhotoDetailBinding.inflate(layoutInflater)
    }

    override fun getViewModelClass(): Class<PhotoViewModel> {
        return PhotoViewModel::class.java
    }
}
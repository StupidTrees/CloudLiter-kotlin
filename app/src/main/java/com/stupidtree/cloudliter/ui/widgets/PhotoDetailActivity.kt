package com.stupidtree.cloudliter.ui.widgets

import android.annotation.SuppressLint
import android.app.Application
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.drawToBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.bm.library.PhotoView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.databinding.ActivityPhotoDetailBinding
import com.stupidtree.cloudliter.ui.imagedetect.ImageDetectFragment
import com.stupidtree.style.base.BaseActivity

class PhotoDetailActivity : BaseActivity<PhotoDetailActivity.PhotoViewModel, ActivityPhotoDetailBinding>() {

    var initIndex = 0
    private val ids: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.toolbar.title = ""
        setToolbarActionBack(binding.toolbar)
    }

    @SuppressLint("SetTextI18n")
    override fun initViews() {
        val data = intent.getStringArrayExtra("ids")
        initIndex = intent.getIntExtra("init_index", 0)

        if (data?.isNotEmpty() == true) {
            ids.addAll(listOf(*data))
            binding.label.text = (initIndex + 1).toString() + "/" + ids.size
            initPager()
        }
        supportFragmentManager.beginTransaction()
                .replace(R.id.detect_layout, ImageDetectFragment.newInstance(ids[initIndex], local = false, refreshOnStart = true), "detect").commit()
    }

    private fun initPager() {
        binding.pager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            @SuppressLint("SetTextI18n")
            override fun onPageSelected(position: Int) {
                binding.label.text = (position + 1).toString() + "/" + ids.size
                (supportFragmentManager.findFragmentByTag("detect")
                        as ImageDetectFragment?)?.preLoadImage(ids[position])


            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        binding.pager.adapter = object : PagerAdapter() {
            override fun getCount(): Int {
                return ids.size
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
                v.contentDescription = getString(R.string.photo_description, position + 1, ids.size)
                com.stupidtree.cloudliter.utils.ImageUtils.loadCloudImageInto(getThis(), ids[position], v)
                v.adjustViewBounds = false
                v.contentDescription = getString(R.string.image)
                v.enable()
                container.addView(v)
                return v
            }
        }
        binding.bar.setOnClickListener {
            val behavior = BottomSheetBehavior.from(binding.bottomLayout)
            if (behavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                binding.bar.contentDescription = getString(R.string.detect_expand)
            } else {
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                binding.bar.contentDescription = getString(R.string.detect_collapse)
            }
            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                        (supportFragmentManager.findFragmentByTag("detect")
                                as ImageDetectFragment?)?.refresh(false, ids[binding.pager.currentItem])
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {

                }

            })
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
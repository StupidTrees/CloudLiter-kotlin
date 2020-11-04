package com.stupidtree.cloudliter.ui.widgets

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModel
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import butterknife.BindView
import com.bm.library.PhotoView
import com.bumptech.glide.Glide
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.ui.base.BaseActivity
import java.util.*

class PhotoDetailActivity : BaseActivity<ViewModel>() {
    @JvmField
    @BindView(R.id.label)
    var label: TextView? = null

    @JvmField
    @BindView(R.id.pager)
    var pager: ViewPager? = null
    var initIndex = 0
    var urls: List<String>? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_photo_detail
    }

    override fun getViewModelClass(): Class<ViewModel>? {
        return null
    }

    @SuppressLint("SetTextI18n")
    override fun initViews() {
        urls = mutableListOf()
        val data = intent.getStringArrayExtra("urls")
        initIndex = intent.getIntExtra("init_index", 0)
        if (data != null) {
            (urls as MutableList<String>).addAll(listOf(*data))
            label!!.text = (initIndex + 1).toString() + "/" + (urls as MutableList<String>).size
            initPager()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setWindowParams(true, false, true)
    }

    fun initPager() {
        pager!!.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            @SuppressLint("SetTextI18n")
            override fun onPageSelected(position: Int) {
                label!!.text = (position + 1).toString() + "/" + urls!!.size
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        pager!!.adapter = object : PagerAdapter() {
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
                v.enable()
                container.addView(v)
                v.setOnClickListener { view: View? -> finish() }
                //                v.setOnLongClickListener(new View.OnLongClickListener() {
//                    @Override
//                    public boolean onLongClick(View v) {
//                        new AlertDialog.Builder(getThis()).setItems(new String[]{getString(R.string.download_image)}, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                // Toast.makeText(from,imageViewToUrl.get(view)+"",Toast.LENGTH_SHORT).show();
//                                ActivityUtils.DownloadImage(getThis(), urls.get(which), new ActivityUtils.OnDownloadDoneListener() {
//                                    @Override
//                                    public void onDone() {
//                                        try {
//                                            Toast.makeText(getThis(), R.string.save_done, Toast.LENGTH_SHORT).show();
//
//                                        } catch (Exception e) {
//
//                                        }
//                                    }
//                                });
//                            }
//                        }).create().show();
//                        return true;
//                    }
//                });
                return v
            }
        }
        pager!!.currentItem = initIndex
    }
}
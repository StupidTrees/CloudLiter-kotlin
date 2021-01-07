package com.stupidtree.cloudliter.ui.imagedetect

import android.os.Bundle
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.ChatMessage
import com.stupidtree.cloudliter.databinding.ActivityImageDetailBinding
import com.stupidtree.cloudliter.ui.base.BaseActivity

class ImageDetectActivity : BaseActivity<ImageDetectViewModel, ActivityImageDetailBinding>() {


    override fun initViewBinding(): ActivityImageDetailBinding {
        return ActivityImageDetailBinding.inflate(layoutInflater)
    }

    override fun getViewModelClass(): Class<ImageDetectViewModel> {
        return ImageDetectViewModel::class.java
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setWindowParams(statusBar = true, darkColor = true, navi = false)
        setToolbarActionBack(binding.toolbar)
    }

    override fun initViews() {
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment, ImageDetectFragment.newInstance(
                        intent.getStringExtra("url")!!, intent.extras?.getSerializable("message") as ChatMessage), "detect"
                ).commit()
    }


}
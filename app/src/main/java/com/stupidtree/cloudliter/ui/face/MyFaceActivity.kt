package com.stupidtree.cloudliter.ui.face

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.databinding.ActivityMyFaceBinding
import com.stupidtree.cloudliter.ui.base.BaseActivity
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.myprofile.MyProfileActivity
import com.stupidtree.cloudliter.utils.FileProviderUtils
import com.stupidtree.cloudliter.utils.GalleryPicker

class MyFaceActivity : BaseActivity<MyFaceViewModel, ActivityMyFaceBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setWindowParams(true, darkColor = true, navi = false)
    }

    override fun initViewBinding(): ActivityMyFaceBinding {
        return ActivityMyFaceBinding.inflate(layoutInflater)
    }

    override fun getViewModelClass(): Class<MyFaceViewModel> {
        return MyFaceViewModel::class.java
    }

    override fun initViews() {
        binding.add.setOnClickListener {
            GalleryPicker.choosePhoto(getThis(), false)
        }
        viewModel.uploadFaceResult.observe(this) {
            if (it.state === DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(), R.string.avatar_change_success, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(applicationContext, R.string.fail, Toast.LENGTH_SHORT).show()
            }
        }

    }

    /**
     * 当用户通过系统相册选择图片返回时，将调用本函数
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        when (requestCode) {
            MyProfileActivity.RC_CHOOSE_PHOTO -> {
                //选择图片返回，要跳转到图片裁剪
                if (null == data) {
                    Toast.makeText(this, R.string.no_image_selected, Toast.LENGTH_SHORT).show()
                    return
                }
                val uri = data.data
                if (null == uri) { //如果单个Uri为空，则可能是1:多个数据 2:没有数据
                    Toast.makeText(this, R.string.no_image_selected, Toast.LENGTH_SHORT).show()
                    return
                }
                // 剪裁图片
                GalleryPicker.cropPhoto(getThis(), FileProviderUtils.getFilePathByUri(getThis(), uri), 500)
            }
            MyProfileActivity.RC_CROP_PHOTO -> {                //裁剪图片返回，此时通知viewModel请求更改头像
                val path = GalleryPicker.getCroppedCacheDir(this)?.let { FileProviderUtils.getFilePathByUri(this, it) }
                // create RequestBody instance from file
                path?.let { viewModel.startUploadFace(it) }
            }
        }
    }

}
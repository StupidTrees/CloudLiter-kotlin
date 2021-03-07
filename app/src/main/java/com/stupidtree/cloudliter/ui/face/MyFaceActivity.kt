package com.stupidtree.cloudliter.ui.face

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.databinding.ActivityMyFaceBinding
import com.stupidtree.cloudliter.ui.base.BaseActivity
import com.stupidtree.cloudliter.ui.base.BaseListAdapter
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.myprofile.MyProfileActivity
import com.stupidtree.cloudliter.ui.widgets.PopUpText
import com.stupidtree.cloudliter.ui.wordcloud.FaceEntity
import com.stupidtree.cloudliter.utils.FileProviderUtils
import com.stupidtree.cloudliter.utils.GalleryPicker

class MyFaceActivity : BaseActivity<MyFaceViewModel, ActivityMyFaceBinding>() {
    lateinit var listAdapter: MyFaceAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setWindowParams(true, darkColor = true, navi = false)
        setToolbarActionBack(binding.toolbar)
    }

    override fun onStart() {
        super.onStart()
        refresh()
    }

    fun refresh() {
        binding.refresh.isRefreshing = true
        viewModel.startRefresh()
    }

    override fun initViewBinding(): ActivityMyFaceBinding {
        return ActivityMyFaceBinding.inflate(layoutInflater)
    }

    override fun getViewModelClass(): Class<MyFaceViewModel> {
        return MyFaceViewModel::class.java
    }

    override fun initViews() {
        binding.refresh.setColorSchemeColors(getColorPrimary())
        listAdapter = MyFaceAdapter(this, mutableListOf(), viewModel)
        binding.list.adapter = listAdapter
        binding.list.layoutManager = GridLayoutManager(this, 2)
        binding.add.setOnClickListener {
            GalleryPicker.choosePhoto(getThis(), false)
        }
        viewModel.uploadFaceResult.observe(this) {
            when {
                it.state === DataState.STATE.SUCCESS -> {
                    refresh()
                    Toast.makeText(getThis(), R.string.upload_success, Toast.LENGTH_SHORT).show()
                }
                it.state == DataState.STATE.SPECIAL -> {
                    Toast.makeText(applicationContext, R.string.no_face, Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(applicationContext, R.string.fail, Toast.LENGTH_SHORT).show()
                }
            }
        }
        viewModel.facesLiveData.observe(this) {
            binding.refresh.isRefreshing = false
            it.data?.let { it1 -> listAdapter.notifyItemChangedSmooth(it1, false) }
        }
        binding.refresh.setOnRefreshListener {
            refresh()
        }
        listAdapter.setOnItemClickListener(object : BaseListAdapter.OnItemClickListener<FaceEntity> {
            override fun onItemClick(data: FaceEntity, card: View?, position: Int) {
                PopUpText().setTitle(R.string.ensure_delete_face)
                        .setOnConfirmListener(object : PopUpText.OnConfirmListener {
                            override fun OnConfirm() {
                                data.id?.let { viewModel.deleteFace(it) }
                            }
                        }).show(supportFragmentManager, "delete")
            }

        })
        viewModel.deleteResult.observe(this) {
            if (it.state == DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(), R.string.delete_face_ok, Toast.LENGTH_SHORT).show()
                viewModel.startRefresh()
            } else {
                Toast.makeText(getThis(), R.string.fail, Toast.LENGTH_SHORT).show()
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
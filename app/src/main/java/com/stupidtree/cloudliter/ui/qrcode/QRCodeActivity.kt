package com.stupidtree.cloudliter.ui.qrcode

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.king.zxing.CaptureFragment
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.UserLocal
import com.stupidtree.cloudliter.databinding.ActivityQRCodeBinding
import com.stupidtree.style.base.BaseActivity
import com.stupidtree.component.data.DataState
import com.stupidtree.cloudliter.utils.ActivityUtils
import com.stupidtree.cloudliter.utils.ImageUtils
import com.stupidtree.style.picker.PermissionUtils
import com.stupidtree.cloudliter.utils.TextUtils


@SuppressLint("NonConstantResourceId")
class QRCodeActivity : BaseActivity<QRCodeViewModel, ActivityQRCodeBinding>() {

    lateinit var captureFragment: CaptureFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        captureFragment = CaptureFragment.newInstance()
        super.onCreate(savedInstanceState)

        setToolbarActionBack(binding.toolbar)
    }


    override fun getViewModelClass(): Class<QRCodeViewModel> {
        return QRCodeViewModel::class.java
    }

    override fun onStart() {
        super.onStart()
        PermissionUtils.grantCameraPermission(this)
        captureFragment.captureHelper.setOnCaptureCallback { result ->
            val jo = TextUtils.decodeUserBusinessCard(result)
            if(jo==null||!jo.containsKey("userId")||!jo.containsKey("time")){
                Toast.makeText(getThis(), getString(R.string.invalid_business_card), Toast.LENGTH_SHORT).show()
            }else{
                val id = jo["userId"]
                id?.let { ActivityUtils.startProfileActivity(getThis(), it.toString()) }
                finish()
            }
            true
        }
        viewModel.startRefresh()
        setUserInfo(viewModel.getLoggedInUser())
    }

    override fun initViews() {
        viewModel.imageLiveData?.observe(this, { data ->
            if (data.state == DataState.STATE.SUCCESS) {
                binding.image.setImageBitmap(data.data)
            }
        })

        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.add(R.id.scanner, captureFragment)
        ft.commit()
        binding.bar.setOnClickListener {
            val behavior = BottomSheetBehavior.from(binding.bottomLayout)
            if(behavior.state== STATE_EXPANDED){
                behavior.state = STATE_COLLAPSED
                binding.bar.contentDescription = getString(R.string.qr_expand)
            }else{
                behavior.state = STATE_EXPANDED
                binding.bar.contentDescription = getString(R.string.qr_collapse)
            }

        }

    }

    private fun setUserInfo(user: UserLocal) {
        ImageUtils.loadAvatarInto(this, user.id, binding.avatar)
        binding.nickname.text = user.nickname
    }



    override fun initViewBinding(): ActivityQRCodeBinding {
        return ActivityQRCodeBinding.inflate(layoutInflater)
    }
}
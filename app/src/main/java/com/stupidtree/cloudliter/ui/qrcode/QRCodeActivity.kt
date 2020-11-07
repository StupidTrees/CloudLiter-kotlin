package com.stupidtree.cloudliter.ui.qrcode

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentTransaction
import butterknife.BindView
import com.king.zxing.CaptureActivity
import com.king.zxing.CaptureFragment
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.UserLocal
import com.stupidtree.cloudliter.ui.base.BaseActivity
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.utils.ActivityUtils
import com.stupidtree.cloudliter.utils.ImageUtils
import com.stupidtree.cloudliter.utils.PermissionUtils
import com.stupidtree.cloudliter.utils.TextUtils


@SuppressLint("NonConstantResourceId")
class QRCodeActivity : BaseActivity<QRCodeViewModel>() {
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    @BindView(R.id.image)
    lateinit var image: ImageView

    @BindView(R.id.avatar)
    lateinit var avatar: ImageView

    @BindView(R.id.nickname)
    lateinit var nickname: TextView

    lateinit var captureFragment: CaptureFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        captureFragment = CaptureFragment.newInstance()
        super.onCreate(savedInstanceState)
        setWindowParams(statusBar = true, darkColor = true, navi = false)
        setToolbarActionBack(toolbar)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_q_r_code
    }

    override fun getViewModelClass(): Class<QRCodeViewModel>? {
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
    }

    override fun initViews() {
        viewModel!!.imageLiveData?.observe(this, { data ->
            if (data.state == DataState.STATE.SUCCESS) {
                image.setImageBitmap(data.data)
            }
        })

        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.add(R.id.scanner, captureFragment)
        ft.commit()
    }

    private fun setUserInfo(user: UserLocal) {
        ImageUtils.loadLocalAvatarInto(this, user.avatar, avatar)
        nickname.text = user.nickname
    }

    override fun onResume() {
        super.onResume()
        viewModel!!.startRefresh()
        setUserInfo(viewModel!!.getLoggedInUser())
    }
}
package com.stupidtree.cloudliter.ui.main.navigation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.View
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.databinding.FragmentNavigationBinding
import com.stupidtree.cloudliter.service.socket.SocketIOClientService
import com.stupidtree.style.base.BaseFragmentWithReceiver
import com.stupidtree.cloudliter.utils.ActivityUtils
import com.stupidtree.cloudliter.utils.ImageUtils

class NavigationFragment : BaseFragmentWithReceiver<NavigationViewModel, FragmentNavigationBinding>() {

    /**
     * 广播区
     */
    override var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.startRefresh()
    }

    override fun getIntentFilter(): IntentFilter {
        val iF = IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        iF.addAction(SocketIOClientService.RECEIVE_RELATION_EVENT)
        return iF
    }

    override fun getViewModelClass(): Class<NavigationViewModel> {
        return NavigationViewModel::class.java
    }

    override fun initViews(view: View) {
        viewModel.localUserLiveData.observe(this) { userLocalInfo ->
            if (userLocalInfo.isValid) { //如果已登录
                //装载头像
                binding?.avatar?.let { ImageUtils.loadLocalAvatarInto(requireContext(), userLocalInfo.avatar, it) }
                //设置各种文字
                binding?.username?.text = userLocalInfo.username
                binding?.nickname?.text = userLocalInfo.nickname
                binding?.userLayout?.setOnClickListener { ActivityUtils.startProfileActivity(requireContext(), userLocalInfo.id!!) }
            } else {
                //未登录的信息显示
                binding?.username?.setText(R.string.not_logged_in)
                binding?.nickname?.setText(R.string.please_log_in)
                binding?.avatar?.setImageResource(R.drawable.place_holder_avatar)
                binding?.userLayout?.setOnClickListener { ActivityUtils.startLoginActivity(requireContext()) }
            }

        }
        binding?.profileLayout?.setOnClickListener {
            ActivityUtils.startMyProfileActivity(requireContext())
        }
        binding?.qrLayout?.setOnClickListener {
            ActivityUtils.startQRCodeActivity(requireContext())
        }
        binding?.notificationLayout?.setOnClickListener {
            ActivityUtils.startRelationEventActivity(requireContext())
        }
//        binding?.faceLayout?.setOnClickListener {
//            ActivityUtils.startMyFaceActivity(requireContext())
//        }
        binding?.accessibilityLayout?.setOnClickListener {
            ActivityUtils.startAccessibilityActivity(requireContext())
        }
        binding?.sceneGalleryLayout?.setOnClickListener {
            ActivityUtils.startGalleryActivity(requireContext())
        }
        binding?.contactGalleryLayout?.setOnClickListener {
            ActivityUtils.startFriendFacesActivity(requireContext())
        }
        binding?.wordCloudLayout?.setOnClickListener {
            ActivityUtils.startWordCloudActivity(requireContext())
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): NavigationFragment {
            return NavigationFragment()
        }
    }

    override fun initViewBinding(): FragmentNavigationBinding {
        return FragmentNavigationBinding.inflate(layoutInflater)
    }
}
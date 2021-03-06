package com.stupidtree.cloudliter.ui.main.navigation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.view.View
import com.stupidtree.cloudliter.databinding.FragmentConversationsBinding
import com.stupidtree.cloudliter.databinding.FragmentNavigationBinding
import com.stupidtree.cloudliter.service.socket.SocketIOClientService
import com.stupidtree.cloudliter.ui.base.BaseFragmentWithReceiver
import com.stupidtree.cloudliter.ui.main.conversations.ConversationsFragment
import com.stupidtree.cloudliter.utils.ActivityUtils

class NavigationFragment : BaseFragmentWithReceiver<NavigationViewModel, FragmentNavigationBinding>() {

    /**
     * 广播区
     */
    override var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

        }
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
        binding?.searchFriend?.setOnClickListener {
            //ActivityUtils.startSearchActivity(requireActivity())
            ActivityUtils.startMyFaceActivity(requireContext())
        }
        binding?.relationEvent?.setOnClickListener { ActivityUtils.startRelationEventActivity(requireContext()) }
        binding?.editGroup?.setOnClickListener { ActivityUtils.startGroupEditorActivity(requireActivity()) }
        binding?.scanQr?.setOnClickListener { ActivityUtils.startQRCodeActivity(requireContext()) }
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
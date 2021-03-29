package com.stupidtree.cloudliter.ui.main.contact

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.databinding.FragmentContactBinding
import com.stupidtree.cloudliter.service.socket.SocketIOClientService.Companion.RECEIVE_RELATION_EVENT
import com.stupidtree.style.base.BaseFragmentWithReceiver
import com.stupidtree.component.data.DataState
import com.stupidtree.cloudliter.ui.main.contact.group.ContactGroupFragment
import com.stupidtree.cloudliter.ui.main.contact.list.ContactListFragment
import com.stupidtree.cloudliter.ui.main.contact.popup.PopUpPickFriendFragment
import com.stupidtree.cloudliter.utils.ActivityUtils

/**
 * 联系人页面的Fragment
 */
class ContactFragment : BaseFragmentWithReceiver<ContactViewModel, FragmentContactBinding>() {

    var listFragment: ContactListFragment? = null
    var groupFragment: ContactGroupFragment? = null

    /**
     * 广播区
     */
    override var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            viewModel.startFetchUnread()
        }
    }

    override fun getIntentFilter(): IntentFilter {
        return IntentFilter(RECEIVE_RELATION_EVENT)
    }

    override fun getViewModelClass(): Class<ContactViewModel> {
        return ContactViewModel::class.java
    }

    private fun setUpButtons() {
        binding?.searchFriend?.setOnClickListener {
            ActivityUtils.startSearchActivity(requireActivity())
            //ActivityUtils.startMyFaceActivity(requireContext())
        }
        binding?.relationEvent?.setOnClickListener { ActivityUtils.startRelationEventActivity(requireContext()) }
        binding?.scanQr?.setOnClickListener { ActivityUtils.startQRCodeActivity(requireContext()) }
        binding?.createGroup?.setOnClickListener {
            PopUpPickFriendFragment(object : PopUpPickFriendFragment.OnConfirmListener {
                override fun onConfirm(userIds: List<String>) {
                    viewModel.startCreateGroup(userIds)
                }

            }, listOf()).show(parentFragmentManager, "create_group")
        }
    }

    override fun initViews(view: View) {
        setUpButtons()
        viewModel.unReadLiveData.observe(this, { integerDataState ->
            if (integerDataState.state === DataState.STATE.SUCCESS) {
                if (integerDataState.data!! > 0) {
                    binding?.unread?.visibility = View.VISIBLE
                    binding?.unread?.text = integerDataState.data.toString()
                } else {
                    binding?.unread?.visibility = View.GONE
                }
            } else {
                binding?.unread?.visibility = View.GONE
            }
        })
        viewModel.createGroupChatResult.observe(this){
            if(it.state==DataState.STATE.SUCCESS){
                Toast.makeText(requireContext(),R.string.create_group_success,Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(requireContext(),R.string.fail,Toast.LENGTH_SHORT).show()
            }
        }
        binding?.tabs?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showListFragment()
                    else -> showGroupFragment()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })
    }

    fun showListFragment() {
        if (listFragment == null) {
            listFragment = ContactListFragment.newInstance()
        }
        listFragment?.let {
            switchFragment(it, "list")
        }
    }

    fun showGroupFragment() {
        if (groupFragment == null) {
            groupFragment = ContactGroupFragment.newInstance()
        }
        groupFragment?.let {
            switchFragment(it, "group")
        }
    }

    private fun switchFragment(fragment: Fragment, tag: String) {
        val trans = childFragmentManager.beginTransaction()
        if (!fragment.isAdded) {
            trans.add(R.id.pager, fragment, tag)
        }
        for (f in childFragmentManager.fragments) {
            if (f != fragment) {
                trans.hide(f)
            }
        }
        trans.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).show(fragment).commit()
    }


    override fun onStart() {
        super.onStart()
        viewModel.startFetchUnread()
        when (binding?.tabs?.selectedTabPosition) {
            0 -> showListFragment()
            else -> showGroupFragment()
        }
    }


    companion object {
        @JvmStatic
        fun newInstance(): ContactFragment {
            return ContactFragment()
        }
    }

    override fun initViewBinding(): FragmentContactBinding {
        return FragmentContactBinding.inflate(layoutInflater)
    }
}
package com.stupidtree.cloudliter.ui.main.contact

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.View
import androidx.fragment.app.Fragment
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.databinding.FragmentContactBinding
import com.stupidtree.cloudliter.service.socket.SocketIOClientService.Companion.ACTION_RELATION_EVENT
import com.stupidtree.cloudliter.ui.base.BaseFragmentWithReceiver
import com.stupidtree.cloudliter.ui.base.BaseTabAdapter
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.main.contact.group.ContactGroupFragment
import com.stupidtree.cloudliter.ui.main.contact.list.ContactListFragment
import com.stupidtree.cloudliter.utils.ActivityUtils

/**
 * 联系人页面的Fragment
 */
class ContactFragment : BaseFragmentWithReceiver<ContactViewModel,FragmentContactBinding>() {

    /**
     * 广播区
     */
    override var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            viewModel.startFetchUnread()
        }
    }

    override fun getIntentFilter(): IntentFilter {
        return IntentFilter(ACTION_RELATION_EVENT)
    }

    override fun getViewModelClass(): Class<ContactViewModel> {
        return ContactViewModel::class.java
    }

    private fun setUpButtons() {
        binding?.searchFriend?.setOnClickListener { ActivityUtils.startSearchActivity(requireActivity()) }
        binding?.relationEvent?.setOnClickListener { ActivityUtils.startRelationEventActivity(requireContext()) }
        binding?.editGroup?.setOnClickListener { ActivityUtils.startGroupEditorActivity(requireActivity()) }
        binding?.scanQr?.setOnClickListener { ActivityUtils.startQRCodeActivity(requireContext()) }
    }

    override fun initViews(view: View) {
        setUpButtons()
        viewModel.unReadLiveData?.observe(this, { integerDataState ->
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
        binding?.pager?.adapter = object : BaseTabAdapter(childFragmentManager, 2) {
            override fun initItem(position: Int): Fragment {
                return if (position == 0) {
                    ContactListFragment.newInstance()
                } else {
                    ContactGroupFragment.newInstance()
                }
            }
            override fun getPageTitle(position: Int): CharSequence {
                return if (position == 0) getString(R.string.contact_friend_list) else getString(R.string.contact_friend_group)
            }
        }
        binding?.tabs?.setupWithViewPager(binding?.pager)
    }

    override fun onResume() {
        super.onResume()
        viewModel.startFetchUnread()
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
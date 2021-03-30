package com.stupidtree.cloudliter.ui.chat.read

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.UserRelation
import com.stupidtree.cloudliter.databinding.DialogBottomReadFriendBinding
import com.stupidtree.component.data.DataState
import com.stupidtree.style.base.BaseListAdapter.RefreshJudge
import com.stupidtree.style.widgets.TransparentModeledBottomSheetDialog

class PopUpReadUserFragment(val messageId: String, val conversationId: String) : TransparentModeledBottomSheetDialog<ReadUserViewModel, DialogBottomReadFriendBinding>() {

    lateinit var listAdapter: ReadUserListAdapter


    override fun getViewModelClass(): Class<ReadUserViewModel> {
        return ReadUserViewModel::class.java
    }

    override fun initViews(view: View) {
        //初始化一下列表的view
        listAdapter = ReadUserListAdapter(requireContext(), mutableListOf(), 1)
        binding?.list?.adapter = listAdapter
        binding?.list?.layoutManager = LinearLayoutManager(context)
        binding?.tabs?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding?.loading?.visibility = View.VISIBLE
                when (tab?.position) {
                    0 -> viewModel.startFetchData(messageId, conversationId, true)
                    else -> viewModel.startFetchData(messageId, conversationId, false)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })
        //当列表数据变更时，将自动调用本匿名函数
        viewModel.listData.observe(this, { contactListState ->
            binding?.loading?.visibility = View.GONE
            if (contactListState.state === DataState.STATE.SUCCESS) {
                //状态为”成功“，那么列表设置为可见，并通知列表适配器丝滑地更新列表项
                contactListState.data?.let {
                    listAdapter.notifyItemChangedSmooth(it)
                }
            }
        })


    }


    override fun onStart() {
        super.onStart()
        binding?.loading?.visibility = View.VISIBLE
        viewModel.startFetchData(messageId, conversationId, binding?.tabs?.selectedTabPosition == 0)
    }


    override fun getLayoutId(): Int {
        return R.layout.dialog_bottom_read_friend
    }

    override fun initViewBinding(v: View): DialogBottomReadFriendBinding {
        return DialogBottomReadFriendBinding.bind(v)
    }
}
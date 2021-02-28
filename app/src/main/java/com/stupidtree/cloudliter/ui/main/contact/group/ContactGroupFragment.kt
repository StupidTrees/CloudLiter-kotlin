package com.stupidtree.cloudliter.ui.main.contact.group

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.donkingliang.groupedadapter.adapter.GroupedRecyclerViewAdapter
import com.donkingliang.groupedadapter.holder.BaseViewHolder
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.UserRelation
import com.stupidtree.cloudliter.databinding.FragmentContactListBinding
import com.stupidtree.cloudliter.service.socket.SocketIOClientService
import com.stupidtree.cloudliter.ui.base.BaseFragmentWithReceiver
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.utils.ActivityUtils

/**
 * 联系人页面的Fragment
 */
@SuppressLint("NonConstantResourceId")
class ContactGroupFragment : BaseFragmentWithReceiver<ContactGroupViewModel,FragmentContactListBinding>() {


    /**
     * 适配器区
     */
    lateinit var listAdapter //列表适配器
            : GroupedFriendsListAdapter

    /**
     * 广播区
     */
    override var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.e("好友事件", intent.toString())
            viewModel.startFetchData()
        }
    }

    override fun getIntentFilter(): IntentFilter {
        return IntentFilter(SocketIOClientService.ACTION_RELATION_EVENT)
    }

    override fun getViewModelClass(): Class<ContactGroupViewModel> {
        return ContactGroupViewModel::class.java
    }

    override fun initViews(view: View) {
        //初始化一下列表的view
        listAdapter = GroupedFriendsListAdapter(context)
        binding?.list?.adapter = listAdapter
        binding?.list?.layoutManager = LinearLayoutManager(context)
        listAdapter.setOnHeaderClickListener { _: GroupedRecyclerViewAdapter?, _: BaseViewHolder?, groupPosition: Int ->
            if (groupPosition >= listAdapter.groupEntities.size) {
                ActivityUtils.startGroupEditorActivity(requireContext())
            } else {
                binding?.list?.let { listAdapter.toggleGroup(it, groupPosition) }
            }
        }
        listAdapter.setOnChildClickListener { _: GroupedRecyclerViewAdapter?, _: BaseViewHolder?, groupPosition: Int, childPosition: Int ->
            val ur = listAdapter.groupEntities[groupPosition].getChildAt(childPosition)
            ActivityUtils.startProfileActivity(requireActivity(), ur.friendId)
        }
        //设置下拉刷新
        binding?.refresh?.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent)
        binding?.refresh?.setOnRefreshListener { viewModel.startFetchData() }
        viewModel.listData.observe(this, { contactListState: DataState<List<UserRelation>?> ->
            binding?.refresh?.isRefreshing = false
            if (contactListState.state === DataState.STATE.SUCCESS) {
                //状态为”成功“，那么列表设置为可见，并通知列表适配器丝滑地更新列表项
                contactListState.data?.let { listAdapter.notifyItemChangedSmooth(it) }
                //listAdapter.notifyItemChangedSmooth(contactListState.getData(), (oldData, newData) -> !Objects.equals(oldData, newData) || !Objects.equals(oldData.getRemark(), newData.getRemark()));
                if (contactListState.data!!.isNotEmpty()) {
                    binding?.listWhole?.visibility = View.VISIBLE
                    binding?.placeHolder?.visibility = View.GONE
                } else {
                    binding?.listWhole?.visibility = View.GONE
                    binding?.placeHolder?.visibility = View.VISIBLE
                    binding?.placeHolderText?.setText(R.string.no_contact)
                }
            } else if (contactListState.state === DataState.STATE.NOT_LOGGED_IN) {
                //状态为”未登录“，那么设置”未登录“内东西为可见，隐藏列表
                binding?.listWhole?.visibility = View.GONE
                binding?.placeHolder?.visibility = View.VISIBLE
                binding?.placeHolderText?.setText(R.string.click_to_log_in)
                binding?.placeHolder?.setOnClickListener {
                    ActivityUtils.startLoginActivity(requireContext())
                }
            } else if (contactListState.state === DataState.STATE.FETCH_FAILED) {
                //状态为”获取失败“，那么弹出提示
                binding?.listWhole?.visibility = View.GONE
                binding?.placeHolder?.visibility = View.VISIBLE
                binding?.placeHolderText?.setText(R.string.fetch_failed)
            }
        })
    }

    override fun onStart() {
        super.onStart()
        viewModel.startFetchData()
    }

    companion object {
        @JvmStatic
        fun newInstance(): ContactGroupFragment {
            return ContactGroupFragment()
        }
    }

    override fun initViewBinding(): FragmentContactListBinding {
        return FragmentContactListBinding.inflate(layoutInflater)
    }
}
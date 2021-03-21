package com.stupidtree.cloudliter.ui.face.permission

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.databinding.ActivityFaceWhiteListBinding
import com.stupidtree.cloudliter.ui.main.contact.popup.PopUpPickFriendFragment
import com.stupidtree.style.base.BaseActivity
import com.stupidtree.style.base.BaseListAdapter
import com.stupidtree.component.data.DataState
import com.stupidtree.style.widgets.PopUpText

class FaceWhiteListActivity : BaseActivity<FaceWhiteListViewModel, ActivityFaceWhiteListBinding>() {

    var listAdapter: FaceWhiteListListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setWindowParams(statusBar = true, darkColor = true, navi = false)
        setToolbarActionBack(binding.toolbar)
    }

    override fun initViewBinding(): ActivityFaceWhiteListBinding {
        return ActivityFaceWhiteListBinding.inflate(layoutInflater)
    }

    override fun getViewModelClass(): Class<FaceWhiteListViewModel> {
        return FaceWhiteListViewModel::class.java
    }

    override fun initViews() {
        listAdapter = FaceWhiteListListAdapter(this, mutableListOf())
        binding.list.adapter = listAdapter
        binding.list.layoutManager = LinearLayoutManager(this)
        binding.refresh.setColorSchemeColors(getColorPrimary())
        binding.fab.setOnClickListener {
            val ids = mutableListOf<String>()
            for(r in viewModel.whiteListLiveData.value?.data?: listOf()){
                r.userId?.let { it1 -> ids.add(it1) }
            }
            PopUpPickFriendFragment(object :PopUpPickFriendFragment.OnConfirmListener{
                override fun onConfirm(userIds: List<String>) {
                    viewModel.addWhitelist(userIds)
                }

            },ids).show(supportFragmentManager,"pick")
        }
        viewModel.whiteListLiveData.observe(this) { it ->
            binding.refresh.isRefreshing = false
            if (it.state == DataState.STATE.SUCCESS) {
                listAdapter?.notifyItemChangedSmooth(it.data!!)
            }
        }
        viewModel.addWhitelistResult.observe(this){
            if (it.state == DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(),R.string.add_success,Toast.LENGTH_SHORT).show()
                viewModel.startRefresh()
            }
        }
        binding.refresh.setOnRefreshListener {
            refresh()
        }
        listAdapter?.setOnItemClickListener(object : BaseListAdapter.OnItemClickListener<FaceWhiteListEntity> {
            override fun onItemClick(data: FaceWhiteListEntity, card: View?, position: Int) {
                PopUpText().setTitle(R.string.ensure_delete)
                        .setOnConfirmListener(object : PopUpText.OnConfirmListener {
                            override fun OnConfirm() {
                                data.userId?.let { viewModel.deleteFaceWhiteList(it) }
                            }
                        }).show(supportFragmentManager, "delete")
            }

        })

        viewModel.deleteResult.observe(this) {
            if (it.state == DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(), R.string.delete_ok, Toast.LENGTH_SHORT).show()
                viewModel.startRefresh()
            } else {
                Toast.makeText(getThis(), R.string.fail, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun refresh() {
        viewModel.startRefresh()
        binding.refresh.isRefreshing = true
    }

    override fun onStart() {
        super.onStart()
        refresh()
    }
}
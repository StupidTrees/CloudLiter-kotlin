package com.stupidtree.cloudliter.ui.groupchat

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.UserRelation
import com.stupidtree.cloudliter.databinding.ActivityCreateGroupChatBinding
import com.stupidtree.cloudliter.ui.main.contact.popup.PickFriendListAdapter
import com.stupidtree.component.data.DataState
import com.stupidtree.style.base.BaseActivity
import com.stupidtree.style.base.BaseListAdapter
import java.util.*

class CreateGroupChatActivity : BaseActivity<CreateGroupChatViewModel, ActivityCreateGroupChatBinding>() {

    lateinit var listAdapter: PickFriendListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolbarActionBack(binding.toolbar)
    }

    override fun initViewBinding(): ActivityCreateGroupChatBinding {
        return ActivityCreateGroupChatBinding.inflate(layoutInflater)
    }

    override fun getViewModelClass(): Class<CreateGroupChatViewModel> {
        return CreateGroupChatViewModel::class.java
    }


    override fun initViews() {
        listAdapter = PickFriendListAdapter(this, mutableListOf(), 1)
        binding.list.adapter = listAdapter
        binding.list.layoutManager = LinearLayoutManager(this)
        binding.refresh.setOnRefreshListener {
            viewModel.startFetchData()
        }
        binding.done.setOnClickListener {
            val ids = mutableListOf<String>()
            for (ur in listAdapter.getCheckedData()) {
                ids.add(ur.friendId)
            }
            if (ids.isNotEmpty()) {
                viewModel.startCreateGroup(ids, binding.name.text.toString())
            }
        }
        binding.name.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    binding.done.hide()
                } else {
                    binding.done.show()
                }
            }

        })
        listAdapter.setOnItemClickListener(object : BaseListAdapter.OnItemClickListener<UserRelation> {
            override fun onItemClick(data: UserRelation, card: View?, position: Int) {
                val checked = listAdapter.getCheckedData()
                if (checked.isEmpty()) {
                    binding.done.hide()
                } else {
                    binding.done.show()
                    val showGroup = mutableListOf<String>()
                    for (i in 0 until 3.coerceAtMost(checked.size)) {
                        showGroup.add(checked[i].getName())
                    }
                    if (checked.size > 3) {
                        showGroup.add("...")
                    }
                    binding.name.setText(showGroup.joinToString(separator = " "))
                }
            }

        })

        viewModel.createGroupChatResult.observe(this) {
            if (it.state == DataState.STATE.SUCCESS) {
                Toast.makeText(this, R.string.create_group_success, Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, R.string.fail, Toast.LENGTH_SHORT).show()
            }
        }

        //当列表数据变更时，将自动调用本匿名函数
        viewModel.listData.observe(this, { contactListState ->
            binding.refresh.isRefreshing = false
            if (contactListState.state === DataState.STATE.SUCCESS) {
                val sortedList = contactListState.data?.sortedWith(comparator = object : Comparator<UserRelation> {
                    override fun compare(o1: UserRelation?, o2: UserRelation?): Int {
                        if (o1 == null || o2 == null) return -1
                        return o1.getName().compareTo(o2.getName())
                    }
                })

                //状态为”成功“，那么列表设置为可见，并通知列表适配器丝滑地更新列表项
                sortedList?.let {
                    listAdapter.notifyItemChangedSmooth(it, object : BaseListAdapter.RefreshJudge<UserRelation> {
                        override fun judge(oldData: UserRelation, newData: UserRelation): Boolean {
                            return oldData != newData || oldData.remark != newData.remark
                        }
                    })
                }
            }
        })
    }


    override fun onStart() {
        super.onStart()
        viewModel.startFetchData()
    }

}
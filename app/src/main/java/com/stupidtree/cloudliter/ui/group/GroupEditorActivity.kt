package com.stupidtree.cloudliter.ui.group

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.RelationGroup
import com.stupidtree.cloudliter.databinding.ActivityGroupEditorBinding
import com.stupidtree.style.base.BaseActivity
import com.stupidtree.style.base.BaseListAdapter
import com.stupidtree.style.base.BaseListAdapter.RefreshJudge
import com.stupidtree.component.data.DataState
import com.stupidtree.style.widgets.PopUpEditText
import com.stupidtree.style.widgets.PopUpText
import java.util.*

class GroupEditorActivity : BaseActivity<GroupEditorViewModel, ActivityGroupEditorBinding>() {


    /**
     * 适配器
     */
    var listAdapter: GroupListAdapter? = null

    override fun getViewModelClass(): Class<GroupEditorViewModel> {
        return GroupEditorViewModel::class.java
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolbarActionBack(binding.toolbar)

    }

    override fun initViews() {

        listAdapter = GroupListAdapter(this, LinkedList())
        binding.list.adapter = listAdapter
        binding.list.layoutManager = LinearLayoutManager(this)
        viewModel.listData?.observe(this, { listDataState ->
            if (listDataState.state === DataState.STATE.SUCCESS) {
                listAdapter!!.notifyItemChangedSmooth(listDataState.data!!, object : RefreshJudge<RelationGroup> {
                    override fun judge(oldData: RelationGroup, newData: RelationGroup): Boolean {
                        return oldData != newData
                    }
                })
            }
        })


        //点击按钮添加分组
        binding.add.setOnClickListener {
            PopUpEditText()
                    .setTitle(R.string.add_group)
                    .setText("")
                    .setOnConfirmListener(object : PopUpEditText.OnConfirmListener {
                        override fun OnConfirm(text: String) {
                            //控制viewModel发起添加分组请求
                            viewModel.startAddGroup(text)
                        }
                    })
                    .show(supportFragmentManager, "edit")
        }

        //点击删除分组
        listAdapter!!.onDeleteClickListener = object:GroupListAdapter.OnDeleteClickListener {
            override fun onDeleteClick(button: View?, group: RelationGroup, position: Int) {
                PopUpText().setTitle(R.string.ensure_delete).setText("").setOnConfirmListener(
                        object : PopUpText.OnConfirmListener {
                            override fun OnConfirm() {
                                group.id?.let { viewModel.startDeleteGroup(it) }
                            }
                        }
                ).show(supportFragmentManager, "confirm")
            }
        }

        listAdapter?.setOnItemClickListener(object :BaseListAdapter.OnItemClickListener<RelationGroup>{
            override fun onItemClick(data: RelationGroup, card: View?, position: Int) {
                PopUpEditText().setTitle(R.string.change_group_name).setOnConfirmListener(object:PopUpEditText.OnConfirmListener{
                    override fun OnConfirm(text: String) {
                        viewModel.startRenameGroup(data.id!!,text)
                    }

                }).setText(data.groupName).show(supportFragmentManager,"rename")
            }

        })

        viewModel.addGroupResult?.observe(this, { stringDataState: DataState<String?> ->
            if (stringDataState.state === DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(), R.string.add_ok, Toast.LENGTH_SHORT).show()
                viewModel.startRefresh()
            } else {
                Toast.makeText(applicationContext, R.string.fail, Toast.LENGTH_SHORT).show()
            }
        })
        viewModel.deleteGroupResult?.observe(this, { stringDataState: DataState<String?> ->
            if (stringDataState.state === DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(), R.string.delete_ok, Toast.LENGTH_SHORT).show()
                viewModel.startRefresh()
            } else {
                Toast.makeText(applicationContext, R.string.fail, Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.renameGroupResult?.observe(this){
            if (it.state === DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(), R.string.rename_ok, Toast.LENGTH_SHORT).show()
                viewModel.startRefresh()
            } else {
                Toast.makeText(applicationContext, R.string.fail, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.startRefresh()
    }

    override fun initViewBinding(): ActivityGroupEditorBinding {
        return ActivityGroupEditorBinding.inflate(layoutInflater)
    }
}
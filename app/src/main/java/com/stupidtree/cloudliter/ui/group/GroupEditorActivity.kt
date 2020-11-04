package com.stupidtree.cloudliter.ui.group

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.RelationGroup
import com.stupidtree.cloudliter.ui.base.BaseActivity
import com.stupidtree.cloudliter.ui.base.BaseListAdapter.RefreshJudge
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.widgets.PopUpEditText
import com.stupidtree.cloudliter.ui.widgets.PopUpText
import java.util.*

class GroupEditorActivity : BaseActivity<GroupEditorViewModel>() {
    /**
     * View绑定区
     */
    @JvmField
    @BindView(R.id.toolbar)
    var toolbar: Toolbar? = null

    @JvmField
    @BindView(R.id.list)
    var list: RecyclerView? = null

    @JvmField
    @BindView(R.id.add)
    var add: //这里是添加按钮
            FloatingActionButton? = null

    /**
     * 适配器
     */
    var listAdapter: GroupListAdapter? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_group_editor
    }

    override fun getViewModelClass(): Class<GroupEditorViewModel> {
        return GroupEditorViewModel::class.java
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setWindowParams(true, true, false)
    }

    override fun initViews() {
        setToolbarActionBack(toolbar!!)
        listAdapter = GroupListAdapter(this, LinkedList())
        list!!.adapter = listAdapter
        list!!.layoutManager = LinearLayoutManager(this)
        viewModel!!.listData?.observe(this, Observer { listDataState ->
            if (listDataState.state === DataState.STATE.SUCCESS) {
                listAdapter!!.notifyItemChangedSmooth(listDataState.data!!, object : RefreshJudge<RelationGroup> {
                    override fun judge(oldData: RelationGroup, newData: RelationGroup): Boolean {
                        return oldData == newData
                    }
                })
            }
        })


        //点击按钮添加分组
        add!!.setOnClickListener { view: View? ->
            PopUpEditText()
                    .setTitle(R.string.add_group)
                    .setText("")
                    .setOnConfirmListener(object : PopUpEditText.OnConfirmListener {
                        override fun OnConfirm(text: String) {
                            //控制viewModel发起添加分组请求
                            viewModel!!.startAddGroup(text)
                        }
                    })
                    .show(supportFragmentManager, "edit")
        }

        //点击删除分组
        listAdapter!!.setOnDeleteClickListener { button: View?, group: RelationGroup, position: Int ->
            PopUpText().setTitle(R.string.ensure_delete).setText("").setOnConfirmListener(
                    object : PopUpText.OnConfirmListener {
                        override fun OnConfirm() {
                            group.id?.let { viewModel!!.startDeleteGroup(it) }
                        }
                    }
            ).show(supportFragmentManager, "confirm")
        }
        viewModel!!.addGroupResult?.observe(this, Observer { stringDataState: DataState<String?> ->
            if (stringDataState.state === DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(), R.string.add_ok, Toast.LENGTH_SHORT).show()
                viewModel!!.startRefresh()
            } else {
                Toast.makeText(applicationContext, "失败", Toast.LENGTH_SHORT).show()
            }
        })
        viewModel!!.deleteGroupResult?.observe(this, Observer { stringDataState: DataState<String?> ->
            if (stringDataState.state === DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(), R.string.delete_ok, Toast.LENGTH_SHORT).show()
                viewModel!!.startRefresh()
            } else {
                Toast.makeText(applicationContext, "失败", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel!!.startRefresh()
    }
}
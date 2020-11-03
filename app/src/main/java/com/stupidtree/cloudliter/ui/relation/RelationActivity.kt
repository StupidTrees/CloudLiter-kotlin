package com.stupidtree.cloudliter.ui.relation

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import butterknife.BindView
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.ui.base.BaseActivity
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.widgets.PopUpEditText

class RelationActivity : BaseActivity<RelationViewModel>() {
    @JvmField
    @BindView(R.id.remark_layout)
    var remarkLayout: ViewGroup? = null

    @JvmField
    @BindView(R.id.remark)
    var remarkText: TextView? = null

    @JvmField
    @BindView(R.id.toolbar)
    var toolbar: Toolbar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setWindowParams(true, true, false)
        setToolbarActionBack(toolbar!!)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_relation
    }

    override fun getViewModelClass(): Class<RelationViewModel> {
        return RelationViewModel::class.java
    }

    override fun initViews() {
        remarkLayout!!.setOnClickListener { view: View? ->
            val up = viewModel!!.relationData!!.value
            if (up != null && up.state === DataState.STATE.SUCCESS) {
                PopUpEditText()
                        .setTitle(R.string.prompt_set_remark)
                        .setText(up.data!!.remark)
                        .setOnConfirmListener(object :PopUpEditText.OnConfirmListener{
                            override fun OnConfirm(text: String) {
                                //控制viewModel发起更改昵称请求
                                viewModel!!.startChangeRemark(text)
                            }
                        })
                        .show(supportFragmentManager, "edit")
            }
        }
        viewModel!!.changeRemarkResult!!.observe(this, Observer { stringDataState: DataState<String?>? ->
            if (stringDataState!!.state === DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(), R.string.avatar_change_success, Toast.LENGTH_SHORT).show()
                intent.getStringExtra("friendId")?.let { viewModel!!.startFetchRelationData(it) }
            } else {
                Toast.makeText(applicationContext, "失败", Toast.LENGTH_SHORT).show()
            }
        })
        viewModel!!.relationData!!.observe(this, Observer { friendContactDataState ->
            if (friendContactDataState!!.state === DataState.STATE.SUCCESS) {
                remarkText!!.text = friendContactDataState!!.data!!.remark
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (intent.hasExtra("friendId")) {
            intent.getStringExtra("friendId")?.let { viewModel!!.startFetchRelationData(it) }
        }
    }
}
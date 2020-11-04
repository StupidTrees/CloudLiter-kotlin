package com.stupidtree.cloudliter.ui.main.contact.group

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import com.donkingliang.groupedadapter.adapter.GroupedRecyclerViewAdapter
import com.donkingliang.groupedadapter.holder.BaseViewHolder
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.UserRelation
import com.stupidtree.cloudliter.ui.base.BaseFragment
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.utils.ActivityUtils

/**
 * 联系人页面的Fragment
 */
@SuppressLint("NonConstantResourceId")
class ContactGroupFragment : BaseFragment<ContactGroupViewModel>() {
    /**
     * View绑定区
     */
    @BindView(R.id.place_holder)
    lateinit var placeHolder //列表无内容时显示的布局
            : ViewGroup

    @BindView(R.id.place_holder_text)
    lateinit var placeHolderText //不显示列表时显示文字
            : TextView

    @BindView(R.id.list)
    lateinit var list //列表
            : RecyclerView

    @BindView(R.id.refresh)
    lateinit var refreshLayout: SwipeRefreshLayout

    /**
     * 适配器区
     */
    lateinit var listAdapter //列表适配器
            : GroupedFriendsListAdapter

    override fun getViewModelClass(): Class<ContactGroupViewModel> {
        return ContactGroupViewModel::class.java
    }

    override fun initViews(view: View) {
        //初始化一下列表的view
        listAdapter = GroupedFriendsListAdapter(context)
        list.adapter = listAdapter
        list.layoutManager = LinearLayoutManager(context)
        listAdapter.setOnHeaderClickListener { _: GroupedRecyclerViewAdapter?, _: BaseViewHolder?, groupPosition: Int -> listAdapter.toggleGroup(list, groupPosition) }
        listAdapter.setOnChildClickListener { _: GroupedRecyclerViewAdapter?, _: BaseViewHolder?, groupPosition: Int, childPosition: Int ->
            val ur = listAdapter.groupEntities[groupPosition].getChildAt(childPosition)
            ActivityUtils.startProfileActivity(requireActivity(), ur.friendId.toString())
        }
        //设置下拉刷新
        refreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent)
        refreshLayout.setOnRefreshListener { viewModel!!.startFetchData() }
        viewModel!!.listData?.observe(this, Observer { contactListState: DataState<List<UserRelation>?> ->
            refreshLayout.isRefreshing = false
            if (contactListState.state === DataState.STATE.SUCCESS) {
                //状态为”成功“，那么列表设置为可见，并通知列表适配器丝滑地更新列表项
                contactListState.data?.let { listAdapter.notifyItemChangedSmooth(it) }
                //listAdapter.notifyItemChangedSmooth(contactListState.getData(), (oldData, newData) -> !Objects.equals(oldData, newData) || !Objects.equals(oldData.getRemark(), newData.getRemark()));
                if (contactListState.data!!.isNotEmpty()) {
                    list.visibility = View.VISIBLE
                    placeHolder.visibility = View.GONE
                } else {
                    list.visibility = View.GONE
                    placeHolder.visibility = View.VISIBLE
                    placeHolderText.setText(R.string.no_contact)
                }
            } else if (contactListState.state === DataState.STATE.NOT_LOGGED_IN) {
                //状态为”未登录“，那么设置”未登录“内东西为可见，隐藏列表
                list.visibility = View.GONE
                placeHolder.visibility = View.VISIBLE
                placeHolderText.setText(R.string.not_logged_in)
            } else if (contactListState.state === DataState.STATE.FETCH_FAILED) {
                //状态为”获取失败“，那么弹出提示
                list.visibility = View.GONE
                placeHolder.visibility = View.VISIBLE
                placeHolderText.setText(R.string.fetch_failed)
            }
        })
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_contact_list
    }

    override fun onResume() {
        super.onResume()
        viewModel!!.startFetchData()
    }

    companion object {
        @JvmStatic
        fun newInstance(): ContactGroupFragment {
            return ContactGroupFragment()
        }
    }
}
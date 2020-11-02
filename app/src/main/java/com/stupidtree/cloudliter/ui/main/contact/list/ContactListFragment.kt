package com.stupidtree.cloudliter.ui.main.contact.list

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.UserRelation
import com.stupidtree.cloudliter.ui.base.BaseFragment
import com.stupidtree.cloudliter.ui.base.BaseListAdapter
import com.stupidtree.cloudliter.ui.base.BaseListAdapter.RefreshJudge
import com.stupidtree.cloudliter.ui.base.BaseViewHolder
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.utils.ActivityUtils
import com.stupidtree.cloudliter.utils.ImageUtils
import com.stupidtree.cloudliter.utils.TextUtils
import java.util.*

/**
 * 联系人页面的Fragment
 */
class ContactListFragment : BaseFragment<ContactListViewModel>() {
    /**
     * View绑定区
     */
    @JvmField
    @BindView(R.id.place_holder)
    var placeHolder //列表无内容时显示的布局
            : ViewGroup? = null

    @JvmField
    @BindView(R.id.place_holder_text)
    var placeHolderText //不显示列表时显示文字
            : TextView? = null

    @JvmField
    @BindView(R.id.list)
    var list //列表
            : RecyclerView? = null

    @JvmField
    @BindView(R.id.refresh)
    var refreshLayout: SwipeRefreshLayout? = null

    /**
     * 适配器区
     */
    var listAdapter //列表适配器
            : XListAdapter? = null

    override fun getViewModelClass(): Class<ContactListViewModel> {
        return ContactListViewModel::class.java
    }

    override fun initViews(view: View) {
        //初始化一下列表的view
        listAdapter = context?.let { XListAdapter(it, LinkedList()) }
        list!!.adapter = listAdapter
        list!!.layoutManager = LinearLayoutManager(context)
        listAdapter!!.setOnItemClickListener(object :BaseListAdapter.OnItemClickListener<UserRelation> {
            override fun onItemClick(data: UserRelation, card: View?, position: Int) {
                ActivityUtils.startProfileActivity(requireActivity(), data.friendId.toString())
            }
        })
        //设置下拉刷新
        refreshLayout!!.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent)
        refreshLayout!!.setOnRefreshListener { viewModel!!.startFetchData() }

        //当列表数据变更时，将自动调用本匿名函数
        viewModel!!.listData.observe(this, Observer { contactListState: DataState<List<UserRelation>> ->
            refreshLayout!!.isRefreshing = false
            if (contactListState.state === DataState.STATE.SUCCESS) {
                //状态为”成功“，那么列表设置为可见，并通知列表适配器丝滑地更新列表项
                listAdapter!!.notifyItemChangedSmooth(contactListState.data!!, object : RefreshJudge<UserRelation> {
                    override fun judge(oldData: UserRelation, newData: UserRelation): Boolean {
                        return oldData != newData || oldData.remark != newData.remark
                    }
                })
                if (contactListState.data!!.size > 0) {
                    list!!.visibility = View.VISIBLE
                    placeHolder!!.visibility = View.GONE
                } else {
                    list!!.visibility = View.GONE
                    placeHolder!!.visibility = View.VISIBLE
                    placeHolderText!!.setText(R.string.no_contact)
                }
            } else if (contactListState.state === DataState.STATE.NOT_LOGGED_IN) {
                //状态为”未登录“，那么设置”未登录“内东西为可见，隐藏列表
                list!!.visibility = View.GONE
                placeHolder!!.visibility = View.VISIBLE
                placeHolderText!!.setText(R.string.not_logged_in)
            } else if (contactListState.state === DataState.STATE.FETCH_FAILED) {
                //状态为”获取失败“，那么弹出提示
                list!!.visibility = View.GONE
                placeHolder!!.visibility = View.VISIBLE
                placeHolderText!!.setText(R.string.fetch_failed)
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

    /**
     * 定义本页面的列表适配器
     */
    class XListAdapter(mContext: Context, mBeans: MutableList<UserRelation>) : BaseListAdapter<UserRelation, XListAdapter.XHolder>(mContext!!, mBeans) {
        override fun getLayoutId(viewType: Int): Int {
            return R.layout.fragment_contact_list_item
        }

        protected override fun bindHolder(holder: XHolder, data: UserRelation?, position: Int) {
            if (data != null) {
                //显示头像
                ImageUtils.loadAvatarInto(mContext, data.friendAvatar, holder.avatar!!)
                //显示名称(备注)
                if (!TextUtils.isEmpty(data.remark)) {
                    holder.name!!.text = data.remark
                } else {
                    holder.name!!.text = data.friendNickname
                }
                //设置点击事件
                if (mOnItemClickListener != null) {
                    holder.item!!.setOnClickListener { view: View? -> mOnItemClickListener!!.onItemClick(data, view, position) }
                }
            }
        }

        override fun createViewHolder(v: View, viewType: Int): XHolder {
            return XHolder(v)
        }

        class XHolder(itemView: View) : BaseViewHolder(itemView) {
            //ButterKnife 永远的神
            @JvmField
            @BindView(R.id.name)
            var name: TextView? = null

            @JvmField
            @BindView(R.id.item)
            var item: ViewGroup? = null

            @JvmField
            @BindView(R.id.avatar)
            var avatar: ImageView? = null
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): ContactListFragment {
            return ContactListFragment()
        }
    }
}
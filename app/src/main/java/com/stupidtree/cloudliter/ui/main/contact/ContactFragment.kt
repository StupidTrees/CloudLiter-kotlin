package com.stupidtree.cloudliter.ui.main.contact

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import butterknife.BindView
import com.google.android.material.tabs.TabLayout
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.UserRelation
import com.stupidtree.cloudliter.ui.base.*
import com.stupidtree.cloudliter.ui.main.contact.group.ContactGroupFragment
import com.stupidtree.cloudliter.ui.main.contact.list.ContactListFragment
import com.stupidtree.cloudliter.utils.ActivityUtils
import com.stupidtree.cloudliter.utils.ImageUtils
import com.stupidtree.cloudliter.utils.TextUtils

/**
 * 联系人页面的Fragment
 */
class ContactFragment : BaseFragment<ContactViewModel>() {
    /**
     * View绑定区
     */
    @JvmField
    @BindView(R.id.search_friend)
    var searchFriendButton: View? = null

    @JvmField
    @BindView(R.id.relation_event)
    var relationEventButton: View? = null

    @JvmField
    @BindView(R.id.edit_group)
    var editGroupButton: View? = null

    @JvmField
    @BindView(R.id.unread)
    var unreadText: TextView? = null

    @JvmField
    @BindView(R.id.pager)
    var pager: ViewPager? = null

    @JvmField
    @BindView(R.id.tabs)
    var tabs: TabLayout? = null
    override fun getViewModelClass(): Class<ContactViewModel> {
        return ContactViewModel::class.java
    }

    private fun setUpButtons() {
        searchFriendButton!!.setOnClickListener { view: View? -> ActivityUtils.startSearchActivity(requireContext()) }
        relationEventButton!!.setOnClickListener { view: View? -> ActivityUtils.startRelationEventActivity(requireContext()) }
        editGroupButton!!.setOnClickListener { view: View? -> ActivityUtils.startGroupEditorActivity(requireActivity()) }
    }

    override fun initViews(view: View) {
        setUpButtons()
        viewModel!!.unReadLiveData?.observe(this, Observer { integerDataState ->
            if (integerDataState.state === DataState.STATE.SUCCESS) {
                if (integerDataState.data!! > 0) {
                    unreadText!!.visibility = View.VISIBLE
                    unreadText!!.text = integerDataState.data.toString()
                } else {
                    unreadText!!.visibility = View.GONE
                }
            } else {
                unreadText!!.visibility = View.GONE
            }
        })
        pager!!.adapter = object : BaseTabAdapter(childFragmentManager, 2) {
            override fun initItem(position: Int): Fragment {
                return if (position == 0) {
                    ContactListFragment.newInstance()
                } else {
                    ContactGroupFragment.newInstance()
                }
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return if (position == 0) getString(R.string.contact_friend_list) else getString(R.string.contact_friend_group)
            }
        }
        tabs!!.setupWithViewPager(pager)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_contact
    }

    override fun onResume() {
        super.onResume()
        viewModel!!.startFetchData()
        viewModel!!.startFetchUnread()
    }

    /**
     * 定义本页面的列表适配器
     */
    internal class XListAdapter(mContext: Context, mBeans:MutableList<UserRelation>) : BaseListAdapter<UserRelation, XListAdapter.XHolder>(mContext, mBeans) {
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

        internal class XHolder(itemView: View) : BaseViewHolder(itemView) {
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
        fun newInstance(): ContactFragment {
            return ContactFragment()
        }
    }
}
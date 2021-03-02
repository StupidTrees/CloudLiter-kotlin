package com.stupidtree.cloudliter.ui.main.contact.list

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.UserRelation
import com.stupidtree.cloudliter.databinding.FragmentContactListBinding
import com.stupidtree.cloudliter.databinding.FragmentContactListItemBinding
import com.stupidtree.cloudliter.service.socket.SocketIOClientService
import com.stupidtree.cloudliter.ui.base.BaseFragmentWithReceiver
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
class ContactListFragment : BaseFragmentWithReceiver<ContactListViewModel,FragmentContactListBinding>() {

    /**
     * 适配器区
     */
    var listAdapter //列表适配器
            : XListAdapter? = null


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
        return IntentFilter(SocketIOClientService.RECEIVE_RELATION_EVENT)
    }

    override fun getViewModelClass(): Class<ContactListViewModel> {
        return ContactListViewModel::class.java
    }

    override fun initViews(view: View) {
        //初始化一下列表的view
        listAdapter = context?.let { XListAdapter(it, LinkedList()) }
        binding?.list?.adapter = listAdapter
        binding?.list?.layoutManager = LinearLayoutManager(context)
        listAdapter!!.setOnItemClickListener(object :BaseListAdapter.OnItemClickListener<UserRelation> {
            override fun onItemClick(data: UserRelation, card: View?, position: Int) {
                ActivityUtils.startProfileActivity(requireActivity(), data.friendId)
            }
        })
        //设置下拉刷新
        binding?.refresh?.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent)
        binding?.refresh?.setOnRefreshListener { viewModel.startFetchData() }

        //当列表数据变更时，将自动调用本匿名函数
        viewModel.listData.observe(this, { contactListState: DataState<List<UserRelation>?> ->
            binding?.refresh?.isRefreshing = false
            if (contactListState.state === DataState.STATE.SUCCESS) {
                val newList = contactListState.data!!.sortedWith(comparator = object:Comparator<UserRelation>{
                    override fun compare(o1: UserRelation?, o2: UserRelation?): Int {
                        if(o1==null||o2==null) return -1
                        val name1:String = if(o1.remark.isNullOrEmpty()) o1.friendNickname.toString() else o1.remark.toString()
                        val name2:String = if(o2.remark.isNullOrEmpty()) o2.friendNickname.toString() else o2.remark.toString()
                        return name1.compareTo(name2)
                    }
                })
                //状态为”成功“，那么列表设置为可见，并通知列表适配器丝滑地更新列表项
                listAdapter!!.notifyItemChangedSmooth(newList, object : RefreshJudge<UserRelation> {
                    override fun judge(oldData: UserRelation, newData: UserRelation): Boolean {
                        return oldData != newData || oldData.remark != newData.remark
                    }
                })
                if (contactListState.data!!.isNotEmpty()) {
                    binding?.list?.visibility = View.VISIBLE
                    
                    binding?.placeHolder?.visibility = View.GONE
                } else {
                    binding?.list?.visibility = View.GONE
                    binding?.placeHolder?.visibility = View.VISIBLE
                    binding?.placeHolderText?.setText(R.string.no_contact)
                }
            } else if (contactListState.state === DataState.STATE.NOT_LOGGED_IN) {
                //状态为”未登录“，那么设置”未登录“内东西为可见，隐藏列表
                binding?.list?.visibility = View.GONE
                binding?.placeHolder?.visibility = View.VISIBLE
                binding?.placeHolderText?.setText(R.string.click_to_log_in)
                binding?.placeHolder?.setOnClickListener {
                    ActivityUtils.startLoginActivity(requireContext())
                }
            } else if (contactListState.state === DataState.STATE.FETCH_FAILED) {
                //状态为”获取失败“，那么弹出提示
                binding?.list?.visibility = View.GONE
                binding?.placeHolder?.visibility = View.VISIBLE
                binding?.placeHolderText?.setText(R.string.fetch_failed)
            }
        })
    }


    override fun onStart() {
        super.onStart()
        viewModel.startFetchData()
    }

    /**
     * 定义本页面的列表适配器
     */
    @SuppressLint("ParcelCreator")
    class XListAdapter(mContext: Context, mBeans: MutableList<UserRelation>) : BaseListAdapter<UserRelation, XListAdapter.XHolder>(mContext, mBeans) {

        protected override fun bindHolder(holder: XHolder, data: UserRelation?, position: Int) {
            if (data != null) {
                //显示头像
                ImageUtils.loadAvatarInto(mContext, data.friendAvatar, holder.binding.avatar)
                //显示名称(备注)
                if (!TextUtils.isEmpty(data.remark)) {
                    holder.binding.name.text = data.remark
                } else {
                    holder.binding.name.text = data.friendNickname
                }
                //设置点击事件
                if (mOnItemClickListener != null) {
                    holder.binding.item.setOnClickListener { view: View? -> mOnItemClickListener!!.onItemClick(data, view, position) }
                }
            }
        }
        

        class XHolder(itemView: FragmentContactListItemBinding) : BaseViewHolder<FragmentContactListItemBinding>(itemView)

        override fun getViewBinding(parent: ViewGroup, viewType: Int): ViewBinding {
            return FragmentContactListItemBinding.inflate(mInflater,parent,false)
        }

        override fun createViewHolder(viewBinding: ViewBinding, viewType: Int): XHolder {
            return XHolder(viewBinding as FragmentContactListItemBinding)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): ContactListFragment {
            return ContactListFragment()
        }
    }

    override fun initViewBinding(): FragmentContactListBinding {
        return FragmentContactListBinding.inflate(layoutInflater)
    }
}
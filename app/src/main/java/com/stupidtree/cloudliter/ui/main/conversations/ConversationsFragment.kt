package com.stupidtree.cloudliter.ui.main.conversations

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
import com.stupidtree.cloudliter.data.model.Conversation
import com.stupidtree.cloudliter.databinding.FragmentConversationsBinding
import com.stupidtree.cloudliter.databinding.FragmentConversationsListItemBinding
import com.stupidtree.cloudliter.service.socket.SocketIOClientService.Companion.RECEIVE_RELATION_EVENT
import com.stupidtree.cloudliter.utils.ActivityUtils
import com.stupidtree.cloudliter.utils.ImageUtils
import com.stupidtree.cloudliter.utils.TextUtils
import com.stupidtree.component.data.DataState
import com.stupidtree.style.base.BaseFragmentWithReceiver
import com.stupidtree.style.base.BaseListAdapter
import com.stupidtree.style.base.BaseViewHolder
import java.util.*

/**
 * “消息”页面
 */
@SuppressLint("NonConstantResourceId")
class ConversationsFragment : BaseFragmentWithReceiver<ConversationsViewModel, FragmentConversationsBinding>() {

    /**
     * 广播区
     */
    override var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "android.net.conn.CONNECTIVITY_CHANGE") {
                Log.e("网络状态改变", intent.toString())
            }
            viewModel.startRefresh()
        }
    }

    /**
     * 适配器区
     */
    private var listAdapter: CAdapter? = null
    override fun getViewModelClass(): Class<ConversationsViewModel> {
        return ConversationsViewModel::class.java
    }

    override fun getIntentFilter(): IntentFilter {
        val iF = IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        iF.addAction(RECEIVE_RELATION_EVENT)
        return iF
    }

    override fun initViews(view: View) {
        listAdapter = context?.let { CAdapter(it, LinkedList()) }
        binding?.list?.adapter = listAdapter
        binding?.list?.layoutManager = LinearLayoutManager(context)
        listAdapter!!.setOnItemClickListener(object : BaseListAdapter.OnItemClickListener<Conversation> {
            override fun onItemClick(data: Conversation, card: View?, position: Int) {
                ActivityUtils.startChatActivity(requireContext(), data.id)
            }
        })

        //设置下拉刷新
        binding?.refresh?.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent)
        binding?.refresh?.setOnRefreshListener { viewModel.startRefresh() }


        // searchBar.setOnClickListener(view1 -> ActivityUtils.startSearchActivity(requireActivity()));
        viewModel.listData.observe(this, { listDataState ->
            binding?.refresh?.isRefreshing = false
            if (listDataState.data != null && listDataState.data!!.isNotEmpty()) {
                val listD = listDataState.data!!
                listD.sortWith { conversation: Conversation, t1: Conversation ->
                    t1.updatedAt!!.compareTo(conversation.updatedAt)
                }
                listAdapter!!.notifyItemChangedSmooth(listD)
                if (listD.isNotEmpty()) {
                    binding?.list?.visibility = View.VISIBLE
                    binding?.placeHolder?.visibility = View.GONE
                } else {
                    binding?.list?.visibility = View.GONE
                    binding?.placeHolder?.visibility = View.VISIBLE
                    binding?.placeHolderText?.setText(R.string.no_conversation)
                }
            }
            when {
                listDataState.state === DataState.STATE.NOT_LOGGED_IN -> {
                    binding?.placeHolder?.visibility = View.VISIBLE
                    binding?.list?.visibility = View.GONE
                    binding?.placeHolderText?.setText(R.string.click_to_log_in)
                    binding?.placeHolder?.setOnClickListener {
                        ActivityUtils.startLoginActivity(requireContext())
                    }
                    if (!listDataState.fromCache) {
                        binding?.connectionFailedBar?.visibility = View.GONE
                    }
                }
                !listDataState.fromCache && listDataState.stateRetried === DataState.STATE.FETCH_FAILED -> {
                    binding?.list?.visibility = View.VISIBLE
                    binding?.placeHolder?.visibility = View.GONE
                    if (!listDataState.fromCache) {
                        binding?.connectionFailedBar?.visibility = View.VISIBLE
                    }
                }
                !listDataState.fromCache && listDataState.stateRetried === DataState.STATE.SUCCESS -> {
                    binding?.placeHolder?.visibility = View.GONE
                    binding?.connectionFailedBar?.visibility = View.GONE
                    if (listDataState.data != null && listDataState.data!!.isEmpty()) {
                        binding?.list?.visibility = View.GONE
                        binding?.placeHolder?.visibility = View.VISIBLE
                        binding?.placeHolderText?.setText(R.string.no_conversation)
                    }
                }
                listDataState.state !== DataState.STATE.SUCCESS -> {
                    binding?.placeHolder?.visibility = View.VISIBLE
                    binding?.list?.visibility = View.GONE
                    if (!listDataState.fromCache) {
                        binding?.connectionFailedBar?.visibility = View.GONE
                    }
                    binding?.placeHolderText?.setText(R.string.fetch_failed)
                }
            }
        })
        viewModel.unreadMessageState.observe(this) {
            //refreshLayout.setRefreshing(true);
            viewModel.startRefresh()
        }
    }


    override fun onResume() {
        super.onResume()
        viewModel.bindService(activity)
        viewModel.startRefresh()
        viewModel.callOnline(requireContext())
    }


    override fun onDetach() {
        super.onDetach()
        viewModel.unbindService(activity)
    }

    inner class CAdapter(mContext: Context, mBeans: MutableList<Conversation>) : BaseListAdapter<Conversation, CAdapter.CHolder>(mContext, mBeans) {
        /**
         * 缓存每个对话的未读状态
         */
        var unreadMap: HashMap<String, Int> = HashMap()


        override fun bindHolder(holder: CHolder, data: Conversation?, position: Int) {
            if (data != null) {
                ImageUtils.loadAvatarInto(mContext, data.avatar, holder.binding.avatar)
                holder.binding.lastMessage.text = data.lastMessage
                holder.binding.name.text = data.name
                val unread = viewModel.getUnreadNumber(data)
                unreadMap[data.id] = unread
                if (unread > 0) {
                    holder.binding.unread.visibility = View.VISIBLE
                    holder.binding.unread.text = unread.toString()
                } else {
                    holder.binding.unread.visibility = View.INVISIBLE
                }
                holder.binding.updatedAt.text = TextUtils.getConversationTimeText(mContext, data.updatedAt)
                if (mOnItemClickListener != null) {
                    holder.binding.item.setOnClickListener { view: View? -> mOnItemClickListener!!.onItemClick(data, view, position) }
                }
            }
        }

        override fun notifyItemChangedSmooth(list: List<Conversation>) {
            val newL = list.toMutableList()
            newL.sortWith { o1, o2 ->
                o2?.updatedAt?.compareTo(o1?.updatedAt) ?: 0
            }
            super.notifyItemChangedSmooth(newL, object : RefreshJudge<Conversation> {
                override fun judge(oldData: Conversation, newData: Conversation): Boolean {
                    val newUnread = viewModel.getUnreadNumber(newData)
                    val lastUnread = unreadMap[oldData.id]
                    return newData != oldData || lastUnread != newUnread
                }
            }, Comparator { conversation: Conversation, t1: Conversation ->
                if (t1.id == conversation.id)
                    return@Comparator 0
                else return@Comparator conversation.updatedAt!!.compareTo(t1.updatedAt)
            })
        }

        inner class CHolder(itemView: FragmentConversationsListItemBinding) : BaseViewHolder<FragmentConversationsListItemBinding>(itemView)

        override fun getViewBinding(parent: ViewGroup, viewType: Int): ViewBinding {
            return FragmentConversationsListItemBinding.inflate(layoutInflater, parent, false)
        }

        override fun createViewHolder(viewBinding: ViewBinding, viewType: Int): CHolder {
            return CHolder(viewBinding as FragmentConversationsListItemBinding)
        }

    }

    companion object {
        @JvmStatic
        fun newInstance(): ConversationsFragment {
            return ConversationsFragment()
        }
    }

    override fun initViewBinding(): FragmentConversationsBinding {
        return FragmentConversationsBinding.inflate(layoutInflater)
    }
}
package com.stupidtree.cloudliter.ui.main.conversations

import android.annotation.SuppressLint
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
import com.stupidtree.cloudliter.data.model.Conversation
import com.stupidtree.cloudliter.ui.base.BaseFragment
import com.stupidtree.cloudliter.ui.base.BaseListAdapter
import com.stupidtree.cloudliter.ui.base.BaseViewHolder
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.widgets.EmoticonsTextView
import com.stupidtree.cloudliter.utils.ActivityUtils
import com.stupidtree.cloudliter.utils.ImageUtils
import com.stupidtree.cloudliter.utils.TextUtils
import java.util.*

/**
 * “消息”页面
 */
@SuppressLint("NonConstantResourceId")
class ConversationsFragment : BaseFragment<ConversationsViewModel>() {
    /**
     * View绑定区
     */
    @JvmField
    @BindView(R.id.list)
    var list: RecyclerView? = null

    @JvmField
    @BindView(R.id.place_holder)
    var placeHolder //列表无内容时显示的布局
            : ViewGroup? = null

    @JvmField
    @BindView(R.id.place_holder_text)
    var placeHolderText //不显示列表时显示文字
            : TextView? = null

    @JvmField
    @BindView(R.id.refresh)
    var refreshLayout: SwipeRefreshLayout? = null

    @JvmField
    @BindView(R.id.connection_failed_bar)
    var connectionFailedBar: ViewGroup? = null

    /**
     * 适配器区
     */
    private var listAdapter: CAdapter? = null
    override fun getViewModelClass(): Class<ConversationsViewModel> {
        return ConversationsViewModel::class.java
    }

    override fun initViews(view: View) {
        listAdapter = context?.let { CAdapter(it, LinkedList()) }
        list!!.adapter = listAdapter
        list!!.layoutManager = LinearLayoutManager(context)
        listAdapter!!.setOnItemClickListener(object : BaseListAdapter.OnItemClickListener<Conversation> {
            override fun onItemClick(data: Conversation, card: View?, position: Int) {
                ActivityUtils.startChatActivity(requireContext(), data!!)
            }
        })

        //设置下拉刷新
        //设置下拉刷新
        refreshLayout!!.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent)
        refreshLayout!!.setOnRefreshListener { viewModel!!.startRefresh() }


        // searchBar.setOnClickListener(view1 -> ActivityUtils.startSearchActivity(requireActivity()));
        viewModel!!.listData?.observe(this, Observer { listDataState ->
            refreshLayout!!.isRefreshing = false
            if (listDataState.data != null && listDataState.data!!.isNotEmpty()) {
                val listD = listDataState.data!!
                Collections.sort(listD) { conversation: Conversation, t1: Conversation ->
                    t1.updatedAt!!.compareTo(conversation.updatedAt)
                }
                listAdapter!!.notifyItemChangedSmooth(listD)
                if (listD.size > 0) {
                    list!!.visibility = View.VISIBLE
                    placeHolder!!.visibility = View.GONE
                } else {
                    list!!.visibility = View.GONE
                    placeHolder!!.visibility = View.VISIBLE
                    placeHolderText!!.setText(R.string.no_conversation)
                }
            }
            // Log.e("state", String.valueOf(listDataState));
            when {
                listDataState.state === DataState.STATE.NOT_LOGGED_IN -> {
                    placeHolder!!.visibility = View.VISIBLE
                    list!!.visibility = View.GONE
                    placeHolderText!!.setText(R.string.not_logged_in)
                    if (listDataState.isRetry) {
                        connectionFailedBar!!.visibility = View.GONE
                    }
                }
                listDataState.state === DataState.STATE.FETCH_FAILED -> {
                    list!!.visibility = View.VISIBLE
                    placeHolder!!.visibility = View.GONE
                    if (listDataState.isRetry) {
                        connectionFailedBar!!.visibility = View.VISIBLE
                    }
                }
                listDataState.state === DataState.STATE.SUCCESS -> {
                    placeHolder!!.visibility = View.GONE
                    if (listDataState.isRetry) {
                        connectionFailedBar!!.visibility = View.GONE
                    }
                }
                listDataState.state !== DataState.STATE.SUCCESS -> {
                    placeHolder!!.visibility = View.VISIBLE
                    list!!.visibility = View.GONE
                    if (listDataState.isRetry) {
                        connectionFailedBar!!.visibility = View.GONE
                    }
                    placeHolderText!!.setText(R.string.fetch_failed)
                }
            }
        })
        viewModel!!.unreadMessageState?.observe(this, Observer { listDataState ->
            //refreshLayout.setRefreshing(true);
            viewModel!!.startRefresh()
        })
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_conversations
    }

    override fun onResume() {
        super.onResume()
        viewModel!!.startRefresh()
        viewModel!!.callOnline(requireContext())
    }

    override fun onStart() {
        super.onStart()
        viewModel!!.bindService(activity)
    }

    override fun onDetach() {
        super.onDetach()
        viewModel!!.unbindService(activity)
    }

    inner class CAdapter(mContext: Context, mBeans: MutableList<Conversation>) : BaseListAdapter<Conversation, CAdapter.CHolder>(mContext, mBeans) {
        /**
         * 缓存每个对话的未读状态
         */
        var unreadMap: HashMap<String, Int> = HashMap()
        override fun getLayoutId(viewType: Int): Int {
            return R.layout.fragment_conversations_list_item
        }

        override fun createViewHolder(v: View, viewType: Int): CHolder {
            return CHolder(v)
        }

        override fun bindHolder(holder: CHolder, data: Conversation?, position: Int) {
            if (data != null) {
                ImageUtils.loadAvatarInto(mContext, data.friendAvatar, holder.avatar!!)
                holder.lastMessage!!.text = data.lastMessage
                if (TextUtils.isEmpty(data.friendRemark)) {
                    holder.name!!.text = data.friendNickname
                } else {
                    holder.name!!.text = data.friendRemark
                }
                val unread = viewModel!!.getUnreadNumber(data)
                unreadMap[data.id] = unread
                if (unread > 0) {
                    holder.unread!!.visibility = View.VISIBLE
                    holder.unread!!.text = unread.toString()
                } else {
                    holder.unread!!.visibility = View.INVISIBLE
                }
                holder.updatedAt!!.text = TextUtils.getConversationTimeText(mContext, data.updatedAt)
                if (mOnItemClickListener != null) {
                    holder.item!!.setOnClickListener { view: View? -> mOnItemClickListener!!.onItemClick(data, view, position) }
                }
            }
        }

        override fun notifyItemChangedSmooth(newL: List<Conversation>) {
            super.notifyItemChangedSmooth(newL, object : RefreshJudge<Conversation> {
                override fun judge(oldData: Conversation, newData: Conversation): Boolean {
                    val newUnread = viewModel!!.getUnreadNumber(newData)
                    val lastUnread = unreadMap[oldData.id]
                    return newData != oldData || lastUnread != newUnread
                }
            }, Comparator { conversation: Conversation, t1: Conversation ->
                if (t1.id == conversation.id)
                    return@Comparator 0
                else return@Comparator conversation.updatedAt!!.compareTo(t1.updatedAt)
            })
        }

        inner class CHolder(itemView: View) : BaseViewHolder(itemView) {
            @JvmField
            @BindView(R.id.name)
            var name: TextView? = null

            @JvmField
            @BindView(R.id.last_message)
            var lastMessage: EmoticonsTextView? = null

            @JvmField
            @BindView(R.id.avatar)
            var avatar: ImageView? = null

            @JvmField
            @BindView(R.id.updated_at)
            var updatedAt: TextView? = null

            @JvmField
            @BindView(R.id.item)
            var item: ViewGroup? = null

            @JvmField
            @BindView(R.id.unread)
            var unread: TextView? = null
        }

    }

    companion object {
        @JvmStatic
        fun newInstance(): ConversationsFragment {
            return ConversationsFragment()
        }
    }
}
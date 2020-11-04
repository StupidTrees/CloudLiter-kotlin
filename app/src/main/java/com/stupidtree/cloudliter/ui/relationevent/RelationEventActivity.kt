package com.stupidtree.cloudliter.ui.relationevent

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.RelationEvent
import com.stupidtree.cloudliter.data.model.RelationEvent.ACTION
import com.stupidtree.cloudliter.ui.base.BaseActivity
import com.stupidtree.cloudliter.ui.base.BaseListAdapter
import com.stupidtree.cloudliter.ui.base.BaseListAdapter.RefreshJudge
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.relationevent.RelationEventActivity.RListAdapter.RHolder
import com.stupidtree.cloudliter.ui.widgets.PopUpCheckableList
import com.stupidtree.cloudliter.ui.widgets.PopUpText
import com.stupidtree.cloudliter.utils.ActivityUtils
import com.stupidtree.cloudliter.utils.ImageUtils
import com.stupidtree.cloudliter.utils.TextUtils
import java.util.*

/**
 * 好友关系页面
 */
@SuppressLint("NonConstantResourceId")
class RelationEventActivity : BaseActivity<RelationEventViewModel>() {
    /**
     * View绑定区
     */
    @JvmField
    @BindView(R.id.toolbar)
    var toolbar: Toolbar? = null

    @JvmField
    @BindView(R.id.list)
    var list: RecyclerView? = null

    /**
     * 适配器
     */
    private lateinit var listAdapter: RListAdapter
    override fun getLayoutId(): Int {
        return R.layout.activity_relation_event
    }

    override fun getViewModelClass(): Class<RelationEventViewModel> {
        return RelationEventViewModel::class.java
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setWindowParams(true, true, false)
        setToolbarActionBack(toolbar!!)
    }

    override fun initViews() {
        listAdapter = RListAdapter(this, LinkedList())
        list!!.adapter = listAdapter
        list!!.layoutManager = LinearLayoutManager(applicationContext)
        listAdapter.setOnButtonClickListener(
                object : BaseListAdapter.OnItemClickListener<RelationEvent> {
                    override fun onItemClick(data: RelationEvent, card: View?, position: Int) {
                        PopUpCheckableList<ACTION>().setTitle(R.string.select_action)
                                .setListData(
                                        listOf(getString(R.string.prompt_accept), getString(R.string.prompt_reject)),
                                        listOf(ACTION.ACCEPT, ACTION.REJECT))
                                .setOnConfirmListener (object:PopUpCheckableList.OnConfirmListener<ACTION> {
                                    override fun OnConfirm(title: String?, key: ACTION) {
                                        key.let { data.id?.let { it1 -> viewModel!!.responseFriendRequest(it1, it) } }
                                    }
                                })
                                .show(supportFragmentManager, "select")
                    }
                })
        listAdapter.setOnItemClickListener(object : BaseListAdapter.OnItemClickListener<RelationEvent> {
            override fun onItemClick(data: RelationEvent, card: View?, position: Int) {
                data.otherId?.let { ActivityUtils.startProfileActivity(getThis(), it) }
            }
        })
        viewModel!!.listData?.observe(this, Observer { listDataState: DataState<List<RelationEvent>?> ->
            if (listDataState.state === DataState.STATE.SUCCESS) {
                listDataState.data?.let {
                    listAdapter.notifyItemChangedSmooth(it, object : RefreshJudge<RelationEvent> {
                        override fun judge(oldData: RelationEvent, newData: RelationEvent): Boolean {
                            return oldData != newData
                        }
                    })
                }
            }
        })
        viewModel!!.responseResult?.observe(this, Observer { dataState: DataState<*> ->
            if (dataState.state === DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(), R.string.make_friends_success, Toast.LENGTH_SHORT).show()
            }
            viewModel!!.startRefresh()
        })
        viewModel!!.markReadResult?.observe(this, Observer { dataState: DataState<Any?>? -> })
    }

    override fun onResume() {
        super.onResume()
        viewModel!!.startRefresh()
        viewModel!!.startMarkRead()
    }

    private inner class RListAdapter(mContext: Context, mBeans: MutableList<RelationEvent>) : BaseListAdapter<RelationEvent, RHolder>(mContext, mBeans) {
        var mOnButtonClickListener: OnItemClickListener<RelationEvent>? = null
        fun setOnButtonClickListener(mOnButtonClickListener: OnItemClickListener<RelationEvent>?) {
            this.mOnButtonClickListener = mOnButtonClickListener
        }

        override fun getLayoutId(viewType: Int): Int {
            return R.layout.activity_relation_event_list_item
        }

        override fun createViewHolder(v: View, viewType: Int): RHolder {
            return RHolder(v)
        }

        override fun bindHolder(holder: RHolder, data: RelationEvent?, position: Int) {
            if (data != null) {
                holder.nickname.text = data.otherNickname
                holder.time.text = TextUtils.getChatTimeText(mContext, data.createdAt)
                ImageUtils.loadAvatarNoCacheInto(mContext, data.otherAvatar, holder.avatar)
                if (mOnItemClickListener != null) {
                    holder.item.setOnClickListener { view: View? -> mOnItemClickListener!!.onItemClick(data, view, position) }
                }
                if (data.isUnread) {
                    holder.unread.visibility = View.VISIBLE
                } else {
                    holder.unread.visibility = View.GONE
                }
                if (data.userId == viewModel!!.localUserId) { //我发出的
                    holder.accept.visibility = View.GONE
                    holder.message.visibility = View.VISIBLE
                    holder.icon.setImageResource(R.drawable.ic_sent)
                    if (data.state == RelationEvent.STATE.REQUESTING) {
                        holder.message.setText(R.string.relation_requesting)
                    } else if (data.state == RelationEvent.STATE.REJECTED) {
                        holder.message.setText(R.string.relation_rejected)
                    } else if (data.state == RelationEvent.STATE.ACCEPTED) {
                        holder.message.setText(R.string.relation_accepted)
                    } else if (data.state == RelationEvent.STATE.DELETE) {
                        holder.message.setText(R.string.relation_you_deleted)
                    }
                } else { //我收到的
                    holder.icon.setImageResource(R.drawable.ic_received)
                    holder.accept.visibility = View.GONE
                    holder.message.visibility = View.VISIBLE
                    if (data.state == RelationEvent.STATE.DELETE) {
                        holder.message.setText(R.string.relation_delete)
                    } else if (data.state == RelationEvent.STATE.ACCEPTED) {
                        holder.message.setText(R.string.relation_accepted)
                    } else if (data.state == RelationEvent.STATE.REJECTED) {
                        holder.message.setText(R.string.relation_rejected)
                    } else if (data.state == RelationEvent.STATE.REQUESTING) {
                        holder.accept.visibility = View.VISIBLE
                        holder.message.visibility = View.GONE
                        if (mOnButtonClickListener != null) {
                            holder.accept.setOnClickListener { view: View? -> mOnButtonClickListener!!.onItemClick(data, view, position) }
                        }
                    }
                }
                if (data.state == RelationEvent.STATE.REQUESTING) {
                    holder.icon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(getThis(), R.color.colorPrimary))
                    holder.icon.setBackgroundResource(R.drawable.element_round_primary)
                } else {
                    holder.icon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(getThis(), android.R.color.darker_gray))
                    holder.icon.setBackgroundResource(R.drawable.element_round_grey)
                }
            }
        }

        inner class RHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var item: View
            var avatar: ImageView
            var nickname: TextView
            var icon: ImageView
            var message: TextView
            var time: TextView
            var accept: View
            var unread: ImageView

            init {
                item = itemView.findViewById(R.id.item)
                accept = itemView.findViewById(R.id.accept)
                avatar = itemView.findViewById(R.id.avatar)
                nickname = itemView.findViewById(R.id.nickname)
                icon = itemView.findViewById(R.id.icon)
                message = itemView.findViewById(R.id.message)
                time = itemView.findViewById(R.id.time)
                unread = itemView.findViewById(R.id.unread)
            }
        }
    }
}
package com.stupidtree.cloudliter.ui.relationevent

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.RelationEvent
import com.stupidtree.cloudliter.data.model.RelationEvent.ACTION
import com.stupidtree.cloudliter.databinding.ActivityRelationEventBinding
import com.stupidtree.cloudliter.databinding.ActivityRelationEventListItemBinding
import com.stupidtree.cloudliter.ui.base.BaseActivity
import com.stupidtree.cloudliter.ui.base.BaseListAdapter
import com.stupidtree.cloudliter.ui.base.BaseListAdapter.RefreshJudge
import com.stupidtree.cloudliter.ui.base.BaseViewHolder
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.relationevent.RelationEventActivity.RListAdapter.RHolder
import com.stupidtree.cloudliter.ui.widgets.PopUpCheckableList
import com.stupidtree.cloudliter.utils.ActivityUtils
import com.stupidtree.cloudliter.utils.ImageUtils
import com.stupidtree.cloudliter.utils.TextUtils
import java.util.*

/**
 * 好友关系页面
 */
@SuppressLint("NonConstantResourceId")
class RelationEventActivity : BaseActivity<RelationEventViewModel,ActivityRelationEventBinding>() {
    /**
     * 适配器
     */
    private lateinit var listAdapter: RListAdapter

    override fun getViewModelClass(): Class<RelationEventViewModel> {
        return RelationEventViewModel::class.java
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setWindowParams(true, darkColor = true, navi = false)
        setToolbarActionBack(binding.toolbar)
    }

    override fun initViews() {
        listAdapter = RListAdapter(this, LinkedList())
        binding.list.adapter = listAdapter
        binding.list.layoutManager = LinearLayoutManager(applicationContext)
        listAdapter.setOnButtonClickListener(
                object : BaseListAdapter.OnItemClickListener<RelationEvent> {
                    override fun onItemClick(data: RelationEvent, card: View?, position: Int) {
                        PopUpCheckableList<ACTION>().setTitle(getString(R.string.select_action))
                                .setListData(
                                        listOf(getString(R.string.prompt_accept), getString(R.string.prompt_reject)),
                                        listOf(ACTION.ACCEPT, ACTION.REJECT))
                                .setOnConfirmListener (object:PopUpCheckableList.OnConfirmListener<ACTION> {
                                    override fun OnConfirm(title: String?, key: ACTION) {
                                        key.let { data.id?.let { it1 -> viewModel.responseFriendRequest(it1, it) } }
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
        viewModel.listData?.observe(this, Observer { listDataState: DataState<List<RelationEvent>?> ->
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
        viewModel.responseResult?.observe(this, Observer { dataState: DataState<*> ->
            if (dataState.state === DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(), R.string.make_friends_success, Toast.LENGTH_SHORT).show()
            }
            viewModel.startRefresh()
        })
        viewModel.markReadResult?.observe(this, { })
    }

    override fun onResume() {
        super.onResume()
        viewModel.startRefresh()
        viewModel.startMarkRead()
    }

    @SuppressLint("ParcelCreator")
    private inner class RListAdapter(mContext: Context, mBeans: MutableList<RelationEvent>) : BaseListAdapter<RelationEvent, RHolder>(mContext, mBeans) {
        var mOnButtonClickListener: OnItemClickListener<RelationEvent>? = null
        fun setOnButtonClickListener(mOnButtonClickListener: OnItemClickListener<RelationEvent>?) {
            this.mOnButtonClickListener = mOnButtonClickListener
        }



        override fun bindHolder(holder: RHolder, data: RelationEvent?, position: Int) {
            if (data != null) {
                holder.binding.nickname.text = data.otherNickname
                holder.binding.time.text = TextUtils.getChatTimeText(mContext, data.createdAt)
                ImageUtils.loadAvatarNoCacheInto(mContext, data.otherAvatar, holder.binding.avatar)
                if (mOnItemClickListener != null) {
                    holder.binding.item.setOnClickListener { view: View? -> mOnItemClickListener!!.onItemClick(data, view, position) }
                }
                if (data.isUnread) {
                    holder.binding.unread.visibility = View.VISIBLE
                } else {
                    holder.binding.unread.visibility = View.GONE
                }
                if (data.userId == viewModel.localUserId) { //我发出的
                    holder.binding.accept.visibility = View.GONE
                    holder.binding.message.visibility = View.VISIBLE
                    holder.binding.icon.setImageResource(R.drawable.ic_sent)
                    if (data.state == RelationEvent.STATE.REQUESTING) {
                        holder.binding.message.setText(R.string.relation_requesting)
                    } else if (data.state == RelationEvent.STATE.REJECTED) {
                        holder.binding.message.setText(R.string.relation_rejected)
                    } else if (data.state == RelationEvent.STATE.ACCEPTED) {
                        holder.binding.message.setText(R.string.relation_accepted)
                    } else if (data.state == RelationEvent.STATE.DELETE) {
                        holder.binding.message.setText(R.string.relation_you_deleted)
                    }
                } else { //我收到的
                    holder.binding.icon.setImageResource(R.drawable.ic_received)
                    holder.binding.accept.visibility = View.GONE
                    holder.binding.message.visibility = View.VISIBLE
                    if (data.state == RelationEvent.STATE.DELETE) {
                        holder.binding.message.setText(R.string.relation_delete)
                    } else if (data.state == RelationEvent.STATE.ACCEPTED) {
                        holder.binding.message.setText(R.string.relation_accepted)
                    } else if (data.state == RelationEvent.STATE.REJECTED) {
                        holder.binding.message.setText(R.string.relation_rejected)
                    } else if (data.state == RelationEvent.STATE.REQUESTING) {
                        holder.binding.accept.visibility = View.VISIBLE
                        holder.binding.message.visibility = View.GONE
                        if (mOnButtonClickListener != null) {
                            holder.binding.accept.setOnClickListener { view: View? -> mOnButtonClickListener!!.onItemClick(data, view, position) }
                        }
                    }
                }
                if (data.state == RelationEvent.STATE.REQUESTING) {
                    holder.binding.icon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(getThis(), R.color.colorPrimary))
                    holder.binding.icon.setBackgroundResource(R.drawable.element_round_primary)
                } else {
                    holder.binding.icon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(getThis(), android.R.color.darker_gray))
                    holder.binding.icon.setBackgroundResource(R.drawable.element_round_grey)
                }
            }
        }

        inner class RHolder(itemView: ActivityRelationEventListItemBinding) : BaseViewHolder<ActivityRelationEventListItemBinding>(itemView)

        override fun getViewBinding(parent: ViewGroup, viewType: Int): ViewBinding {
            return ActivityRelationEventListItemBinding.inflate(mInflater,parent,false)
        }

        override fun createViewHolder(viewBinding: ViewBinding, viewType: Int): RHolder {
            return RHolder(viewBinding as ActivityRelationEventListItemBinding)
        }
    }

    override fun initViewBinding(): ActivityRelationEventBinding {
        return ActivityRelationEventBinding.inflate(layoutInflater)
    }
}
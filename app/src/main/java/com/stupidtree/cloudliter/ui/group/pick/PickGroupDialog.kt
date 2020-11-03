package com.stupidtree.cloudliter.ui.group.pick

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.RelationGroup
import com.stupidtree.cloudliter.ui.base.BaseViewHolder
import com.stupidtree.cloudliter.ui.base.BasicSelectableListAdapter
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.widgets.TransparentBottomSheetDialog
import java.util.*

/**
 * 选择好友分组的底部弹窗
 */
class PickGroupDialog : TransparentBottomSheetDialog() {
    @JvmField
    @BindView(R.id.list)
    var list: RecyclerView? = null

    @JvmField
    @BindView(R.id.confirm)
    var confirm: View? = null

    @JvmField
    @BindView(R.id.cancel)
    var cancel: View? = null

    @JvmField
    @BindView(R.id.loading)
    var loading: ProgressBar? = null
    var viewModel: PickGroupViewModel? = null
    var listAdapter: LAdapter? = null
    var onConfirmListener: OnConfirmListener? = null
    var initGroupId: String? = null

    interface OnConfirmListener {
        fun OnConfirmed(group: RelationGroup?)
    }

    fun setOnConfirmListener(onConfirmListener: OnConfirmListener?): PickGroupDialog {
        this.onConfirmListener = onConfirmListener
        return this
    }

    fun setInitGroupId(initGroupId: String?): PickGroupDialog {
        this.initGroupId = initGroupId
        return this
    }

    override fun getLayoutId(): Int {
        return R.layout.dialog_bottom_group_list
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)).get(PickGroupViewModel::class.java)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun initViews(v: View) {
        listAdapter = LAdapter(requireContext(), LinkedList())
        list!!.adapter = listAdapter
        list!!.layoutManager = LinearLayoutManager(requireContext())
        viewModel!!.listData?.observe(this, Observer { listDataState: DataState<List<RelationGroup>?> ->
            loading!!.visibility = View.INVISIBLE
            if (listDataState.state === DataState.STATE.SUCCESS) {
                listAdapter!!.setSelected(listDataState.data!!, initGroupId)
                listAdapter!!.notifyItemChangedSmooth(listDataState.data!!)
            }
        })
        confirm!!.setOnClickListener { view: View? ->
            if (onConfirmListener != null) {
                onConfirmListener!!.OnConfirmed(listAdapter!!.selectedData)
                dismiss()
            }
        }
        cancel!!.setOnClickListener { view: View? -> dismiss() }
    }

    override fun onResume() {
        super.onResume()
        loading!!.visibility = View.VISIBLE
        viewModel!!.startRefresh()
    }

    class LAdapter(mContext: Context, mBeans: MutableList<RelationGroup>) : BasicSelectableListAdapter<RelationGroup, LAdapter.LHolder>(mContext, mBeans) {
        override fun getLayoutId(viewType: Int): Int {
            return R.layout.dialog_bottom_selectable_list_item
        }

        override fun createViewHolder(v: View, viewType: Int): LHolder {
            return LHolder(v)
        }

        override fun bindHolder(holder: LHolder, data: RelationGroup?, position: Int) {
            if (data != null) {
                holder.text!!.text = data.groupName
            }
            if (position == selectedIndex) { //若被选中
                holder.selected!!.visibility = View.VISIBLE
            } else {
                holder.selected!!.visibility = View.GONE
            }
            holder.item!!.setOnClickListener { data?.let { selectItem(position, it) } }
        }

        fun setSelected(data: List<RelationGroup>, groupId: String?) {
            mBeans.clear()
            mBeans.addAll(data)
            if (groupId == null && data.size > 0) {
                selectedIndex = 0
                selectedData = data[0]
            } else {
                for (relationGroup in mBeans) {
                    if (relationGroup.id == groupId) {
                        selectedIndex = mBeans.indexOf(relationGroup)
                        selectedData = relationGroup
                    }
                }
            }
        }

        class LHolder(itemView: View) : BaseViewHolder(itemView) {
            @JvmField
            @BindView(R.id.text)
            var text: TextView? = null

            @JvmField
            @BindView(R.id.item)
            var item: ViewGroup? = null

            @JvmField
            @BindView(R.id.selected)
            var selected: ImageView? = null
        }
    }
}
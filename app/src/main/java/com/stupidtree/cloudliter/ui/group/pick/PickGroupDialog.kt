package com.stupidtree.cloudliter.ui.group.pick

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.RelationGroup
import com.stupidtree.cloudliter.databinding.DialogBottomGroupListBinding
import com.stupidtree.style.base.BaseViewHolder
import com.stupidtree.component.data.DataState
import com.stupidtree.style.base.BasicSelectableListAdapter
import com.stupidtree.style.databinding.DialogBottomSelectableListItemBinding
import com.stupidtree.style.widgets.TransparentBottomSheetDialog
import java.util.*

/**
 * 选择好友分组的底部弹窗
 */
class PickGroupDialog : TransparentBottomSheetDialog<DialogBottomGroupListBinding>() {
    var viewModel: PickGroupViewModel? = null
    var listAdapter: LAdapter? = null
    private var onConfirmListener: OnConfirmListener? = null
    private var initGroupId: String? = null

    interface OnConfirmListener {
        fun onConfirmed(group: RelationGroup?)
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
        binding.list.adapter = listAdapter
        binding.list.layoutManager = LinearLayoutManager(requireContext())
        viewModel!!.listData?.observe(this, { listDataState: DataState<List<RelationGroup>?> ->
            binding.loading.visibility = View.INVISIBLE
            if (listDataState.state === DataState.STATE.SUCCESS) {
                listAdapter!!.setSelected(listDataState.data!!, initGroupId)
                listAdapter!!.notifyItemChangedSmooth(listDataState.data!!)
            }
        })
        binding.confirm.setOnClickListener {
            if (onConfirmListener != null) {
                onConfirmListener!!.onConfirmed(listAdapter!!.selectedData)
                dismiss()
            }
        }
        binding.cancel.setOnClickListener { dismiss() }
    }

    override fun onStart() {
        super.onStart()
        binding.loading.visibility = View.VISIBLE
        viewModel!!.startRefresh()
    }

    class LAdapter(mContext: Context, mBeans: MutableList<RelationGroup>) : BasicSelectableListAdapter<RelationGroup, LAdapter.LHolder>(mContext, mBeans) {

        override fun bindHolder(holder: LHolder, data: RelationGroup?, position: Int) {
            if (data != null) {
                holder.binding.text.text = data.groupName
            }
            if (position == selectedIndex) { //若被选中
                holder.binding.selected.visibility = View.VISIBLE
            } else {
                holder.binding.selected.visibility = View.GONE
            }
            holder.binding.item.setOnClickListener { data?.let { selectItem(position, it) } }
        }

        fun setSelected(data: List<RelationGroup>, groupId: String?) {
            mBeans.clear()
            mBeans.addAll(data)
            if (groupId == null && data.isNotEmpty()) {
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

        class LHolder(itemView: DialogBottomSelectableListItemBinding) : BaseViewHolder<DialogBottomSelectableListItemBinding>(itemView)

        override fun getViewBinding(parent: ViewGroup, viewType: Int): ViewBinding {
            return DialogBottomSelectableListItemBinding.inflate(mInflater, parent, false)
        }

        override fun createViewHolder(viewBinding: ViewBinding, viewType: Int): LHolder {
            return LHolder(viewBinding as DialogBottomSelectableListItemBinding)
        }
    }

    override fun initViewBinding(v: View): DialogBottomGroupListBinding {
        return DialogBottomGroupListBinding.bind(v)
    }
}
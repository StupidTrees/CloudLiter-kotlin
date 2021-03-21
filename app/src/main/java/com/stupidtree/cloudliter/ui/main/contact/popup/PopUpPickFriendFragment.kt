package com.stupidtree.cloudliter.ui.main.contact.popup

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.UserRelation
import com.stupidtree.cloudliter.databinding.DialogBottomPickFriendBinding
import com.stupidtree.component.data.DataState
import com.stupidtree.style.base.BaseListAdapter.RefreshJudge
import com.stupidtree.style.widgets.TransparentModeledBottomSheetDialog
import java.util.*

class PopUpPickFriendFragment(private val onConfirmListener: OnConfirmListener,private var excludeIds:List<String>) : TransparentModeledBottomSheetDialog<PickFriendViewModel, DialogBottomPickFriendBinding>() {

    lateinit var listAdapter: PickFriendListAdapter

    interface OnConfirmListener {
        fun onConfirm(userIds: List<String>)
    }

    override fun getViewModelClass(): Class<PickFriendViewModel> {
        return PickFriendViewModel::class.java
    }

    override fun initViews(view: View) {
        //初始化一下列表的view
        listAdapter = PickFriendListAdapter(requireContext(), mutableListOf(), 1)
        binding?.list?.adapter = listAdapter
        binding?.list?.layoutManager = LinearLayoutManager(context)
        binding?.confirm?.setOnClickListener {
            val ids = mutableListOf<String>()
            for (ur in listAdapter.getCheckedData()) {
                ids.add(ur.friendId)
            }
            if (ids.isNotEmpty()) {
                onConfirmListener.onConfirm(ids)
                dismiss()
            }
        }
        binding?.cancel?.setOnClickListener {
            dismiss()
        }


        //当列表数据变更时，将自动调用本匿名函数
        viewModel.listData.observe(this, { contactListState->
            binding?.loading?.visibility = View.GONE
            if (contactListState.state === DataState.STATE.SUCCESS) {
                val newList = mutableListOf<UserRelation>()
                for(u in contactListState.data?: listOf()){
                    if(!excludeIds.contains(u.friendId)){
                        newList.add(u)
                    }
                }
                val sortedList = newList.sortedWith(comparator = object : Comparator<UserRelation> {
                    override fun compare(o1: UserRelation?, o2: UserRelation?): Int {
                        if (o1 == null || o2 == null) return -1
                        val name1: String = if (o1.remark.isNullOrEmpty()) o1.friendNickname.toString() else o1.remark.toString()
                        val name2: String = if (o2.remark.isNullOrEmpty()) o2.friendNickname.toString() else o2.remark.toString()
                        return name1.compareTo(name2)
                    }
                })

                //状态为”成功“，那么列表设置为可见，并通知列表适配器丝滑地更新列表项
                listAdapter.notifyItemChangedSmooth(sortedList, object : RefreshJudge<UserRelation> {
                    override fun judge(oldData: UserRelation, newData: UserRelation): Boolean {
                        return oldData != newData || oldData.remark != newData.remark
                    }
                })
            }
        })
    }


    override fun onStart() {
        super.onStart()
        viewModel.startFetchData()
    }


    override fun getLayoutId(): Int {
        return R.layout.dialog_bottom_pick_friend
    }

    override fun initViewBinding(v: View): DialogBottomPickFriendBinding {
        return DialogBottomPickFriendBinding.bind(v)
    }
}
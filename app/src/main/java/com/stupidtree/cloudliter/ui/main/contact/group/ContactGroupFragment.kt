package com.stupidtree.cloudliter.ui.main.contact.group

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import com.donkingliang.groupedadapter.adapter.GroupedRecyclerViewAdapter
import com.donkingliang.groupedadapter.holder.BaseViewHolder
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.UserRelation
import com.stupidtree.cloudliter.ui.base.BaseFragment
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.utils.ActivityUtils

/**
 * 联系人页面的Fragment
 */
class ContactGroupFragment : BaseFragment<ContactGroupViewModel>() {
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
            : GroupedFriendsListAdapter? = null

    override fun getViewModelClass(): Class<ContactGroupViewModel> {
        return ContactGroupViewModel::class.java
    }

    override fun initViews(view: View) {
        //初始化一下列表的view
        listAdapter = GroupedFriendsListAdapter(context)
        list!!.adapter = listAdapter
        list!!.layoutManager = LinearLayoutManager(context)
        listAdapter!!.setOnHeaderClickListener { adapter: GroupedRecyclerViewAdapter?, holder: BaseViewHolder?, groupPosition: Int -> listAdapter!!.toggleGroup(list, groupPosition) }
        listAdapter!!.setOnChildClickListener { adapter: GroupedRecyclerViewAdapter?, holder: BaseViewHolder?, groupPosition: Int, childPosition: Int ->
            val ur = listAdapter!!.groupEntities[groupPosition].getChildAt(childPosition)
            ActivityUtils.startProfileActivity(requireActivity(), ur.friendId.toString())
        }
        //设置下拉刷新
        refreshLayout!!.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent)
        refreshLayout!!.setOnRefreshListener { viewModel!!.startFetchData() }
        viewModel!!.listData?.observe(this, Observer { contactListState: DataState<List<UserRelation>?> ->
            refreshLayout!!.isRefreshing = false
            if (contactListState.state === DataState.STATE.SUCCESS) {
                //状态为”成功“，那么列表设置为可见，并通知列表适配器丝滑地更新列表项
                listAdapter!!.notifyItemChangedSmooth(contactListState.data)
                //listAdapter.notifyItemChangedSmooth(contactListState.getData(), (oldData, newData) -> !Objects.equals(oldData, newData) || !Objects.equals(oldData.getRemark(), newData.getRemark()));
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
    } //

    //    static class XListAdapter extends BaseListAdapter<UserRelation, XListAdapter.XHolder> {
    //        private static final int TYPE_LABEL = 69;
    //        private static final int TYPE_ITEM = 678;
    //
    //        HashMap<String,Boolean> expanded = new HashMap<>();
    //        HashMap<String,String> id2Name = new HashMap<>();
    //
    //        RecyclerView recyclerView;
    //
    //        public XListAdapter(Context mContext, List<UserRelation> mBeans,RecyclerView recyclerView) {
    //            super(mContext, mBeans);
    //            this.recyclerView = recyclerView;
    //        }
    //
    //        @Override
    //        protected int getLayoutId(int viewType) {
    //            if (viewType == TYPE_ITEM) {
    //                return R.layout.fragment_contact_list_item;
    //            } else {
    //                return R.layout.fragment_contact_group_item_label;
    //            }
    //        }
    //
    //        @Override
    //        public void notifyItemChangedSmooth(List<UserRelation> newL, RefreshJudge<UserRelation> judge) {
    //            HashMap<String,List<UserRelation>> groupSorted = new HashMap<>();
    //            id2Name.clear();
    //            expanded.clear();
    //            for(UserRelation ur:newL){
    //                String tmpId = ur.getGroupId()==null?"null":ur.getGroupId();
    //                if(!groupSorted.containsKey(tmpId)){
    //                    groupSorted.put(tmpId,new LinkedList<>());
    //                    id2Name.put(tmpId,ur.getGroupName()==null?"未分组":ur.getGroupName());
    //                    expanded.put(tmpId,false);
    //                }
    //                List<UserRelation> list = groupSorted.get(tmpId);
    //                if(list!=null){
    //                    list.add(ur);
    //                }
    //            }
    //            List<UserRelation> result = new LinkedList<>();
    //            for(Map.Entry<String,List<UserRelation>> e:groupSorted.entrySet()){
    //                UserRelation group = UserRelation.getLabelInstance(e.getKey(),id2Name.get(e.getKey()));
    //                result.add(group);
    //                result.addAll(e.getValue());
    //            }
    //            super.notifyItemChangedSmooth(result, judge);
    //        }
    //
    //        @Override
    //        public int getItemViewType(int position) {
    //            if (mBeans.get(position).isLabel()) {
    //                return TYPE_LABEL;
    //            } else {
    //                return TYPE_ITEM;
    //            }
    //        }
    //
    //
    //        private void refreshExpanded(){
    ////            for(int i=0;i<getItemCount();i++){
    ////                if(i<mBeans.size()){
    ////                    UserRelation ur = mBeans.get(i);
    ////                    XHolder holder = (XHolder) recyclerView.findViewHolderForAdapterPosition(i);
    ////                    if(holder!=null){
    ////                        holder.bindVisible(expanded,ur);
    ////                    }
    ////                }
    ////            }
    //            notifyDataSetChanged();
    //        }
    //        @Override
    //        protected void bindHolder(@NonNull XHolder holder, @Nullable UserRelation data, int position) {
    //            if (data != null) {
    //                String groupId = data.getGroupId()==null?"null":data.getGroupId();
    //                Boolean visible = expanded.get(groupId);
    //                if (data.isLabel()) {
    //                    holder.itemView.setVisibility(View.VISIBLE);
    //                    holder.name.setText(data.getFriendNickname());
    //                    holder.item.setOnClickListener(view -> {
    //                        expanded.put(groupId,visible!=null&&!visible);
    //                        refreshExpanded();
    //                    });
    //                } else {
    //                    //显示头像
    //                    ImageUtils.loadAvatarInto(mContext, data.getFriendAvatar(), Objects.requireNonNull(holder.avatar));
    //                    //显示名称(备注)
    //                    if (!TextUtils.isEmpty(data.getRemark())) {
    //                        holder.name.setText(data.getRemark());
    //                    } else {
    //                        holder.name.setText(data.getFriendNickname());
    //                    }
    //                    //设置点击事件
    //                    if (mOnItemClickListener != null) {
    //                        holder.item.setOnClickListener(view -> mOnItemClickListener.onItemClick(data, view, position));
    //                    }
    //                    if(visible!=null&&visible){
    //                        holder.item.setVisibility(View.VISIBLE);
    //                    }else{
    //                        holder.item.setVisibility(View.GONE);
    //                    }
    //
    //                }
    //
    //            }
    //
    //        }
    //
    //        @Override
    //        public XHolder createViewHolder(View v, int viewType) {
    //            return new XHolder(v);
    //        }
    //
    //        static class XHolder extends BaseViewHolder {
    //            @BindView(R.id.name)
    //            TextView name;
    //            @BindView(R.id.item)
    //            ViewGroup item;
    //            @Nullable
    //            @BindView(R.id.avatar)
    //            ImageView avatar;
    //
    //            public void bindVisible(HashMap<String,Boolean> expanded,UserRelation data){
    //                if(data.isLabel()){
    //                    itemView.setVisibility(View.VISIBLE);
    //                    return;
    //                }
    //                String groupId = data.getGroupId()==null?"null":data.getGroupId();
    //                Boolean visible = expanded.get(groupId);
    //                if(visible!=null&&visible){
    //                    item.setVisibility(View.VISIBLE);
    //                }else{
    //                    item.setVisibility(View.GONE);
    //                }
    //            }
    //            public XHolder(@NonNull View itemView) {
    //                super(itemView);
    //            }
    //        }
    //
    //    }
    companion object {
        @JvmStatic
        fun newInstance(): ContactGroupFragment {
            return ContactGroupFragment()
        }
    }
}
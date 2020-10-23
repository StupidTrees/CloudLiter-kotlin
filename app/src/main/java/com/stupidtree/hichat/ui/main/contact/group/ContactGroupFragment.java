package com.stupidtree.hichat.ui.main.contact.group;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.stupidtree.hichat.R;
import com.stupidtree.hichat.data.model.UserRelation;
import com.stupidtree.hichat.ui.base.BaseFragment;
import com.stupidtree.hichat.ui.base.BaseListAdapter;
import com.stupidtree.hichat.ui.base.BaseViewHolder;
import com.stupidtree.hichat.ui.base.DataState;
import com.stupidtree.hichat.utils.ActivityUtils;
import com.stupidtree.hichat.utils.ImageUtils;
import com.stupidtree.hichat.utils.TextUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;

/**
 * 联系人页面的Fragment
 */
public class ContactGroupFragment extends BaseFragment<ContactGroupViewModel> {

    /**
     * View绑定区
     */
    @BindView(R.id.place_holder)
    ViewGroup placeHolder;//列表无内容时显示的布局

    @BindView(R.id.place_holder_text)
    TextView placeHolderText; //不显示列表时显示文字


    @BindView(R.id.list)
    RecyclerView list; //列表

    @BindView(R.id.refresh)
    SwipeRefreshLayout refreshLayout;


    /**
     * 适配器区
     */
    XListAdapter listAdapter;//列表适配器


    public ContactGroupFragment() {
    }

    public static ContactGroupFragment newInstance() {
        return new ContactGroupFragment();
    }

    @Override
    protected Class<ContactGroupViewModel> getViewModelClass() {
        return ContactGroupViewModel.class;
    }


    @Override
    protected void initViews(View view) {
        //初始化一下列表的view
        listAdapter = new XListAdapter(getContext(), new LinkedList<>());
        list.setAdapter(listAdapter);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        listAdapter.setOnItemClickListener((UserRelation data, View card, int position) -> {
            //点击列表项时，跳转到对应用户的Profile页面
            if (!data.isLabel()) {
                ActivityUtils.startProfileActivity(requireActivity(), String.valueOf(data.getFriendId()));
            }

        });
        //设置下拉刷新
        refreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent);
        refreshLayout.setOnRefreshListener(() -> viewModel.startFetchData());

        //当列表数据变更时，将自动调用本匿名函数
        viewModel.getListData().observe(this, contactListState -> {
            refreshLayout.setRefreshing(false);
            if (contactListState.getState() == DataState.STATE.SUCCESS) {
                //状态为”成功“，那么列表设置为可见，并通知列表适配器丝滑地更新列表项
                listAdapter.notifyItemChangedSmooth(contactListState.getData(), (oldData, newData) -> !Objects.equals(oldData, newData) || !Objects.equals(oldData.getRemark(), newData.getRemark()));
                if (contactListState.getData().size() > 0) {
                    list.setVisibility(View.VISIBLE);
                    placeHolder.setVisibility(View.GONE);
                } else {
                    list.setVisibility(View.GONE);
                    placeHolder.setVisibility(View.VISIBLE);
                    placeHolderText.setText(R.string.no_contact);
                }

            } else if (contactListState.getState() == DataState.STATE.NOT_LOGGED_IN) {
                //状态为”未登录“，那么设置”未登录“内东西为可见，隐藏列表
                list.setVisibility(View.GONE);
                placeHolder.setVisibility(View.VISIBLE);
                placeHolderText.setText(R.string.not_logged_in);
            } else if (contactListState.getState() == DataState.STATE.FETCH_FAILED) {
                //状态为”获取失败“，那么弹出提示
                list.setVisibility(View.GONE);
                placeHolder.setVisibility(View.VISIBLE);
                placeHolderText.setText(R.string.fetch_failed);
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_contact_list;
    }


    @Override
    public void onResume() {
        super.onResume();
        viewModel.startFetchData();
    }


    /**
     * 定义本页面的列表适配器
     */
    static class XListAdapter extends BaseListAdapter<UserRelation, XListAdapter.XHolder> {
        private static final int TYPE_LABEL = 69;
        private static final int TYPE_ITEM = 678;

        public XListAdapter(Context mContext, List<UserRelation> mBeans) {
            super(mContext, mBeans);
        }

        @Override
        protected int getLayoutId(int viewType) {
            if (viewType == TYPE_ITEM) {
                return R.layout.fragment_contact_list_item;
            } else {
                return R.layout.fragment_contact_group_item_label;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (mBeans.get(position).isLabel()) {
                return TYPE_LABEL;
            } else {
                return TYPE_ITEM;
            }
        }

        @Override
        protected void bindHolder(@NonNull XHolder holder, @Nullable UserRelation data, int position) {
            if (data != null) {
                if (data.isLabel()) {
                    holder.name.setText(data.getFriendNickname());
                } else {
                    //显示头像
                    ImageUtils.loadAvatarInto(mContext, data.getFriendAvatar(), holder.avatar);
                    //显示名称(备注)
                    if (!TextUtils.isEmpty(data.getRemark())) {
                        holder.name.setText(data.getRemark());
                    } else {
                        holder.name.setText(data.getFriendNickname());
                    }
                    //设置点击事件
                    if (mOnItemClickListener != null) {
                        holder.item.setOnClickListener(view -> mOnItemClickListener.onItemClick(data, view, position));
                    }
                }

            }

        }

        @Override
        public XHolder createViewHolder(View v, int viewType) {
            return new XHolder(v);
        }

        static class XHolder extends BaseViewHolder {
            @BindView(R.id.name)
            TextView name;
            @BindView(R.id.item)
            ViewGroup item;
            @Nullable
            @BindView(R.id.avatar)
            ImageView avatar;

            public XHolder(@NonNull View itemView) {
                super(itemView);
            }
        }

    }
}
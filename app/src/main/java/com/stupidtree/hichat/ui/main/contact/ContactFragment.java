package com.stupidtree.hichat.ui.main.contact;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.stupidtree.hichat.R;
import com.stupidtree.hichat.data.model.FriendContact;
import com.stupidtree.hichat.ui.base.BaseFragment;
import com.stupidtree.hichat.ui.base.BaseListAdapter;
import com.stupidtree.hichat.ui.base.BaseViewHolder;
import com.stupidtree.hichat.ui.base.DataState;
import com.stupidtree.hichat.utils.ActivityUtils;
import com.stupidtree.hichat.utils.ImageUtils;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;

/**
 * 联系人页面的Fragment
 */
public class ContactFragment extends BaseFragment<ContactViewModel> {

    /**
     * View绑定区
     */
    @BindView(R.id.not_logged_in)
    ViewGroup notLoggedIn;//未登录显示的东西

    @BindView(R.id.list)
    RecyclerView list; //列表

    /**
     * 适配器区
     */
    XListAdapter listAdapter;//列表适配器



    public ContactFragment() {
    }

    public static ContactFragment newInstance() {
        return new ContactFragment();
    }

    @Override
    protected Class<ContactViewModel> getViewModelClass() {
        return ContactViewModel.class;
    }

    @Override
    protected void initViews(View view) {
        //初始化一下列表的view
        listAdapter = new XListAdapter(getContext(), new LinkedList<>());
        list.setAdapter(listAdapter);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        listAdapter.setOnItemClickListener((FriendContact data, View card, int position) -> {
            //点击列表项时，跳转到对应用户的Profile页面
            ActivityUtils.startProfileActivity(requireActivity(), String.valueOf(data.getId()));
        });

        //当列表数据变更时，将自动调用本匿名函数
        viewModel.getListData().observe(this, contactListState -> {
            if (contactListState == null) {
                return;
            }
            if (contactListState.getState() == DataState.STATE.SUCCESS) {
                //状态为”成功“，那么列表设置为可见，并通知列表适配器丝滑地更新列表项
                list.setVisibility(View.VISIBLE);
                notLoggedIn.setVisibility(View.GONE);
                listAdapter.notifyItemChangedSmooth(contactListState.getData(), false);
            } else if (contactListState.getState() == DataState.STATE.NOT_LOGGED_IN) {
                //状态为”未登录“，那么设置”未登录“内东西为可见，隐藏列表
                list.setVisibility(View.GONE);
                notLoggedIn.setVisibility(View.VISIBLE);
            } else if (contactListState.getState() == DataState.STATE.FETCH_FAILED) {
                //状态为”获取失败“，那么弹出提示
                list.setVisibility(View.VISIBLE);
                notLoggedIn.setVisibility(View.GONE);
                Toast.makeText(getContext(), "获取失败" + contactListState.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_contact;
    }


    @Override
    public void onResume() {
        super.onResume();
        viewModel.startFetchData();
    }





    /**
     * 定义本页面的列表适配器
     */
    static class XListAdapter extends BaseListAdapter<FriendContact, XListAdapter.XHolder> {


        public XListAdapter(Context mContext, List<FriendContact> mBeans) {
            super(mContext, mBeans);
        }

        @Override
        protected int getLayoutId(int viewType) {
            return R.layout.fragment_contact_list_item;
        }

        @Override
        protected void bindHolder(@NonNull XHolder holder, @Nullable FriendContact data, int position) {
            if (data != null) {
                //显示头像
                ImageUtils.loadAvatarInto(mContext, data.getAvatar(), holder.avatar);
                //显示名称(备注)
                if(data.getRemark()!=""){
                    holder.name.setText(data.getRemark());
                }
                else {
                    holder.name.setText(data.getName());
                }

                //设置点击事件
                if (mOnItemClickListener != null) {
                    holder.item.setOnClickListener(view -> mOnItemClickListener.onItemClick(data, view, position));
                }
            }

        }

        @Override
        public XHolder createViewHolder(View v, int viewType) {
            return new XHolder(v);
        }

        static class XHolder extends BaseViewHolder {
            //ButterKnife 永远的神
            @BindView(R.id.name)
            TextView name;
            @BindView(R.id.item)
            ViewGroup item;
            @BindView(R.id.avatar)
            ImageView avatar;


            public XHolder(@NonNull View itemView) {
                super(itemView);
            }
        }

    }
}
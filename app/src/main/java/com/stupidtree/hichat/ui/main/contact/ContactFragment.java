package com.stupidtree.hichat.ui.main.contact;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.stupidtree.hichat.R;
import com.stupidtree.hichat.data.model.UserRelation;
import com.stupidtree.hichat.ui.base.BaseFragment;
import com.stupidtree.hichat.ui.base.BaseListAdapter;
import com.stupidtree.hichat.ui.base.BaseTabAdapter;
import com.stupidtree.hichat.ui.base.BaseViewHolder;
import com.stupidtree.hichat.ui.base.DataState;
import com.stupidtree.hichat.ui.main.contact.group.ContactGroupFragment;
import com.stupidtree.hichat.ui.main.contact.list.ContactListFragment;
import com.stupidtree.hichat.utils.ActivityUtils;
import com.stupidtree.hichat.utils.ImageUtils;
import com.stupidtree.hichat.utils.TextUtils;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;

/**
 * 联系人页面的Fragment
 */
public class ContactFragment extends BaseFragment<ContactViewModel> {

    /**
     * View绑定区
     */

    @BindView(R.id.search_friend)
    View searchFriendButton;

    @BindView(R.id.relation_event)
    View relationEventButton;

    @BindView(R.id.edit_group)
    View editGroupButton;

    @BindView(R.id.unread)
    TextView unreadText;

    @BindView(R.id.pager)
    ViewPager pager;

    @BindView(R.id.tabs)
    TabLayout tabs;


    public ContactFragment() {
    }

    public static ContactFragment newInstance() {
        return new ContactFragment();
    }

    @Override
    protected Class<ContactViewModel> getViewModelClass() {
        return ContactViewModel.class;
    }


    private void setUpButtons() {
        searchFriendButton.setOnClickListener(view -> ActivityUtils.startSearchActivity(requireContext()));
        relationEventButton.setOnClickListener(view -> ActivityUtils.startRelationEventActivity(requireContext()));
        editGroupButton.setOnClickListener(view -> ActivityUtils.startGroupEditorActivity(getActivity()));
    }

    @Override
    protected void initViews(View view) {
        setUpButtons();
        viewModel.getUnReadLiveData().observe(this, integerDataState -> {
            if (integerDataState.getState() == DataState.STATE.SUCCESS) {
                if (integerDataState.getData() > 0) {
                    unreadText.setVisibility(View.VISIBLE);
                    unreadText.setText(String.valueOf(integerDataState.getData()));
                } else {
                    unreadText.setVisibility(View.GONE);
                }

            } else {
                unreadText.setVisibility(View.GONE);
            }
        });
        pager.setAdapter(new BaseTabAdapter(getChildFragmentManager(),2) {
            @Override
            protected Fragment initItem(int position) {
                if(position==0){
                    return ContactListFragment.newInstance();
                }else{
                    return ContactGroupFragment.newInstance();
                }
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                if(position==0) return getString(R.string.contact_friend_list);
                else return getString(R.string.contact_friend_group);
            }
        });
        tabs.setupWithViewPager(pager);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_contact;
    }


    @Override
    public void onResume() {
        super.onResume();
        viewModel.startFetchData();
        viewModel.startFetchUnread();

    }


    /**
     * 定义本页面的列表适配器
     */
    static class XListAdapter extends BaseListAdapter<UserRelation, XListAdapter.XHolder> {


        public XListAdapter(Context mContext, List<UserRelation> mBeans) {
            super(mContext, mBeans);
        }

        @Override
        protected int getLayoutId(int viewType) {
            return R.layout.fragment_contact_list_item;
        }

        @Override
        protected void bindHolder(@NonNull XHolder holder, @Nullable UserRelation data, int position) {
            if (data != null) {
                //显示头像
                ImageUtils.loadAvatarInto(mContext, data.getAvatar(), holder.avatar);
                //显示名称(备注)
                if (!TextUtils.isEmpty(data.getRemark())) {
                    holder.name.setText(data.getRemark());
                } else {
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
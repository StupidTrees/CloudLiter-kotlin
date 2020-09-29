package com.stupidtree.hichat.ui.search;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.stupidtree.hichat.R;
import com.stupidtree.hichat.data.model.UserSearched;
import com.stupidtree.hichat.ui.base.BaseActivity;
import com.stupidtree.hichat.ui.base.BaseListAdapter;
import com.stupidtree.hichat.ui.base.BaseViewHolder;
import com.stupidtree.hichat.ui.base.DataState;
import com.stupidtree.hichat.utils.ActivityUtils;
import com.stupidtree.hichat.utils.ImageUtils;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;

/**
 * "搜索"页面Activity
 */
public class SearchActivity extends BaseActivity<SearchViewModel> {

    /**
     * View绑定区
     */
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.searchview)
    EditText searchText;
    @BindView(R.id.loading)
    ProgressBar progressLoading;
    @BindView(R.id.list)
    RecyclerView list;

    /**
     * 适配器区
     */
    SListAdapter listAdapter; //搜索结果列表的Adapter

    @Override
    protected Class<SearchViewModel> getViewModelClass() {
        return SearchViewModel.class;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolbarActionBack(toolbar);
        setWindowParams(true,true,false);
    }

    @Override
    protected void initViews() {
        //设置列表
        listAdapter = new SListAdapter(getThis(),new LinkedList<>());
        list.setAdapter(listAdapter);
        list.setLayoutManager(new LinearLayoutManager(getThis()));
        listAdapter.setOnItemClickListener((data, card, position) -> {
            //点击要跳转到用户资料页
            ActivityUtils.startProfileActivity(getThis(), String.valueOf(data.getId()));
        });

        //为viewModel的各种data设置监听
        viewModel.getSearchListStateLiveData().observe(this, searchListState -> {
            Log.e("refresh", String.valueOf(searchListState.getData()));
            progressLoading.setVisibility(View.GONE);
            if(searchListState.getState()== DataState.STATE.SUCCESS){
                listAdapter.notifyItemChangedSmooth(searchListState.getData(),false);
            }
            if(searchListState.getState()!= DataState.STATE.SUCCESS){
                Toast.makeText(getApplicationContext(),"搜索失败！",Toast.LENGTH_SHORT).show();
            }
          });

        //搜索输入框选择”搜索“的动作监听
        searchText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (TextUtils.isEmpty(textView.getText().toString())) return false;
            if (i == EditorInfo.IME_ACTION_GO || i == EditorInfo.IME_ACTION_SEARCH) {
                // 隐藏软键盘
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                //通知ViewModel进行搜索，传入搜索框的文本作为检索语句
                viewModel.beginSearch(searchText.getText().toString());
                progressLoading.setVisibility(View.VISIBLE);
                return true;
            }
            return false;
        });

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_search;
    }

    /**
     * 本页面搜索结果列表的适配器
     */
    static class SListAdapter extends BaseListAdapter<UserSearched, SListAdapter.SViewHolder>{


        public SListAdapter(Context mContext, List<UserSearched> mBeans) {
            super(mContext, mBeans);
        }

        @Override
        protected int getLayoutId(int viewType) {
            return R.layout.activity_search_list_item;
        }

        @Override
        protected void bindHolder(@NonNull SViewHolder holder, @Nullable UserSearched data, int position) {
            if(data!=null){
                //显示头像
                ImageUtils.loadAvatarInto(mContext,data.getAvatar(),holder.avatar);
                //显示各种其他信息
                holder.username.setText(data.getUsername());
                holder.nickname.setText(data.getNickname());
                //点击事件
                holder.item.setOnClickListener(view -> {
                    if(mOnItemClickListener!=null){
                        mOnItemClickListener.onItemClick(data,view,position);
                    }
                });
            }
        }


        @Override
        public SViewHolder createViewHolder(View v, int viewType) {
            return new SViewHolder(v);
        }

        static class SViewHolder extends BaseViewHolder {
            @BindView(R.id.nickname)
            TextView nickname;
            @BindView(R.id.username)
            TextView username;
            @BindView(R.id.item)
            ViewGroup item;
            @BindView(R.id.avatar)
            ImageView avatar;
            public SViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }

}
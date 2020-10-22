package com.stupidtree.hichat.ui.group;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.stupidtree.hichat.R;
import com.stupidtree.hichat.ui.base.BaseActivity;
import com.stupidtree.hichat.ui.base.DataState;

import java.util.LinkedList;
import java.util.Objects;

import butterknife.BindView;

public class GroupEditorActivity extends BaseActivity<GroupEditorViewModel>{

    /**
     * View绑定区
     */
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.list)
    RecyclerView list;

    /**
     * 适配器
     */
    GroupListAdapter listAdapter;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_group_editor;
    }

    @Override
    protected Class<GroupEditorViewModel> getViewModelClass() {
        return GroupEditorViewModel.class;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true,true,false);
    }

    @Override
    protected void initViews() {
        setToolbarActionBack(toolbar);
        listAdapter = new GroupListAdapter(this,new LinkedList<>());
        list.setAdapter(listAdapter);
        list.setLayoutManager(new LinearLayoutManager(this));
        viewModel.getListData().observe(this, listDataState -> {
            if(listDataState.getState()== DataState.STATE.SUCCESS){
                listAdapter.notifyItemChangedSmooth(listDataState.getData(), Objects::equals);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.startRefresh();
    }
}
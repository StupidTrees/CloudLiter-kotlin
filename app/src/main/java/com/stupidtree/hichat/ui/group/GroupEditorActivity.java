package com.stupidtree.hichat.ui.group;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.stupidtree.hichat.R;
import com.stupidtree.hichat.data.model.RelationGroup;
import com.stupidtree.hichat.data.model.UserProfile;
import com.stupidtree.hichat.ui.base.BaseActivity;
import com.stupidtree.hichat.ui.base.DataState;
import com.stupidtree.hichat.ui.myprofile.ChangeInfoTrigger;
import com.stupidtree.hichat.ui.widgets.PopUpEditText;
import com.stupidtree.hichat.ui.widgets.PopUpText;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;

public class GroupEditorActivity extends BaseActivity<GroupEditorViewModel> {

    /**
     * View绑定区
     */
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.list)
    RecyclerView list;

    @BindView(R.id.add)//这里是添加按钮
            FloatingActionButton add;

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
        setWindowParams(true, true, false);
    }

    @Override
    protected void initViews() {
        setToolbarActionBack(toolbar);
        listAdapter = new GroupListAdapter(this, new LinkedList<>());
        list.setAdapter(listAdapter);
        list.setLayoutManager(new LinearLayoutManager(this));
        viewModel.getListData().observe(this, listDataState -> {
            if (listDataState.getState() == DataState.STATE.SUCCESS) {
                listAdapter.notifyItemChangedSmooth(listDataState.getData(), Objects::equals);
            }
        });


        //点击按钮添加分组
        add.setOnClickListener(view -> {
            new PopUpEditText()
                    .setTitle(R.string.add_group)
                    .setText("")
                    .setOnConfirmListener(text -> {
                        //控制viewModel发起添加分组请求
                        viewModel.startAddGroup(text);
                    })
                    .show(getSupportFragmentManager(), "edit");

        });

        //点击删除分组
        listAdapter.setOnDeleteClickListener((button, group, position) -> new PopUpText().setTitle(R.string.ensure_delete).setText("").setOnConfirmListener(new PopUpText.OnConfirmListener() {
            @Override
            public void OnConfirm() {
                viewModel.startDeleteGroup(group.getId());
            }
        }).show(getSupportFragmentManager(), "confirm"));

        viewModel.getAddGroupResult().observe(this, stringDataState -> {
            if (stringDataState.getState() == DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(), R.string.add_ok, Toast.LENGTH_SHORT).show();
                viewModel.startRefresh();
            } else {
                Toast.makeText(getApplicationContext(), "失败", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getDeleteGroupResult().observe(this, stringDataState -> {
            if (stringDataState.getState() == DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(), R.string.delete_ok, Toast.LENGTH_SHORT).show();
                viewModel.startRefresh();
            } else {
                Toast.makeText(getApplicationContext(), "失败", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.startRefresh();
    }
}
package com.stupidtree.hichat.ui.relation;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.stupidtree.hichat.R;
import com.stupidtree.hichat.data.model.UserRelation;
import com.stupidtree.hichat.ui.base.BaseActivity;
import com.stupidtree.hichat.ui.base.DataState;
import com.stupidtree.hichat.ui.widgets.PopUpEditText;

import butterknife.BindView;

public class RelationActivity extends BaseActivity<RelationViewModel> {

    @BindView(R.id.remark_layout)
    ViewGroup remarkLayout;

    @BindView(R.id.remark)
    TextView remarkText;


    @BindView(R.id.toolbar)
    Toolbar toolbar;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true, true, false);
        setToolbarActionBack(toolbar);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_relation;
    }

    @Override
    protected Class<RelationViewModel> getViewModelClass() {
        return RelationViewModel.class;
    }

    @Override
    protected void initViews() {
        remarkLayout.setOnClickListener(view -> {
            DataState<UserRelation> up = viewModel.getRelationData().getValue();
            if (up != null && up.getState() == DataState.STATE.SUCCESS) {
                new PopUpEditText()
                        .setTitle(R.string.prompt_set_remark)
                        .setText(up.getData().getRemark())
                        .setOnConfirmListener(text -> {
                            //控制viewModel发起更改昵称请求
                            viewModel.startChangeRemark(text);
                        })
                        .show(getSupportFragmentManager(), "edit");
            }

        });
        viewModel.getChangeRemarkResult().observe(this, stringDataState -> {
            if (stringDataState.getState() == DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(), R.string.avatar_change_success, Toast.LENGTH_SHORT).show();
                viewModel.startFetchRelationData(getIntent().getStringExtra("friendId"));
            } else {
                Toast.makeText(getApplicationContext(), "失败", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getRelationData().observe(this, friendContactDataState -> {
            if (friendContactDataState.getState() == DataState.STATE.SUCCESS) {
                remarkText.setText(friendContactDataState.getData().getRemark());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent().hasExtra("friendId")) {
            viewModel.startFetchRelationData(getIntent().getStringExtra("friendId"));
        }
    }
}
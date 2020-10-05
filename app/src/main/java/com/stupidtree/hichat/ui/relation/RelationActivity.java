package com.stupidtree.hichat.ui.relation;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.stupidtree.hichat.R;
import com.stupidtree.hichat.ui.base.BaseActivity;
import com.stupidtree.hichat.ui.base.DataState;

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
        setWindowParams(true,true,false);
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
        viewModel.getRelationData().observe(this, friendContactDataState -> {
            if(friendContactDataState.getState()== DataState.STATE.SUCCESS){
                remarkText.setText(friendContactDataState.getData().getRemark());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(getIntent().hasExtra("friendId")){
            viewModel.startFetchRelationData(getIntent().getStringExtra("friendId"));
        }
    }
}
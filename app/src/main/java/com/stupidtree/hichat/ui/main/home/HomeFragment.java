package com.stupidtree.hichat.ui.main.home;

import android.view.View;
import android.widget.TextView;
import com.stupidtree.hichat.R;
import com.stupidtree.hichat.ui.base.BaseFragment;

import butterknife.BindView;

public class HomeFragment extends BaseFragment<HomeViewModel> {


    @BindView(R.id.text_home)
    TextView textView;


    public HomeFragment(){}


    public static HomeFragment newInstance(){
        return new HomeFragment();
    }

    @Override
    protected Class<HomeViewModel> getViewModelClass() {
        return HomeViewModel.class;
    }

    @Override
    protected void initViews(View view) {
        viewModel.getText().observe(getViewLifecycleOwner(), s -> textView.setText(s));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home;
    }


}
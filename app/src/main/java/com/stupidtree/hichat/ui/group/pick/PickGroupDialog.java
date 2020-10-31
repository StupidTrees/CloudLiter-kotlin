package com.stupidtree.hichat.ui.group.pick;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.stupidtree.hichat.R;
import com.stupidtree.hichat.data.model.RelationGroup;
import com.stupidtree.hichat.ui.base.BaseViewHolder;
import com.stupidtree.hichat.ui.base.BasicSelectableListAdapter;
import com.stupidtree.hichat.ui.base.DataState;
import com.stupidtree.hichat.ui.widgets.TransparentBottomSheetDialog;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;

/**
 * 选择好友分组的底部弹窗
 */
public class PickGroupDialog extends TransparentBottomSheetDialog {

    @BindView(R.id.list)
    RecyclerView list;

    @BindView(R.id.confirm)
    View confirm;

    @BindView(R.id.cancel)
    View cancel;

    @BindView(R.id.loading)
    ProgressBar loading;

    PickGroupViewModel viewModel;
    LAdapter listAdapter;
    OnConfirmListener onConfirmListener;
    String initGroupId = null;
    public interface OnConfirmListener{
        void OnConfirmed(@Nullable RelationGroup group);
    }


    public PickGroupDialog setOnConfirmListener(OnConfirmListener onConfirmListener) {
        this.onConfirmListener = onConfirmListener;
        return this;
    }

    public PickGroupDialog setInitGroupId(String initGroupId) {
        this.initGroupId = initGroupId;
        return this;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_bottom_group_list;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())).get(PickGroupViewModel.class);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void initViews(View v) {
        listAdapter = new LAdapter(requireContext(),new LinkedList<>());
        list.setAdapter(listAdapter);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        viewModel.getListData().observe(this, listDataState -> {
            loading.setVisibility(View.INVISIBLE);
            if(listDataState.getState()== DataState.STATE.SUCCESS){
                listAdapter.setSelected(listDataState.getData(),initGroupId);
                listAdapter.notifyItemChangedSmooth(listDataState.getData());
            }
        });

        confirm.setOnClickListener(view -> {
            if(onConfirmListener!=null){
                onConfirmListener.OnConfirmed(listAdapter.getSelectedData());
                dismiss();
            }
        });

        cancel.setOnClickListener(view -> dismiss());
    }

    @Override
    public void onResume() {
        super.onResume();
        loading.setVisibility(View.VISIBLE);
        viewModel.startRefresh();
    }

    static class LAdapter extends BasicSelectableListAdapter<RelationGroup, LAdapter.LHolder> {


        public LAdapter(Context mContext, List<RelationGroup> mBeans) {
            super(mContext, mBeans);
        }

        @Override
        protected int getLayoutId(int viewType) {
            return R.layout.dialog_bottom_selectable_list_item;
        }

        @Override
        public LHolder createViewHolder(View v, int viewType) {
            return new LHolder(v);
        }

        @Override
        protected void bindHolder(@NonNull LHolder holder, @Nullable RelationGroup data, int position) {
            if (data != null) {
                holder.text.setText(data.getGroupName());
            }
            if(position==getSelectedIndex()){ //若被选中
                holder.selected.setVisibility(View.VISIBLE);
            }else{
                holder.selected.setVisibility(View.GONE);
            }
            holder.item.setOnClickListener(view -> selectItem(position,data));
        }


        public void setSelected(List<RelationGroup> data,String groupId) {
            mBeans.clear();
            mBeans.addAll(data);
            if(groupId==null&&data.size()>0){
                selectedIndex = 0;
                selectedData = data.get(0);
            }else{
                for(RelationGroup relationGroup:mBeans){
                    if(Objects.equals(relationGroup.getId(),groupId)){
                        selectedIndex = mBeans.indexOf(relationGroup);
                        selectedData = relationGroup;
                    }
                }
            }

        }



        static class LHolder extends BaseViewHolder {
            @BindView(R.id.text)
            TextView text;
            @BindView(R.id.item)
            ViewGroup item;

            @BindView(R.id.selected)
            ImageView selected;
            public LHolder(@NonNull View itemView) {
                super(itemView);
            }
        }

    }

}

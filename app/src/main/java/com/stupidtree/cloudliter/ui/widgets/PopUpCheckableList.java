package com.stupidtree.cloudliter.ui.widgets;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.stupidtree.cloudliter.R;
import com.stupidtree.cloudliter.ui.base.BaseListAdapter;
import com.stupidtree.cloudliter.ui.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;

/**
 * 圆角的文本框底部弹窗
 */
public class PopUpCheckableList<T> extends TransparentBottomSheetDialog {
    /**
     * View绑定区
     */
    @BindView(R.id.title)
    TextView title;

    @BindView(R.id.list)
    RecyclerView list;

    @StringRes
    Integer init_title;


    /**
     * 适配器区
     */
    LAdapter<T> listAdapter;

    /**
     * 不得已放在UI里的数据
     */
    List<ItemData<T>> listRes;

    OnConfirmListener<T> onConfirmListener;

    public interface OnConfirmListener<T>{
        void OnConfirm(String title, T key);
    }


    public PopUpCheckableList<T> setTitle(@StringRes int title) {
        this.init_title = title;
        return this;
    }

    public PopUpCheckableList<T> setListData(List<String> titles, List<T> keys){
        listRes = new ArrayList<>();
        for(int i =0;i<Math.min(titles.size(),keys.size());i++){
            listRes.add(new ItemData<>(titles.get(i),keys.get(i)));
        }
        return this;
    }

    


    public PopUpCheckableList<T> setOnConfirmListener(OnConfirmListener<T> onConfirmListener){
        this.onConfirmListener = onConfirmListener;
        return this;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_bottom_checkable_list;
    }

    @Override
    public void onStart() {
        super.onStart();
        listAdapter.notifyDataSetChanged();
    }

    @Override
    protected void initViews(View v) {
        listAdapter = new LAdapter<>(getContext(),listRes);
        listAdapter.setOnItemClickListener((data, card, position) -> {
            if(onConfirmListener!=null){
                onConfirmListener.OnConfirm(data.name,data.data);
                dismiss();
            }
        });
        list.setAdapter(listAdapter);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        if(init_title!=null){
            title.setText(init_title);
        }
    }


    static class ItemData<K>{
        String name;
        K data;

        public ItemData(String name, K data) {
            this.name = name;
            this.data = data;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ItemData<?> keyData = (ItemData<?>) o;
            return Objects.equals(data, keyData.data);
        }

        @Override
        public int hashCode() {
            return Objects.hash(data);
        }
    }
    static class LAdapter<C> extends BaseListAdapter<ItemData<C>, LAdapter.LHolder> {


        public LAdapter(Context mContext, List<ItemData< C>> mBeans) {
            super(mContext, mBeans);
        }

        @Override
        protected int getLayoutId(int viewType) {
            return R.layout.dialog_bottom_checkable_list_item;
        }

        @Override
        public LHolder createViewHolder(View v, int viewType) {
            return new LHolder(v);
        }

        @Override
        protected void bindHolder(@NonNull LHolder holder, @Nullable ItemData< C> data, int position) {
            if (data != null) {
                holder.text.setText(data.name);
            }

            holder.item.setOnClickListener(view -> {
                if(mOnItemClickListener!=null){
                    mOnItemClickListener.onItemClick(data,view,position);
                }
            });
        }


        static class LHolder extends BaseViewHolder{
            @BindView(R.id.text)
            TextView text;
            @BindView(R.id.item)
            ViewGroup item;
            public LHolder(@NonNull View itemView) {
                super(itemView);
            }
        }

    }
}

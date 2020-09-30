package com.stupidtree.hichat.ui.widgets;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.stupidtree.hichat.R;
import com.stupidtree.hichat.ui.base.BaseViewHolder;
import com.stupidtree.hichat.ui.base.BasicSelectableListAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;

/**
 * 圆角的文本框底部弹窗
 */
public class PopUpSelectableList<T> extends TransparentBottomSheetDialog {
    /**
     * View绑定区
     */
    @BindView(R.id.title)
    TextView title;

    @BindView(R.id.list)
    RecyclerView list;

    @BindView(R.id.confirm)
    View confirm;

    @BindView(R.id.cancel)
    View cancel;

    @StringRes
    Integer init_title;
    @StringRes
    Integer init_hint;
    String init_text;

    /**
     * 适配器区
     */
    LAdapter<T> listAdapter;

    /**
     * 不得已放在UI里的数据
     */
    List<ItemData<T>> listRes;
    T init_selected;

    OnConfirmListener<T> onConfirmListener;

    public interface OnConfirmListener<T>{
        void OnConfirm(String title,T key);
    }


    public PopUpSelectableList<T> setTitle(@StringRes int title) {
        this.init_title = title;
        return this;
    }

    public PopUpSelectableList<T> setText(String text) {
        this.init_text = text;
        return this;
    }
    public PopUpSelectableList<T> setListData(List<String> titles,List<T> keys){
        listRes = new ArrayList<>();
        for(int i =0;i<Math.min(titles.size(),keys.size());i++){
            listRes.add(new ItemData<>(titles.get(i),keys.get(i)));
        }
        return this;
    }

    public PopUpSelectableList<T> setHint(@StringRes int hint) {
        this.init_hint = hint;
        return this;
    }
    
    public PopUpSelectableList<T> setInitValue(T value){
        this.init_selected = value;
        return this;
    }
    

    public PopUpSelectableList<T> setOnConfirmListener(OnConfirmListener<T> onConfirmListener){
        this.onConfirmListener = onConfirmListener;
        return this;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_bottom_selectable_list;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(init_selected!=null){
            listAdapter.setSelected(new ItemData<>(null,init_selected));
        }
        listAdapter.notifyDataSetChanged();
    }

    @Override
    protected void initViews(View v) {
        listAdapter = new LAdapter<>(getContext(),listRes);
        list.setAdapter(listAdapter);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        if(init_title!=null){
            title.setText(init_title);
        }
        cancel.setOnClickListener(view -> dismiss());
        confirm.setOnClickListener(view -> {
            if(onConfirmListener!=null){
                ItemData<T> data = listAdapter.getSelectedData();
                Log.e("data", String.valueOf(data));
                if(data!=null){
                    onConfirmListener.OnConfirm(data.name,data.data);
                }
               }
            dismiss();
        });
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
    static class LAdapter<C> extends BasicSelectableListAdapter<ItemData<C>, LAdapter.LHolder>{


        public LAdapter(Context mContext, List<ItemData< C>> mBeans) {
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
        protected void bindHolder(@NonNull LHolder holder, @Nullable ItemData< C> data, int position) {
            if (data != null) {
                holder.text.setText(data.name);
            }
            if(position==getSelectedIndex()){ //若被选中
                holder.selected.setVisibility(View.VISIBLE);
            }else{
                holder.selected.setVisibility(View.GONE);
            }
            holder.item.setOnClickListener(view -> selectItem(position,data));
        }


        static class LHolder extends BaseViewHolder{
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

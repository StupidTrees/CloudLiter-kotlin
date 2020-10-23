package com.stupidtree.hichat.ui.base;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * 一个基本的可选择列表Adapter
 *
 * @param <T> T泛型指定每个列表项的数据Model的类型
 * @param <H> H泛型指定ViewHolder的类型
 */
public abstract class BasicSelectableListAdapter<T, H extends RecyclerView.ViewHolder> extends BaseListAdapter<T, H> {

    protected int selectedIndex = 0;
    protected T selectedData;

    public BasicSelectableListAdapter(Context mContext, List<T> mBeans) {
        super(mContext, mBeans);
    }


    public int getSelectedIndex() {
        return selectedIndex;
    }


    /**
     * 设置选中项目
     *
     * @param data 选中数据
     */
    public void setSelected(T data) {
        if (mBeans.contains(data)) {
            selectedIndex = mBeans.indexOf(data);
            selectedData = data;
        }
    }



    @Nullable
    public T getSelectedData() {
        return selectedData;
    }

    protected void selectItem(int index, T data) {
        int old = selectedIndex;
        this.selectedData = data;
        notifyItemChanged(old);//原来选中的位置刷新
        selectedIndex = index;
        notifyItemChanged(selectedIndex);//新选中的位置刷新
    }


}

package com.stupidtree.hichat.ui.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;


/**
 * 一个基本的可选择列表Adapter
 * @param <T> T泛型指定每个列表项的数据Model的类型
 * @param <H> H泛型指定ViewHolder的类型
 */
public abstract class BasicSelectableListAdapter<T, H extends RecyclerView.ViewHolder> extends BaseListAdapter<T,H> {

    private int selectedIndex = 0;
    private T selectedData;

    public BasicSelectableListAdapter(Context mContext, List<T> mBeans) {
        super(mContext, mBeans);
    }


    public  int getSelectedIndex(){
        return selectedIndex;
    }


    /**
     * 设置选中项目
     * @param data 选中数据
     */
    public void setSelected(T data){
        if(mBeans.indexOf(data)>=0){
            selectedIndex = mBeans.indexOf(data);
            selectedData = data;
        }
    }
    @Nullable
    public T getSelectedData(){
        return selectedData;
    }

    protected void selectItem(int index,T data){
        int old = selectedIndex;
        this.selectedData = data;
        notifyItemChanged(old);//原来选中的位置刷新
        selectedIndex = index;
        notifyItemChanged(selectedIndex);//新选中的位置刷新
    }


}

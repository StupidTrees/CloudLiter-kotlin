package com.stupidtree.hichat.ui.base;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.ButterKnife;

/**
 * 本项目所有RecycleView的ViewHolder的基类
 */
public class BaseViewHolder extends RecyclerView.ViewHolder {


    //让这个ViewHolder也支持ButterKnifeView注入
    public BaseViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this,itemView);
    }
}

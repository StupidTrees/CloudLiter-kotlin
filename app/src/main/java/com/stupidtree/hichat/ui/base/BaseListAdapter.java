package com.stupidtree.hichat.ui.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;


/**
 * 本项目一般的RecyclerView的Adapter的基类
 * @param <T> T泛型指定每个列表项的数据Model的类型
 * @param <H> H泛型指定ViewHolder的类型
 */
public abstract class BaseListAdapter<T, H extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<H> {
    /**
     * 标准三件：数据源列表、Context、Inflater对象
     */
    protected List<T> mBeans;
    protected Context mContext;
    LayoutInflater mInflater;

    /**
     * 提供一个单击的Listener和一个长按的Listener
     */
    protected OnItemClickListener<T> mOnItemClickListener;
    OnItemLongClickListener mOnItemLongClickListener;


    /**
     * 所有继承此类的Adapter都需要实现以下三个函数
     */

    //获取每个列表项的布局id
    protected abstract int getLayoutId(int viewType);
    //初始化每个holder
    public abstract H createViewHolder(View v, int viewType);
    //用于绑定每个holder
    protected abstract void bindHolder(@NonNull H holder, @Nullable T data, int position);



    public BaseListAdapter(Context mContext, List<T> mBeans) {
        this.mBeans = mBeans;
        this.mContext = mContext;
        mInflater = LayoutInflater.from(mContext);
    }

    public void setOnItemClickListener(OnItemClickListener<T> mOnItemClickLitener) {
        this.mOnItemClickListener = mOnItemClickLitener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener mOnItemLongClickLitener) {
        this.mOnItemLongClickListener = mOnItemLongClickLitener;
    }

    public List<T> getBeans() {
        return mBeans;
    }


    @NonNull
    @Override
    public H onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = mInflater.inflate(getLayoutId(viewType), parent, false);
        return createViewHolder(v, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull H holder, int position) {
        try {
            T data = mBeans.get(position);
            bindHolder(holder, data, position);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 偏移量，即不算在mBeans内的头部元素的数量
     **/
    int getIndexBias() {
        return 0;
    }

    boolean willNotifyNormalChange() { //当位置不发生变化时，是否刷新该item
        return true;
    }

    /**
     * 优雅地进行RecyclerView的刷新，自动识别新增、移动、删除
     * @param newL 新的数据List
     */
    public void notifyItemChangedSmooth(List<T> newL) {
        List<Integer> toInsert = new ArrayList<>();//记录变化的操作表，正表示加入，负表示删除
        Stack<Integer> toRemove = new Stack<>();
        List<T> remains = new ArrayList<>(); //留下来的元素
        //找到要移除的
        for (int i = 0; i < mBeans.size(); i++) {
            if (!newL.contains(mBeans.get(i))) toRemove.push(i);
        }
        //先处理删除,从后往前删
        while (toRemove.size() > 0) {
            int index = toRemove.pop();
            mBeans.remove(index);
            notifyItemRemoved(index + getIndexBias());
            notifyItemRangeChanged(index + getIndexBias(), mBeans.size() + getIndexBias());
        }
        //找到要插入的
        for (int i = 0; i < newL.size(); i++) {
            T ei = newL.get(i);
            if (!mBeans.contains(ei)) toInsert.add(i); //新加入的
            else remains.add(ei);
        }
        for (int i = 0; i < toInsert.size(); i++) {
            int index = toInsert.get(i);
            if (index > mBeans.size() - 1) mBeans.add(newL.get(index));
            else mBeans.add(index, newL.get(index));
            notifyItemInserted(index + getIndexBias());
            notifyItemRangeChanged(index + getIndexBias(), mBeans.size() + getIndexBias());
        }
        for (T ei : remains) { //保留的
            int oldIndex = mBeans.indexOf(ei);
            int newIndex = newL.indexOf(ei);
            if (oldIndex == newIndex && willNotifyNormalChange()) {
                mBeans.set(oldIndex, newL.get(newIndex));
                notifyItemChanged(newIndex + getIndexBias());
            } else {
                mBeans.remove(oldIndex);
                T to = newL.get(newIndex);
                if (newIndex > mBeans.size() - 1) mBeans.add(to);
                else mBeans.add(newIndex, to);
                notifyItemMoved(oldIndex + getIndexBias(), newIndex + getIndexBias());
                notifyItemRangeChanged(Math.min(oldIndex, newIndex) + getIndexBias(), mBeans.size() + getIndexBias());
            }
        }
    }

    /**
     * 优雅地进行RecyclerView的刷新，自动识别新增、移动、删除
     * @param newL 新的数据List
     * @param notifyNormalItem 对于那些位置不变的项目，是否原地刷新
     */
    public void notifyItemChangedSmooth(List<T> newL, boolean notifyNormalItem) {
        List<Integer> toInsert = new ArrayList<>();//记录变化的操作表，正表示加入，负表示删除
        Stack<Integer> toRemove = new Stack<>();
        List<T> remains = new ArrayList<>(); //留下来的元素
        //找到要移除的
        for (int i = 0; i < mBeans.size(); i++) {
            if (!newL.contains(mBeans.get(i))) toRemove.push(i);
        }
        //先处理删除,从后往前删
        while (toRemove.size() > 0) {
            int index = toRemove.pop();
            mBeans.remove(index);
            notifyItemRemoved(index + getIndexBias());
            notifyItemRangeChanged(index + getIndexBias(), mBeans.size() + getIndexBias());
        }
        //找到要插入的
        for (int i = 0; i < newL.size(); i++) {
            T ei = newL.get(i);
            if (!mBeans.contains(ei)) toInsert.add(i); //新加入的
            else remains.add(ei);
        }
        for (int i = 0; i < toInsert.size(); i++) {
            int index = toInsert.get(i);
            if (index > mBeans.size() - 1) mBeans.add(newL.get(index));
            else mBeans.add(index, newL.get(index));
            notifyItemInserted(index + getIndexBias());
            notifyItemRangeChanged(index + getIndexBias(), mBeans.size() + getIndexBias());
        }
        for (T ei : remains) { //保留的
            int oldIndex = mBeans.indexOf(ei);
            int newIndex = newL.indexOf(ei);
            if (oldIndex == newIndex && notifyNormalItem) {
                mBeans.set(oldIndex, newL.get(newIndex));
                notifyItemChanged(newIndex + getIndexBias());
            } else if (notifyNormalItem) {
                mBeans.remove(oldIndex);
                T to = newL.get(newIndex);
                if (newIndex > mBeans.size() - 1) mBeans.add(to);
                else mBeans.add(newIndex, to);
                notifyItemMoved(oldIndex + getIndexBias(), newIndex + getIndexBias());
                notifyItemRangeChanged(Math.min(oldIndex, newIndex) + getIndexBias(), mBeans.size() + getIndexBias());
            }
        }
    }


    /**
     * 优雅地进行RecyclerView的刷新，自动识别新增、移动、删除
     * @param newL 新的数据List
     * @param notifyNormalItem 对于那些位置不变的项目，是否原地刷新
     * @param comparator 用于排序，比较两个Item的Comparator
     */
    public void notifyItemChangedSmooth(List<T> newL, boolean notifyNormalItem, Comparator<T> comparator) {
        List<Integer> toInsert = new ArrayList<>();//记录变化的操作表，正表示加入，负表示删除
        Stack<Integer> toRemove = new Stack<>();
        List<T> remains = new ArrayList<>(); //留下来的元素
        //找到要移除的
        for (int i = 0; i < mBeans.size(); i++) {
            // boolean contains = false;
            if (!contains(newL, mBeans.get(i), comparator)) toRemove.push(i);
            // if(!newL.contains(mBeans.get(i))) toRemove.push(i);
        }
        //先处理删除,从后往前删
        while (toRemove.size() > 0) {
            int index = toRemove.pop();
            mBeans.remove(index);
            notifyItemRemoved(index + getIndexBias());
            notifyItemRangeChanged(index + getIndexBias(), mBeans.size() + getIndexBias());
        }
        //找到要插入的
        for (int i = 0; i < newL.size(); i++) {
            T ei = newL.get(i);
            if (!contains(mBeans, ei, comparator)) toInsert.add(i);
                //if(!mBeans.contains(ei)) toInsert.add(i); //新加入的
            else remains.add(ei);
        }
        for (int i = 0; i < toInsert.size(); i++) {
            int index = toInsert.get(i);
            if (index > mBeans.size() - 1) mBeans.add(newL.get(index));
            else mBeans.add(index, newL.get(index));
            notifyItemInserted(index + getIndexBias());
            notifyItemRangeChanged(index + getIndexBias(), mBeans.size() + getIndexBias());
        }
        for (T ei : remains) { //保留的
            int oldIndex = indexOf(mBeans, ei, comparator);
            int newIndex = indexOf(newL, ei, comparator);
            if (oldIndex == newIndex && notifyNormalItem) {
                mBeans.set(oldIndex, newL.get(newIndex));
                notifyItemChanged(newIndex + getIndexBias());
            } else if (notifyNormalItem) {
                mBeans.remove(oldIndex);
                T to = newL.get(newIndex);
                if (newIndex > mBeans.size() - 1) mBeans.add(to);
                else mBeans.add(newIndex, to);
                notifyItemMoved(oldIndex + getIndexBias(), newIndex + getIndexBias());
                notifyItemRangeChanged(Math.min(oldIndex, newIndex) + getIndexBias(), mBeans.size() + getIndexBias());
            }
        }
    }

    /**
     * 优雅地进行RecyclerView的刷新，自动识别新增、移动、删除
     * @param newL 新的数据List
     * @param judge 判定某一个Item，当其位置不变时，是否原地刷新
     */
    public void notifyItemChangedSmooth(List<T> newL, RefreshJudge<T> judge) {
        List<Integer> toInsert = new ArrayList<>();//记录变化的操作表，正表示加入，负表示删除
        Stack<Integer> toRemove = new Stack<>();
        List<T> remains = new ArrayList<>(); //留下来的元素
        //找到要移除的
        for (int i = 0; i < mBeans.size(); i++) {
            if (!newL.contains(mBeans.get(i))) toRemove.push(i);
        }
        //先处理删除,从后往前删
        while (toRemove.size() > 0) {
            int index = toRemove.pop();
            mBeans.remove(index);
            notifyItemRemoved(index + getIndexBias());
            notifyItemRangeChanged(index + getIndexBias(), mBeans.size() + getIndexBias());
        }
        //找到要插入的
        for (int i = 0; i < newL.size(); i++) {
            T ei = newL.get(i);
            if (!mBeans.contains(ei)) toInsert.add(i); //新加入的
            else remains.add(ei);
        }
        for (int i = 0; i < toInsert.size(); i++) {
            int index = toInsert.get(i);
            if (index > mBeans.size() - 1) mBeans.add(newL.get(index));
            else mBeans.add(index, newL.get(index));
            notifyItemInserted(index + getIndexBias());
            notifyItemRangeChanged(index + getIndexBias(), mBeans.size() + getIndexBias());
        }
        for (T ei : remains) { //保留的
            int oldIndex = mBeans.indexOf(ei);
            int newIndex = newL.indexOf(ei);
            boolean willRefresh = judge.judge(mBeans.get(oldIndex), newL.get(newIndex));
            if (oldIndex == newIndex && willRefresh) {
                mBeans.set(oldIndex, newL.get(newIndex));
                notifyItemChanged(newIndex + getIndexBias());
            } else if (oldIndex != newIndex) {
                mBeans.remove(oldIndex);
                T to = newL.get(newIndex);
                if (newIndex > mBeans.size() - 1) mBeans.add(to);
                else mBeans.add(newIndex, to);
                notifyItemMoved(oldIndex + getIndexBias(), newIndex + getIndexBias());
                notifyItemRangeChanged(Math.min(oldIndex, newIndex) + getIndexBias(), mBeans.size() + getIndexBias());
            }
        }
    }


    /**
     * 优雅地进行RecyclerView的刷新，自动识别新增、移动、删除
     * @param newL 新的数据List
     * @param judge 判定某一个Item，当其位置不变时，是否原地刷新
     * @param comparator 用于排序，比较两个Item的Comparator
     */
    public void notifyItemChangedSmooth(List<T> newL, RefreshJudge<T> judge, Comparator<T> comparator) {
        List<Integer> toInsert = new ArrayList<>();//记录变化的操作表，正表示加入，负表示删除
        Stack<Integer> toRemove = new Stack<>();
        List<T> remains = new ArrayList<>(); //留下来的元素
        //找到要移除的
        for (int i = 0; i < mBeans.size(); i++) {
            // boolean contains = false;
            if (!contains(newL, mBeans.get(i), comparator)) toRemove.push(i);
            // if(!newL.contains(mBeans.get(i))) toRemove.push(i);
        }
        //先处理删除,从后往前删
        while (toRemove.size() > 0) {
            int index = toRemove.pop();
            mBeans.remove(index);
            notifyItemRemoved(index + getIndexBias());
            notifyItemRangeChanged(index + getIndexBias(), mBeans.size() + getIndexBias());
        }
        //找到要插入的
        for (int i = 0; i < newL.size(); i++) {
            T ei = newL.get(i);
            if (!contains(mBeans, ei, comparator)) toInsert.add(i);
                //if(!mBeans.contains(ei)) toInsert.add(i); //新加入的
            else remains.add(ei);
        }
        for (int i = 0; i < toInsert.size(); i++) {
            int index = toInsert.get(i);
            if (index > mBeans.size() - 1) mBeans.add(newL.get(index));
            else mBeans.add(index, newL.get(index));
            notifyItemInserted(index + getIndexBias());
            notifyItemRangeChanged(index + getIndexBias(), mBeans.size() + getIndexBias());
        }
        for (T ei : remains) { //保留的
            int oldIndex = indexOf(mBeans, ei, comparator);
            int newIndex = indexOf(newL, ei, comparator);
            boolean willRefresh = judge.judge(mBeans.get(oldIndex), newL.get(newIndex));
            if (oldIndex == newIndex && willRefresh) {
                mBeans.set(oldIndex, newL.get(newIndex));
                notifyItemChanged(newIndex + getIndexBias());
            } else if (oldIndex != newIndex) {
                mBeans.remove(oldIndex);
                T to = newL.get(newIndex);
                if (newIndex > mBeans.size() - 1) mBeans.add(to);
                else mBeans.add(newIndex, to);
                notifyItemMoved(oldIndex + getIndexBias(), newIndex + getIndexBias());
                notifyItemRangeChanged(Math.min(oldIndex, newIndex) + getIndexBias(), mBeans.size() + getIndexBias());
            }
        }
    }


    @Override
    public int getItemCount() {
        return mBeans.size() + getIndexBias();
    }

    private boolean contains(List<T> collection, T element, Comparator<T> comparator) {
        return indexOf(collection, element, comparator) != -1;
    }

    private int indexOf(List<T> collection, T element, Comparator<T> comparator) {
        for (T t : collection) {
            if (comparator.compare(element, t) == 0) return collection.indexOf(t);
        }
        return -1;
    }


    public interface OnItemClickListener<T> {
        void onItemClick(T data, View card, int position);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(View view, int position);
    }

    public interface RefreshJudge<T> {
        boolean judge(T oldData, T newData);
    }

}

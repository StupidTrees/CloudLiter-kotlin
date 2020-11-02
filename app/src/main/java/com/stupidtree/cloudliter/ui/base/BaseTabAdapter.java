package com.stupidtree.cloudliter.ui.base;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import org.jetbrains.annotations.NotNull;

/**
 * 一个简单的ViewPagerAdapter实现
 */
public abstract class BaseTabAdapter extends FragmentStatePagerAdapter {
    protected Fragment[] mFragments;
    protected int size = 0;
    private Fragment currentFragment;
    private boolean destroyFragment = true;

    public BaseTabAdapter(FragmentManager fm, int size) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mFragments = new Fragment[size];
        this.size = size;
    }


    public Fragment getCurrentFragment() {
        return currentFragment;
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        try {
            currentFragment = (Fragment) object;
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.setPrimaryItem(container, position, object);

    }

    public BaseTabAdapter setDestroyFragment(boolean destroyFragment) {
        this.destroyFragment = destroyFragment;
        return this;
    }

    @NotNull
    @Override
    public Fragment getItem(int position) {
        mFragments[position] = initItem(position);
        return mFragments[position];
    }

    protected abstract Fragment initItem(int position);

    @Override
    public int getCount() {
        return size;
    }

    @NotNull
    @Override
    public Object instantiateItem(@NotNull ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        mFragments[position] = fragment;
        return fragment;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        if (destroyFragment) super.destroyItem(container, position, object);
        mFragments[position] = null;
    }
}

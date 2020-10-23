package com.stupidtree.hichat.ui.widgets;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bm.library.PhotoView;
import com.bumptech.glide.Glide;
import com.stupidtree.hichat.R;
import com.stupidtree.hichat.ui.base.BaseActivity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;


public class PhotoDetailActivity extends BaseActivity<ViewModel> {

    @BindView(R.id.label)
    TextView label;
    @BindView(R.id.pager)
    ViewPager pager;
    int initIndex;


    List<String> urls;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_photo_detail;
    }

    @Override
    protected Class<ViewModel> getViewModelClass() {
        return null;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void initViews() {
        urls = new ArrayList<>();
        String[] data = getIntent().getStringArrayExtra("urls");
        initIndex = getIntent().getIntExtra("init_index", 0);
        if (data != null) {
            urls.addAll(Arrays.asList(data));
            label.setText((initIndex + 1) + "/" + urls.size());
            initPager();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true, false, true);

    }

    void initPager() {
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onPageSelected(int position) {
                label.setText((position + 1) + "/" + urls.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        pager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return urls.size();
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView((View) object);
            }

            @NotNull
            @Override
            public Object instantiateItem(@NotNull ViewGroup container, int position) {
                PhotoView v = new PhotoView(getThis());
                v.setScaleType(ImageView.ScaleType.FIT_CENTER);
                v.setTransitionName("image");
                Glide.with(getThis()).load(urls.get(position)).timeout(10000).into(v);
                v.setAdjustViewBounds(false);
                v.enable();
                container.addView(v);
                v.setOnClickListener(view -> finish());
//                v.setOnLongClickListener(new View.OnLongClickListener() {
//                    @Override
//                    public boolean onLongClick(View v) {
//                        new AlertDialog.Builder(getThis()).setItems(new String[]{getString(R.string.download_image)}, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                // Toast.makeText(from,imageViewToUrl.get(view)+"",Toast.LENGTH_SHORT).show();
//                                ActivityUtils.DownloadImage(getThis(), urls.get(which), new ActivityUtils.OnDownloadDoneListener() {
//                                    @Override
//                                    public void onDone() {
//                                        try {
//                                            Toast.makeText(getThis(), R.string.save_done, Toast.LENGTH_SHORT).show();
//
//                                        } catch (Exception e) {
//
//                                        }
//                                    }
//                                });
//                            }
//                        }).create().show();
//                        return true;
//                    }
//                });
                return v;
            }
        });
        pager.setCurrentItem(initIndex);
    }


}

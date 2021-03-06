package com.atguigu.ljt.beijingnews.detailpager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.atguigu.ljt.beijingnews.R;
import com.atguigu.ljt.beijingnews.activity.PicassoSampleActivity;
import com.atguigu.ljt.beijingnews.base.MenuDetailBasePager;
import com.atguigu.ljt.beijingnews.bean.NewsCenterBean;
import com.atguigu.ljt.beijingnews.bean.PhotosMenuDetailbean;
import com.atguigu.ljt.beijingnews.util.BitmapCacheUtils;
import com.atguigu.ljt.beijingnews.util.CacheUtils;
import com.atguigu.ljt.beijingnews.util.Constants;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import okhttp3.Call;

/**
 * Created by 李金桐 on 2017/2/6.
 * QQ: 474297694
 * 功能: RecyclerView 实现图组
 */

public class InteractMenuDetailPager extends MenuDetailBasePager {

    private final NewsCenterBean.DataBean bean;
    private final DisplayImageOptions options;
    @InjectView(R.id.recyclerview)
    RecyclerView recyclerview;
    @InjectView(R.id.swiperefreshlayout)
    SwipeRefreshLayout swiperefreshlayout;
    private List<PhotosMenuDetailbean.DataBean.NewsBean> datas;
    private boolean isList = true;
    private BitmapCacheUtils bitmapCacheUtils;

    public InteractMenuDetailPager(Context context, NewsCenterBean.DataBean dataBean) {
        super(context);
        this.bean = dataBean;
        bitmapCacheUtils = new BitmapCacheUtils(mContext);
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.home_scroll_default)
                .showImageForEmptyUri(R.drawable.home_scroll_default)
                .showImageOnFail(R.drawable.home_scroll_default)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new RoundedBitmapDisplayer(10))
                .build();
    }

    @Override
    public View initView() {
        View view = View.inflate(mContext, R.layout.interact_menu_detail_pager, null);
        ButterKnife.inject(this, view);
        swiperefreshlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getDataFromNet(Constants.BASE_URL + bean.getUrl());
            }
        });
        swiperefreshlayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_red_light);
        return view;
    }

    @Override
    public void initData() {
        super.initData();
        getDataFromNet(Constants.BASE_URL + bean.getUrl());
    }

    private void getDataFromNet(final String url) {
        String cache = CacheUtils.getString(mContext, url);
        if (!TextUtils.isEmpty(cache)) {
            processData(cache);
        }
        //OkHttpUtils请求网络
        OkHttpUtils
                .get()
                .url(url)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {

                    }

                    @Override
                    public void onResponse(String response, int id) {
                        processData(response);
                        swiperefreshlayout.setRefreshing(false);
                        CacheUtils.putString(mContext, url, response);
                    }


                });
        //xutil3联网请求
//        RequestParams params = new RequestParams(url);
//        x.http().get(params, new Callback.CommonCallback<String>() {
//            @Override
//            public void onSuccess(String result) {
//                Log.e("TAG", "InteractMenuDetailPager onSuccess()");
//                CacheUtils.putString(mContext, url, result);
//                processData(result);
//                swiperefreshlayout.setRefreshing(false);
//            }
//
//            @Override
//            public void onError(Throwable ex, boolean isOnCallback) {
//                Log.e("TAG", "InteractMenuDetailPager onError()" + ex.getMessage());
//            }
//
//            @Override
//            public void onCancelled(CancelledException cex) {
//
//            }
//
//            @Override
//            public void onFinished() {
//
//            }
//        });
    }

    private void processData(String json) {
        PhotosMenuDetailbean pagerBean = new Gson().fromJson(json, PhotosMenuDetailbean.class);
        datas = pagerBean.getData().getNews();
        MyRectclerViewAdapter adapter = new MyRectclerViewAdapter();
        recyclerview.setAdapter(adapter);
        recyclerview.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
    }

    public void switchListOrGrid(ImageButton list_or_grid) {
        isList = !isList;
        if (isList) {
            list_or_grid.setImageResource(R.drawable.icon_pic_grid_type);
            recyclerview.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        } else {
            list_or_grid.setImageResource(R.drawable.icon_pic_list_type);
            recyclerview.setLayoutManager(new GridLayoutManager(mContext, 2, GridLayoutManager.VERTICAL, false));
        }
    }

    class MyRectclerViewAdapter extends RecyclerView.Adapter<MyRectclerViewAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(View.inflate(mContext, R.layout.photos_menu_pager_item, null));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.tvTitle.setText(datas.get(position).getTitle());
            //Glide请求图片
//            Glide.with(mContext).load(Constants.BASE_URL + datas.get(position).getListimage())
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                    .placeholder(R.drawable.home_scroll_default)
//                    .error(R.drawable.home_scroll_default)
//                    .into(holder.ivIcon);
            //三级缓存请求图片
//            bitmapCacheUtils.setBitmap(Constants.BASE_URL + datas.get(position).getListimage(),holder.ivIcon);
            ImageLoader.getInstance().displayImage(Constants.BASE_URL + datas.get(position).getListimage(), holder.ivIcon, options);
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            @InjectView(R.id.iv_icon)
            ImageView ivIcon;
            @InjectView(R.id.tv_title)
            TextView tvTitle;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.inject(this, itemView);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mContext.startActivity(new Intent(mContext, PicassoSampleActivity.class)
                                .putExtra("url", Constants.BASE_URL + datas.get(getLayoutPosition()).getListimage()));
                    }
                });
            }
        }
    }
}

package com.example.jinphy.mylooklook.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.jinphy.mylooklook.MainActivity;
import com.example.jinphy.mylooklook.R;
import com.example.jinphy.mylooklook.activity.ZhihuDescribeActivity;
import com.example.jinphy.mylooklook.adapter.listener.GlideRequestListenerAdapter;
import com.example.jinphy.mylooklook.bean.zhihu.ZhihuDailyItem;
import com.example.jinphy.mylooklook.config.Config;
import com.example.jinphy.mylooklook.util.AnimUtils;
import com.example.jinphy.mylooklook.util.DBUtils;
import com.example.jinphy.mylooklook.util.DribbbleTarget;
import com.example.jinphy.mylooklook.util.ObservableColorMatrix;
import com.example.jinphy.mylooklook.util.ScreenUtils;
import com.example.jinphy.mylooklook.widget.BadgedFourThreeImageView;

import java.util.ArrayList;


public class ZhihuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements MainActivity.LoadingMore {

    private static final int TYPE_LOADING_MORE = -1;
    private static final int TYPE_NORMAL = 1;

    private boolean loadingMore;
    private int mImageWidth;
    private int mImageHeigh;
    private ArrayList<ZhihuDailyItem> zhihuDailyItems = new ArrayList<>();
    private Context mContext;




    public ZhihuAdapter(Context context) {

        this.mContext = context;
        float width = mContext.getResources().getDimension(R.dimen.image_width);
        mImageWidth = ScreenUtils.dp2px(mContext,width);
        mImageHeigh = mImageWidth * 3 / 4;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        switch (viewType) {
            case TYPE_NORMAL:
                return new ZhihuViewHolder(LayoutInflater.from(mContext).inflate(R.layout.zhihu_layout_item, parent, false));

            case TYPE_LOADING_MORE:
                return new LoadingMoreHolder(LayoutInflater.from(mContext).inflate(R.layout.infinite_loading, parent, false));
            default:
                return null;
        }

    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        int type = getItemViewType(position);
        switch (type) {
            case TYPE_NORMAL:
                bindViewHolderNormal((ZhihuViewHolder) holder, position);
                break;
            case TYPE_LOADING_MORE:
                bindLoadingViewHold((LoadingMoreHolder) holder, position);
                break;
        }


    }

    private void bindLoadingViewHold(LoadingMoreHolder holder, int position) {
        holder.progressBar.setVisibility(loadingMore == true ? View.VISIBLE : View.INVISIBLE);
    }

    private void bindViewHolderNormal(final ZhihuViewHolder holder, final int position) {

        final ZhihuDailyItem item = zhihuDailyItems.get(position);

        // 判断该条新闻是否已经阅读过，从而标记不同的颜色
        if (DBUtils.getDB(mContext).isRead(Config.ZHIHU, item.getId(), 1))
            holder.textView.setTextColor(Color.GRAY);
        else
            holder.textView.setTextColor(Color.BLACK);

        holder.textView.setText(item.getTitle());
        holder.cardView.setOnClickListener(view -> onItemClick(item,holder));

        Glide.with(mContext)
                .load(zhihuDailyItems.get(position).getImages()[0])
                .listener(new GlideRequestListenerAdapter<String, GlideDrawable>() {
                    private int saturationDuration = 1500;
                    private float saturationTo = 1f;
                    private float saturationFrom = 0f;
                    @Override
                    public boolean onResourceReady(
                            GlideDrawable resource,
                            String model,
                            Target<GlideDrawable> target,
                            boolean isFromMemoryCache, boolean isFirstResource) {
                        if (!isFromMemoryCache) {
                            AnimUtils.Builder.create(holder.imageView)
                                    .setSaturation(saturationFrom, saturationTo)
                                    .setDuration(saturationDuration)
                                    .setInterpolator(new AccelerateInterpolator())
                                    .animate();
                        }
                        return false;
                    }
                })
                .override(mImageWidth, mImageHeigh)
                .into(new DribbbleTarget(holder.imageView, false));

    }


    private void onItemClick(ZhihuDailyItem item,ZhihuViewHolder holder){

        // 更新数据库
        DBUtils.getDB(mContext).insertHasRead(Config.ZHIHU, item.getId(), 1);
        // 修改文字颜色
        holder.textView.setTextColor(Color.GRAY);
        // 启动活动
        startDescribeActivity(item,holder);
    }

    private void startDescribeActivity(ZhihuDailyItem item,  ZhihuViewHolder holder) {

        String id = item.getId();
        String title = item.getTitle();

        ZhihuDescribeActivity.startActivity(
                ((Activity) mContext),
                holder.imageView,
                holder.cardView,
                id,title
        );

    }


    @Override
    public int getItemCount() {
        return zhihuDailyItems.size();
    }


    public void addItems(ArrayList<ZhihuDailyItem> list) {

        zhihuDailyItems.addAll(list);
        notifyDataSetChanged();
    }


    @Override
    public int getItemViewType(int position) {
        if (position < getDataItemCount()
                && getDataItemCount() > 0) {

            return TYPE_NORMAL;
        }
        return TYPE_LOADING_MORE;
    }

    private int getDataItemCount() {

        return zhihuDailyItems.size();
    }

    private int getLoadingMoreItemPosition() {
        return loadingMore ? getItemCount() - 1 : RecyclerView.NO_POSITION;
    }

    // TODO: 16/8/13  don't forget call fellow method
    @Override
    public void onStartLoading() {
        if (loadingMore) return;
        loadingMore = true;
        notifyItemInserted(getLoadingMoreItemPosition());
    }

    @Override
    public void onFinishLoading() {
        if (!loadingMore) return;
        final int loadingPos = getLoadingMoreItemPosition();
        loadingMore = false;
        notifyItemRemoved(loadingPos);
    }


    public void clearData() {
        zhihuDailyItems.clear();
        notifyDataSetChanged();
    }

    private static class LoadingMoreHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;

        public LoadingMoreHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView;
        }
    }

    private static class ZhihuViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        CardView cardView;
        BadgedFourThreeImageView imageView;

        ZhihuViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image_id);
            textView = itemView.findViewById(R.id.item_text_id);
            cardView = (CardView) itemView;
        }
    }


}

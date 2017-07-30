package com.example.jinphy.mylooklook.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.target.Target;
import com.example.jinphy.mylooklook.MainActivity;
import com.example.jinphy.mylooklook.R;
import com.example.jinphy.mylooklook.activity.TopNewsDescribeActivity;
import com.example.jinphy.mylooklook.adapter.listener.GlideRequestListenerAdapter;
import com.example.jinphy.mylooklook.bean.news.NewsBean;
import com.example.jinphy.mylooklook.config.Config;
import com.example.jinphy.mylooklook.util.AnimUtils;
import com.example.jinphy.mylooklook.util.DBUtils;
import com.example.jinphy.mylooklook.util.ScreenUtils;
import com.example.jinphy.mylooklook.widget.BadgedFourThreeImageView;

import java.util.ArrayList;


public class TopNewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements MainActivity.LoadingMore {

    private static final int TYPE_LOADING_MORE = -1;
    private static final int TYPE_NORMAL = 1;

    private boolean loadingMore;
    private int mImageViewWidth;
    private int mImageViewHeight;

    private ArrayList<NewsBean> topNewsItems = new ArrayList<>();
    private Context mContext;

    private static Bitmap selectedBitmap = null;

    public TopNewsAdapter(Context context) {
        this.mContext = context;
        float width = mContext.getResources().getDimension(R.dimen.image_width);

        mImageViewWidth = ScreenUtils.dp2px(mContext,width);
        mImageViewHeight = mImageViewWidth * 3 / 4;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        switch (viewType) {
            case TYPE_NORMAL:
                return new TopNewsViewHolder(LayoutInflater.from(mContext).inflate(R.layout.topnews_layout_item, parent, false));

            case TYPE_LOADING_MORE:
                return new LoadingMoreHolder(LayoutInflater.from(mContext).inflate(R.layout.infinite_loading, parent, false));

        }
        return null;

    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        int type = getItemViewType(position);
        switch (type) {
            case TYPE_NORMAL:
                bindViewHolderNormal((TopNewsViewHolder) holder, position);
                break;
            case TYPE_LOADING_MORE:
                bindLoadingViewHold((LoadingMoreHolder) holder, position);
                break;
        }


    }

    private void bindLoadingViewHold(LoadingMoreHolder holder, int position) {
        holder.progressBar.setVisibility(loadingMore ? View.VISIBLE : View.INVISIBLE);
    }

    private void bindViewHolderNormal(final TopNewsViewHolder holder, final int position) {

        final NewsBean item = topNewsItems.get(position);

        if (DBUtils.getDB(mContext).isRead(Config.TOPNEWS, item.getTitle(), 1)) {
            setTextColor(holder,Color.GRAY);
        } else {
            setTextColor(holder,Color.BLACK);
        }

        holder.titleText.setText(item.getTitle());
        holder.sourceText.setText(item.getSource());
        holder.item.setOnClickListener(view -> onItemClick(holder,item));

        Glide.with(mContext)
                .load(item.getImgsrc())
                .listener(new GlideRequestListenerAdapter<String, GlideDrawable>() {
                    private int saturationDuration = 1500;
                    private float saturationTo = 1f;
                    private float saturationFrom = 0f;
                    @Override
                    public boolean onResourceReady(
                            GlideDrawable resource,
                            String model,
                            Target<GlideDrawable> target,
                            boolean isFromMemoryCache,
                            boolean isFirstResource) {

                        if (!isFromMemoryCache) {
                            AnimUtils.Builder.create(holder.photo)
                                    .setSaturation(saturationFrom,saturationTo)
                                    .setDuration(saturationDuration)
                                    .setInterpolator(new AccelerateInterpolator())
                                    .animate();
                        }

                        return false;
                    }
                })
                .override(mImageViewWidth, mImageViewHeight)
                .into(holder.photo);


    }

    private void setSelectedBitmap(ImageView view){
        view.setDrawingCacheEnabled(true);
        selectedBitmap = view.getDrawingCache();
    }

    public static Bitmap getSelectedBitmap() {
        return selectedBitmap;
    }
    public static void removeSelectedBitmap() {
        selectedBitmap = null;
    }

    private void onItemClick(TopNewsViewHolder holder,NewsBean item) {
        // 设置选中的Bitmap
        setSelectedBitmap(holder.photo);

        // 更新数据库
        DBUtils.getDB(mContext).insertHasRead(Config.ZHIHU, item.getTitle(), 1);
        // 修改字体颜色
        setTextColor(holder,Color.GRAY);
        // 启动活动
        startTopNewsActivity(item, holder);
    }

    private void setTextColor(TopNewsViewHolder holder,int color) {
        holder.titleText.setTextColor(color);
        holder.sourceText.setTextColor(color);
    }

    private void startTopNewsActivity(NewsBean item, TopNewsViewHolder holder) {
        TopNewsDescribeActivity.startActivity(
                ((Activity) mContext),
                item,
                holder.item,
                holder.photo
        );

    }

    @Override
    public int getItemCount() {
        return topNewsItems.size();
    }

    public void addItems(ArrayList<NewsBean> list) {
        list.remove(0);
        topNewsItems.addAll(list);
        notifyDataSetChanged();
    }


    @Override
    public int getItemViewType(int position) {
        if (position < topNewsItems.size()
                && topNewsItems.size() > 0) {

            return TYPE_NORMAL;
        }
        return TYPE_LOADING_MORE;
    }

    private int getLoadingMoreItemPosition() {
        return loadingMore ? getItemCount() - 1 : RecyclerView.NO_POSITION;
    }

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
        topNewsItems.clear();
        notifyDataSetChanged();
    }

    private static class LoadingMoreHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;

        public LoadingMoreHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView;
        }
    }

    private static class TopNewsViewHolder extends RecyclerView.ViewHolder {
        CardView item;
        TextView titleText;
        TextView sourceText;
        BadgedFourThreeImageView photo;

        TopNewsViewHolder(View itemView) {
            super(itemView);
            item = ((CardView) itemView);
            photo =  itemView.findViewById(R.id.item_image_id);
            titleText =  itemView.findViewById(R.id.title);
            sourceText =  itemView.findViewById(R.id.source);
        }
    }


}

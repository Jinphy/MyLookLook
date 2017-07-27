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
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
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
import com.example.jinphy.mylooklook.activity.TopNewsDescribeActivity;
import com.example.jinphy.mylooklook.bean.news.NewsBean;
import com.example.jinphy.mylooklook.config.Config;
import com.example.jinphy.mylooklook.util.DBUtils;
import com.example.jinphy.mylooklook.util.DribbbleTarget;
import com.example.jinphy.mylooklook.util.Help;
import com.example.jinphy.mylooklook.util.ObservableColorMatrix;
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

        final NewsBean newsBeanItem = topNewsItems.get(holder.getAdapterPosition());

        if (DBUtils.getDB(mContext).isRead(Config.TOPNEWS, newsBeanItem.getTitle(), 1)) {

            holder.textView.setTextColor(Color.GRAY);
            holder.sourceTextview.setTextColor(Color.GRAY);
        } else {
            holder.textView.setTextColor(Color.BLACK);
            holder.sourceTextview.setTextColor(Color.BLACK);
        }


        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DBUtils.getDB(mContext).insertHasRead(Config.ZHIHU, newsBeanItem.getTitle(), 1);
                holder.textView.setTextColor(Color.GRAY);
                holder.sourceTextview.setTextColor(Color.GRAY);
                startTopNewsActivity(newsBeanItem, holder);

            }
        });
        holder.textView.setText(newsBeanItem.getTitle());
        holder.sourceTextview.setText(newsBeanItem.getSource());
        holder.linearLayout.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startTopNewsActivity(newsBeanItem, holder);
                    }
                });

        Glide.with(mContext)
                .load(newsBeanItem.getImgsrc())
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        if (!newsBeanItem.hasFadedIn) {
                            holder.imageView.setHasTransientState(true);
                            final ObservableColorMatrix cm = new ObservableColorMatrix();
                            final ObjectAnimator animator = ObjectAnimator.ofFloat(cm, ObservableColorMatrix.SATURATION, 0f, 1f);
                            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                    holder.imageView.setColorFilter(new ColorMatrixColorFilter(cm));
                                }
                            });
                            animator.setDuration(2000L);
                            animator.setInterpolator(new AccelerateInterpolator());
                            animator.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    holder.imageView.clearColorFilter();
                                    holder.imageView.setHasTransientState(false);
                                    animator.start();
                                    newsBeanItem.hasFadedIn = true;

                                }
                            });
                        }

                        return false;
                    }
                }).diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .centerCrop().override(mImageViewWidth, mImageViewHeight)
                .into(new DribbbleTarget(holder.imageView, false));


    }

    private void startTopNewsActivity(NewsBean newsBeanItem, RecyclerView.ViewHolder holder) {

        Intent intent = new Intent(mContext, TopNewsDescribeActivity.class);
        intent.putExtra("docid", newsBeanItem.getDocid());
        intent.putExtra("title", newsBeanItem.getTitle());
        intent.putExtra("image", newsBeanItem.getImgsrc());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final android.support.v4.util.Pair<View, String>[] pairs = Help.createSafeTransitionParticipants
                    ((Activity) mContext, false, new android.support.v4.util.Pair<>(((TopNewsViewHolder) holder).imageView, mContext.getString(R.string.transition_topnew)),
                            new android.support.v4.util.Pair<>(((TopNewsViewHolder) holder).linearLayout, mContext.getString(R.string.transition_topnew_linear)));
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, pairs);
            mContext.startActivity(intent, options.toBundle());
        } else {

            mContext.startActivity(intent);

        }

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
        if (position < getDataItemCount()
                && getDataItemCount() > 0) {

            return TYPE_NORMAL;
        }
        return TYPE_LOADING_MORE;
    }

    private int getDataItemCount() {
        return topNewsItems.size();
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
        TextView textView;
        LinearLayout linearLayout;
        TextView sourceTextview;
        BadgedFourThreeImageView imageView;

        TopNewsViewHolder(View itemView) {
            super(itemView);
            imageView = (BadgedFourThreeImageView) itemView.findViewById(R.id.item_image_id);
            textView = (TextView) itemView.findViewById(R.id.item_text_id);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.zhihu_item_layout);
            sourceTextview = (TextView) itemView.findViewById(R.id.item_text_source_id);
        }
    }


}

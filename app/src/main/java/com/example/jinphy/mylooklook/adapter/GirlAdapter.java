package com.example.jinphy.mylooklook.adapter;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.target.Target;
import com.example.jinphy.mylooklook.MainActivity;
import com.example.jinphy.mylooklook.R;
import com.example.jinphy.mylooklook.activity.MeiziPhotoDescribeActivity;
import com.example.jinphy.mylooklook.adapter.listener.GlideRequestListenerAdapter;
import com.example.jinphy.mylooklook.bean.meizi.Gank;
import com.example.jinphy.mylooklook.bean.meizi.Meizi;
import com.example.jinphy.mylooklook.util.AnimUtils;
import com.example.jinphy.mylooklook.util.FileUtils;
import com.example.jinphy.mylooklook.util.PermissionUtils;
import com.example.jinphy.mylooklook.widget.BadgedFourThreeImageView;

import java.io.File;
import java.util.ArrayList;


public class GirlAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements MainActivity.LoadingMore {

    private static final int TYPE_LOADING_MORE = -1;
    private static final int TYPE_NOMAL = 1;
    private boolean loadingMore;

    private ArrayList<Meizi> meiziItems = new ArrayList<>();
    private Context mContext;
    private static Bitmap selectedBitmap;

    public GirlAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public  RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        switch (viewType) {
            case TYPE_NOMAL:
                return new GirlViewHolder(LayoutInflater.from(mContext).inflate(R.layout.meizi_layout_item, parent, false));

            case TYPE_LOADING_MORE:
                return new LoadingMoreHolder(LayoutInflater.from(mContext).inflate(R.layout.infinite_loading, parent, false));

        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {


        int type = getItemViewType(position);
        switch (type) {
            case TYPE_NOMAL:
                bindViewHolderNormal((GirlViewHolder) holder, position);
                break;
            case TYPE_LOADING_MORE:
                bindLoadingViewHold((LoadingMoreHolder) holder, position);
                break;
        }


    }

    private void bindLoadingViewHold(LoadingMoreHolder holder, int position) {
        holder.progressBar.setVisibility(loadingMore ? View.VISIBLE : View.INVISIBLE);
    }

    private void bindViewHolderNormal(final GirlViewHolder holder, final int position) {

//        final Meizi meizi = meiziItems.get(holder.getAdapterPosition());
        final Meizi meizi = meiziItems.get(position);

        holder.imageView.setOnClickListener(view ->
                startDescribeActivity(meizi,(ImageView)view));
        holder.imageView.setOnLongClickListener(this::onItemLongClick);
        Glide.with(mContext)
                .load(meizi.getUrl())
                .listener(new GlideRequestListenerAdapter<String,GlideDrawable>(){
                    private int saturationDuration = 1500;
                    private float saturationTo = 1f;
                    private float saturationFrom = 0f;

                    @Override
                    public boolean onResourceReady(
                            GlideDrawable resource,
                            String model, Target<GlideDrawable> target,
                            boolean isFromMemoryCache, boolean isFirstResource) {
                        if (!isFromMemoryCache) {
                            new AnimUtils.Builder(holder.imageView)
                                    .setSaturation(saturationFrom, saturationTo)
                                    .setDuration(saturationDuration)
                                    .setInterpolator(new AccelerateInterpolator())
                                    .animate();
                            //

                        }
                        return false;
                    }
                })
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
//                .centerCrop()
                .into(holder.imageView);


    }

    private void startDescribeActivity(Meizi meizi,ImageView imageView){

        int location[] = new int[2];
        imageView.getLocationOnScreen(location);
        int left =location[0];
        int top = location[1];
        int width = imageView.getWidth();
        int height = imageView.getHeight();

        imageView.setDrawingCacheEnabled(true);
        selectedBitmap = imageView.getDrawingCache(true);

        MeiziPhotoDescribeActivity.startActivity(
                ((Activity) mContext), imageView,
                left,top,
                width,height);

    }


    /**
     * 通过该方法获取当前被点击的图片的bitmap
     *
     *
     * */
    public static Bitmap getSelectedBitmap() {
        return selectedBitmap;
    }


    @Override
    public int getItemCount() {
        return meiziItems.size();
    }

    public void addItems(ArrayList<Meizi> list) {
        meiziItems.addAll(list);
        notifyDataSetChanged();
    }


    @Override
    public int getItemViewType(int position) {
        if (position < meiziItems.size() && meiziItems.size() > 0) {
            return TYPE_NOMAL;
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

    public void addVideoData(ArrayList<Gank> list){

    }

    public void clearData() {
        meiziItems.clear();
        notifyDataSetChanged();
    }

    public static class LoadingMoreHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;
        public LoadingMoreHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView;
        }
    }

    private static class GirlViewHolder extends RecyclerView.ViewHolder {
        BadgedFourThreeImageView imageView;
        GirlViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image_id);

        }
    }

    private boolean onItemLongClick(View view){
        new AlertDialog.Builder(mContext)
                            .setMessage(mContext.getString(R.string.save_meizi))
                            .setNegativeButton(
                                    android.R.string.cancel, (anInterface, i) -> anInterface.dismiss())
                            .setPositiveButton(android.R.string.ok, (anInterface, i) -> {
                                anInterface.dismiss();
                                saveImage(((ImageView) view));
                            }).show();

        return true;
    }


    // 保存图片到文件中
    private void saveImage(ImageView imageView) {
        // 危险权限需要动态申请权限
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        if (PermissionUtils.has(((Activity) mContext), permission)) {
            // 有读写文件的权限
            String path = Environment.getExternalStorageDirectory() + "/MyLookLook/image/";
            File file = FileUtils.createImageFile(path, FileUtils.PREFIX_IMAGE, ".jpg");

            boolean successful = FileUtils.saveImage(mContext,imageView,file,true);

            if (successful) {
                Snackbar.make(imageView, "图片已保存", Snackbar.LENGTH_SHORT).show();
            }else{
                Snackbar.make(imageView, "阿偶出错了呢", Snackbar.LENGTH_SHORT).show();
            }

        } else {
            // 用户拒绝了读写文件的权限
            Snackbar.make(imageView, "您已拒绝读写文件的权限，保存失败！", Snackbar.LENGTH_SHORT).show();
        }
    }




}

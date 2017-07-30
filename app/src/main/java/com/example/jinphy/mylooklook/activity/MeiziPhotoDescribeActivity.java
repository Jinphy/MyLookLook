package com.example.jinphy.mylooklook.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.Toolbar;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.jinphy.mylooklook.MyApplication;
import com.example.jinphy.mylooklook.R;
import com.example.jinphy.mylooklook.adapter.GirlAdapter;
import com.example.jinphy.mylooklook.adapter.listener.TransitionListenerAdapter;
import com.example.jinphy.mylooklook.util.AnimUtils;
import com.example.jinphy.mylooklook.util.Help;
import com.example.jinphy.mylooklook.util.ScreenUtils;
import com.wingsofts.dragphotoview.DragPhotoView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by xinghongfei on 16/8/13.
 */
public class MeiziPhotoDescribeActivity extends BaseActivity {
    private static final float SCRIM_ADJUSTMENT = 0.075f;

    public static final String LEFT = "left";
    public static final String TOP = "right";
    public static final String WIDTH = "width";
    public static final String HEIGHT = "height";
    public static final String URL = "url";


    private String mImageUrl;
    @BindView(R.id.shot)
    DragPhotoView mDragPhotoView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.background)
    RelativeLayout mRelativeLayout;

    private int mOriginLeft;
    private int mOriginTop;
    private int mOriginHeight;
    private int mOriginWidth;
    private int mOriginCenterX;
    private int mOriginCenterY;
    private boolean mIsHidden = false;
    private float mTargetHeight;
    private float mTargetWidth;
    private float mScaleX;
    private float mScaleY;
    private float mTranslationX;
    private float mTranslationY;
    private int screenWidth = ScreenUtils.getScreenWidth(MyApplication.getContext());
    private int screenHeiht = ScreenUtils.getScreenHeight(MyApplication.getContext());
    private float density = ScreenUtils.getDensity(MyApplication.getContext());
    private Transition.TransitionListener mListener = new TransitionListenerAdapter() {
        @Override
        public void onTransitionStart(Transition transition) {
            //                 mRelativeLayout.animate()
            //                .alpha(1f)
            //                .setDuration(1000L)
            //                .setInterpolator(new AccelerateInterpolator())
            //                .start();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        ButterKnife.bind(this);

        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        mToolbar.setNavigationOnClickListener(view -> finishWithAnimation());
        mToolbar.setAlpha(0.3f);

        parseIntent(getIntent());
        mDragPhotoView.setImageBitmap(GirlAdapter.getSelectedBitmap());
        initialPhotoAttacher();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getSharedElementEnterTransition().addListener(mListener);
            getWindow().setSharedElementEnterTransition(new ChangeBounds());
            //            setStatusColor();
        }
    }

    // 解析传递过来的Intent
    private void parseIntent(Intent intent) {
        mImageUrl = intent.getStringExtra(URL);
        mOriginLeft = intent.getIntExtra(LEFT, 0);
        mOriginTop = intent.getIntExtra(TOP, 0);
        mOriginWidth = intent.getIntExtra(WIDTH, 0);
        mOriginHeight = intent.getIntExtra(HEIGHT, 0);

        mOriginCenterX = mOriginLeft + mOriginWidth / 2;
        mOriginCenterY = mOriginTop + mOriginHeight / 2;
    }


    /**
     * 启动当前的活动的函数
     *
     * @param activity    启动端的activity
     * @param imageView   启动端界面中的imageView
     * @param imageLeft   imageView的左边坐标
     * @param imageTop    imageView的上边坐标
     * @param imageWidth  imageView的宽度
     * @param imageHeight imageView的高度
     */
    public static void startActivity(
            Activity activity, ImageView imageView,
            int imageLeft, int imageTop,
            int imageWidth, int imageHeight) {

        Intent intent = new Intent(activity, MeiziPhotoDescribeActivity.class);
        intent.putExtra(LEFT, imageLeft)
                .putExtra(TOP, imageTop)
                .putExtra(WIDTH, imageWidth)
                .putExtra(HEIGHT, imageHeight);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final Pair<View, String>[] pairs = Help.createSafeTransitionParticipants(
                    activity, false, new Pair<>(imageView, activity.getString(R.string.meizi)));
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation
                    (activity, pairs);
            activity.startActivity(intent, options.toBundle());

        } else {
            activity.startActivity(intent);
        }
    }


    private boolean isFirst = true;

    // 监听器的回调函数,测试后发现在方法回调了三次
    private void onGlobalLayout() {


        if (isFirst) {
            mTargetWidth = screenWidth;
            mTargetHeight = screenHeiht;
            mScaleX = (float) mOriginWidth / screenWidth;
            mScaleY = (float) mOriginHeight / screenHeiht;
            mDragPhotoView.setScaleX(mScaleX);
            mDragPhotoView.setScaleY(mScaleY);

            float targetCenterX = screenWidth / 2;
            float targetCenterY = screenHeiht / 2;
            mTranslationX = mOriginCenterX - targetCenterX;
            mTranslationY = mOriginCenterY - targetCenterY;
            mDragPhotoView.setTranslationX(mTranslationX);
            mDragPhotoView.setTranslationY(mTranslationY);

            mDragPhotoView.setMinScale(mScaleX);
            performEnterAnimation();
            isFirst = false;
        }


    }

    private void initialPhotoAttacher() {
        mDragPhotoView.setOnTapListener(view -> finishWithAnimation());
        mDragPhotoView.setOnExitListener(this::performExitAnimation);

        // 在onCreate方法中无法获取View的高度，因为还没测量，所以要监听在回调中获取
        mRelativeLayout.getViewTreeObserver()
                .addOnGlobalLayoutListener(this::onGlobalLayout);

        mDragPhotoView.setOnViewTapListener((view, x, y) -> hideOrShowToolbar());

    }

    @Override
    protected void onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getSharedElementEnterTransition().removeListener(mListener);
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finishWithAnimation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRelativeLayout.animate()
                .alpha(1f)
                .setDuration(1000L)
                .setInterpolator(new AccelerateInterpolator())
                .start();
    }

    protected void hideOrShowToolbar() {
        mToolbar.animate()
                .translationY(mIsHidden ? 0 : -mToolbar.getHeight())
                .setInterpolator(new DecelerateInterpolator(2))
                .start();
        mIsHidden = !mIsHidden;
    }


    // 当按下返回键或者toolbar上的退出箭头时调用该函数，
    // 启动动画并在动画完成后退出当前activity
    private void finishWithAnimation() {
        AnimUtils.Builder.create(mDragPhotoView)
                .setX(0, mTranslationX)
                .setY(0, mTranslationY)
                .setScaleX(1, mScaleX)
                .setScaleY(1, mScaleY)
                .setDuration(300)
                .onEnd(animator -> {
                    animator.removeAllListeners();
                    finish();
                    overridePendingTransition(0, 0);
                })
                .animate();

    }

    // 拖拽图片到超过一定距离，出发退出当前Activity是调用该方法执行退出动画
    private void performExitAnimation(final DragPhotoView view, float x, float y, float w, float
            h) {
        view.finishAnimationCallBack();

        float sx = mOriginWidth / w;
        float sy = mOriginHeight / h;

        AnimUtils.Builder.create(view)
                .setX(x, mTranslationX)
                .setY(y, mTranslationY)
                .setScaleX(1, sx)
                .setScaleY(1, sy)
                .setDuration(300)
                .onEnd(animator -> {
                    animator.removeAllListeners();
                    finish();
                    overridePendingTransition(0, 0);
                })
                .animate();

    }


    // 启动进入动画
    private void performEnterAnimation() {
        mToolbar.animate()
                .alpha(1f)
                .setDuration(1000L)
                .setInterpolator(new AccelerateInterpolator())
                .start();

        AnimUtils.Builder.create(mDragPhotoView)
                .setX(mDragPhotoView.getX(), 0)
                .setY(mDragPhotoView.getY(), 0)
                .setScaleX(mScaleX, 1)
                .setScaleY(mScaleY, 1)
                .setDuration(300)
                .animate();
    }


}




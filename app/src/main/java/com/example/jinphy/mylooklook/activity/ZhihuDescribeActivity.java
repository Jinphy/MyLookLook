package com.example.jinphy.mylooklook.activity;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.jinphy.mylooklook.R;
import com.example.jinphy.mylooklook.adapter.ZhihuAdapter;
import com.example.jinphy.mylooklook.adapter.listener.TransitionListenerAdapter;
import com.example.jinphy.mylooklook.bean.zhihu.ZhihuStory;
import com.example.jinphy.mylooklook.presenter.IZhihuStoryPresenter;
import com.example.jinphy.mylooklook.presenter.implPresenter.ZhihuStoryPresenterImpl;
import com.example.jinphy.mylooklook.presenter.implView.IZhihuStory;
import com.example.jinphy.mylooklook.util.AnimUtils;
import com.example.jinphy.mylooklook.util.ColorUtils;
import com.example.jinphy.mylooklook.util.Help;
import com.example.jinphy.mylooklook.util.ScreenUtils;
import com.example.jinphy.mylooklook.widget.ElasticDragDismissFrameLayout;
import com.example.jinphy.mylooklook.widget.ParallaxScrimageView;
import com.example.jinphy.mylooklook.widget.TranslateYTextView;
import com.liuguangqiang.swipeback.SwipeBackActivity;
import com.liuguangqiang.swipeback.SwipeBackLayout;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by xinghongfei on 16/8/13.
 */
public class ZhihuDescribeActivity extends SwipeBackActivity implements IZhihuStory {
    private static final float SCRIM_ADJUSTMENT = 0.075f;

    public static final String ID = "id";
    public static final String TITLE = "title";


    @BindView(R.id.shot)
    ParallaxScrimageView mShot;
    @Nullable
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.wv_zhihu)
    WebView wvZhihu;
    @BindView(R.id.nest)
    NestedScrollView mNest;
    @BindView(R.id.title)
    TranslateYTextView titleText;

    String mBody;


    int[] mDeviceInfo;
    int width;
    int heigh;
    private Transition.TransitionListener zhihuReturnHomeListener;

    private String id;
    private String title;
    private String url;
    private IZhihuStoryPresenter mIZhihuStoryPresenter;
    private ElasticDragDismissFrameLayout.SystemChromeFader chromeFader;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zhihudescribe);
        setDragEdge(SwipeBackLayout.DragEdge.LEFT);
        ButterKnife.bind(this);

        mDeviceInfo = ScreenUtils.getDeviceInfo(this);
        width = mDeviceInfo[0];
        heigh = width * 3 / 4;
        setSupportActionBar(mToolbar);
        initlistenr();
        initData();
        initView();
        getData();

        chromeFader = new ElasticDragDismissFrameLayout.SystemChromeFader(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            getWindow().getSharedElementReturnTransition().addListener(zhihuReturnHomeListener);
            getWindow().setSharedElementEnterTransition(new ChangeBounds());
        }

    }

    private void initlistenr() {
        zhihuReturnHomeListener =
                new TransitionListenerAdapter() {
                    @Override
                    public void onTransitionStart(Transition transition) {
                        super.onTransitionStart(transition);
                        AnimUtils.Builder.create()
                                .setFloat(1f, 0f)
                                .setDuration(100)
                                .setInterpolator(new AccelerateInterpolator())
                                .onUpdateFloat(animator ->
                                        setAlpha(animator, mToolbar, titleText, mNest))
                                .animate();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            mShot.setElevation(1f);
                            mToolbar.setElevation(0f);
                        }
                    }
                };
    }

    protected void initData() {
        id = getIntent().getStringExtra(ID);
        title = getIntent().getStringExtra(TITLE);
        mIZhihuStoryPresenter = new ZhihuStoryPresenterImpl(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition();
            mShot.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver
                    .OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mShot.getViewTreeObserver().removeOnPreDrawListener(this);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        startPostponedEnterTransition();
                    }
                    return true;
                }
            });
        }


    }

    private void initView() {
        mToolbar.setTitleMargin(20, 20, 0, 10);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back);

        mToolbar.setNavigationOnClickListener(v -> expandImageAndFinish());
        titleText.setText(title);

        WebSettings settings = wvZhihu.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(true);
        //settings.setUseWideViewPort(true);造成文字太小
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAppCachePath(getCacheDir().getAbsolutePath() + "/webViewCache");
        settings.setAppCacheEnabled(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        wvZhihu.setWebChromeClient(new WebChromeClient());



    }


    @Override
    protected void onResume() {
        super.onResume();
        try {
            wvZhihu.getClass().getMethod("onResume").invoke(wvZhihu, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            wvZhihu.getClass().getMethod("onPause").invoke(wvZhihu, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        ZhihuAdapter.removeSelectedBitmap();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getSharedElementReturnTransition().removeListener(zhihuReturnHomeListener);
        }
        //webView内存泄露
        if (wvZhihu != null) {
            ((ViewGroup) wvZhihu.getParent()).removeView(wvZhihu);
            wvZhihu.destroy();
            wvZhihu = null;
        }
        mIZhihuStoryPresenter.unsubscrible();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        expandImageAndFinish();

    }

    private void getData() {
        mIZhihuStoryPresenter.getZhihuStory(id);

    }

    @Override
    public void showError(String error) {
        Snackbar.make(wvZhihu, getString(R.string.snack_infor), Snackbar.LENGTH_INDEFINITE)
                .setAction("重试", v -> getData())
                .show();
    }

    @Override
    public void showZhihuStory(ZhihuStory zhihuStory) {
        Glide.with(this)
                .load(zhihuStory.getImage()).centerCrop()
                .override(width, heigh)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(mShot);

        url = zhihuStory.getShareUrl();
        mBody = zhihuStory.getBody();
        wvZhihu.loadUrl(url);


        Bitmap bitmap = ZhihuAdapter.getSelectedBitmap();

        Palette.Swatch swatch = Palette.from(bitmap).generate().getDarkVibrantSwatch();
        int statusColor = ColorUtils.changeLightNess(Color.BLACK,0);
        if (swatch!=null){
            statusColor = swatch.getRgb();
        }
        ScreenUtils.setStatusBarColor(this,statusColor);


    }


    private void expandImageAndFinish() {
        if (mShot.getOffset() != 0f) {
            AnimUtils.Builder.create()
                    .setFloat(0f)
                    .setDuration(80)
                    .setInterpolator(new AccelerateInterpolator())
                    .onUpdateFloat(animator -> mShot.setOffset((float)animator.getAnimatedValue()))
                    .onEnd(this::finish)
                    .animate();

        } else {
            finish(null);
        }
    }

    private void finish(Animator animator) {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
            finishAfterTransition();
        } else {
            finish();
        }
    }




    public static void startActivity(
            Activity activity,
            CardView itemView,
            ImageView photo,
            String id, String title) {
        Intent intent = new Intent(activity, ZhihuDescribeActivity.class);
        intent.putExtra(ID, id).putExtra(TITLE, title);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final Pair<View, String>[] pairs = Help.createSafeTransitionParticipants(
                    activity,
                    false,
                    new Pair<>(photo, activity.getString(R.string.transition_shot)),
                    new Pair<>(itemView, activity.getString(R.string.transition_shot_background)));
            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(activity, pairs);
            activity.startActivity(intent, options.toBundle());
        } else {
            activity.startActivity(intent);
        }

    }


    private void setAlpha(ValueAnimator animator, View... views) {
        float alpha = (float) animator.getAnimatedValue();
        for (View view : views) {
            view.setAlpha(alpha);
        }
    }
}

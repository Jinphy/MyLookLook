package com.example.jinphy.mylooklook.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.jinphy.mylooklook.MyApplication;
import com.example.jinphy.mylooklook.R;
import com.example.jinphy.mylooklook.adapter.TopNewsAdapter;
import com.example.jinphy.mylooklook.adapter.listener.GlideRequestListenerAdapter;
import com.example.jinphy.mylooklook.adapter.listener.TransitionListenerAdapter;
import com.example.jinphy.mylooklook.bean.news.NewsBean;
import com.example.jinphy.mylooklook.bean.news.NewsDetailBean;
import com.example.jinphy.mylooklook.presenter.implPresenter.TopNewsDesPresenterImpl;
import com.example.jinphy.mylooklook.presenter.implView.ITopNewsDesFragment;
import com.example.jinphy.mylooklook.util.AnimUtils;
import com.example.jinphy.mylooklook.util.ColorUtils;
import com.example.jinphy.mylooklook.util.FileUtils;
import com.example.jinphy.mylooklook.util.GlideUtils;
import com.example.jinphy.mylooklook.util.Help;
import com.example.jinphy.mylooklook.util.MathUtils;
import com.example.jinphy.mylooklook.util.ScreenUtils;
import com.example.jinphy.mylooklook.util.ViewUtils;
import com.example.jinphy.mylooklook.widget.ElasticDragDismissFrameLayout;
import com.example.jinphy.mylooklook.widget.ParallaxScrimageView;
import com.example.jinphy.mylooklook.widget.TranslateYTextView;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by xinghongfei on 16/8/13.
 */
public class TopNewsDescribeActivity extends BaseActivity implements ITopNewsDesFragment {
    public static final String ID = "ID";
    public static final String TITLE = "TITLE";
    int screenWidth = ScreenUtils.getScreenWidth(MyApplication.getContext());
    int screenHeight = ScreenUtils.getScreenHeight(MyApplication.getContext());
    float density = ScreenUtils.getDensity(MyApplication.getContext());

    @BindView(R.id.progress)
    ProgressBar mProgress;
    @BindView(R.id.htNewsContent)
    HtmlTextView mHtNewsContent;
    @BindView(R.id.shot)
    ParallaxScrimageView mShot;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.draggable_frame)
    ElasticDragDismissFrameLayout mDraggableFrame;
    @BindView(R.id.nest)
    NestedScrollView mNest;
    @BindView(R.id.title)
    TranslateYTextView mTextView;

    private String id;
    private String title;
    private ElasticDragDismissFrameLayout.SystemChromeFader chromeFader;
    private TopNewsDesPresenterImpl mTopNewsDesPresenter;
    private Transition.TransitionListener mReturnHomeListener;
    private Transition.TransitionListener mEnterTrasitionListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.topnews_describe);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        initData();
        initView();
        getData();
        enterAnimation();

        chromeFader = new ElasticDragDismissFrameLayout.SystemChromeFader(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getSharedElementReturnTransition().addListener(mReturnHomeListener);
            getWindow().getSharedElementEnterTransition().addListener(mEnterTrasitionListener);
        }

    }



    protected void initData() {
        id = getIntent().getStringExtra(ID);
        title = getIntent().getStringExtra(TITLE);
        mTextView.setText(title);

        Bitmap bitmap = TopNewsAdapter.getSelectedBitmap();
        mShot.setImageBitmap(bitmap);

        Palette.Swatch swatch = Palette.from(bitmap).generate().getDarkVibrantSwatch();
        int statusColor = ColorUtils.changeLightNess(Color.BLACK,0);
        int toolbarColor = ColorUtils.changeLightNess(statusColor,1.3f);
        if (swatch!=null){
            statusColor = swatch.getRgb();
            toolbarColor = ColorUtils.changeLightNess(statusColor,1.3f);
        }
        ScreenUtils.setStatusBarColor(this,statusColor);
        mToolbar.setBackgroundColor(toolbarColor);

        mNest.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int
                    oldScrollX, int oldScrollY) {
                int maxScroll = mToolbar.getHeight()+mShot.getHeight();
                int transY= 0;
                if (scrollY > maxScroll) {
                    transY = -mToolbar.getHeight();
                }
                if (Math.abs(transY - mToolbar.getTranslationY()) < 5) {
                    return;
                }
                AnimUtils.Builder.create()
                        .setInt(transY)
                        .onUpdateInt(animator -> {
                            int trans = ((int) animator.getAnimatedValue());
                            mToolbar.setTranslationY(trans);
                            mShot.setTranslationY(trans);
                        })
                        .setInterpolator(new DecelerateInterpolator())
                        .animate();
            }
        });

        mTopNewsDesPresenter = new TopNewsDesPresenterImpl(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition();
            mShot.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver
                    .OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mShot.getViewTreeObserver().removeOnPreDrawListener(this);
                    TopNewsDescribeActivity.this.startPostponedEnterTransition();
                    return true;
                }
            });
        }
        mReturnHomeListener = new TransitionListenerAdapter() {
                    @Override
                    public void onTransitionStart(Transition transition) {
                        super.onTransitionStart(transition);
                        AnimUtils.Builder.create()
                                .setFloat(1f,0f)
                                .setDuration(100)
                                .setInterpolator(new AccelerateInterpolator())
                                .onUpdateFloat(animator -> setAlpha(animator,mToolbar,mTextView,mNest))
                                .animate();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            mShot.setElevation(1f);
                            mToolbar.setElevation(0f);
                        }
                    }
                };
        mEnterTrasitionListener = new TransitionListenerAdapter() {
                    @Override
                    public void onTransitionEnd(Transition transition) {
                        super.onTransitionEnd(transition);
                        //                    解决5.0 shara element bug

                        AnimUtils.Builder.create()
                                .setInt(0,100)
                                .setDuration(100)
                                .onUpdateInt(animator ->
                                        mNest.smoothScrollTo((int) animator.getAnimatedValue()/10,0))
                                .animate();

                    }

                };


    }

    private void initView() {
        mNest.setAlpha(0.5f);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        mToolbar.setOnClickListener(view -> mNest.smoothScrollTo(0, 0));
        mToolbar.setNavigationOnClickListener(v -> expandImageAndFinish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDraggableFrame.addListener(chromeFader);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mDraggableFrame.removeListener(chromeFader);

    }

    @Override
    protected void onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getSharedElementReturnTransition().removeListener(mReturnHomeListener);
            getWindow().getSharedElementEnterTransition().removeListener(mEnterTrasitionListener);

        }
        mTopNewsDesPresenter.unsubscrible();
        super.onDestroy();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        expandImageAndFinish();

    }

    private void getData() {
        mTopNewsDesPresenter.getDescribleMessage(id);

    }

    @OnClick(R.id.shot)
    public void onClick() {
        mNest.smoothScrollTo(0, 0);

    }

    @Override
    public void showProgressDialog() {
        mProgress.setVisibility(View.VISIBLE);
    }

    @Override
    public void hidProgressDialog() {
        mProgress.setVisibility(View.GONE);

    }

    @Override
    public void showError(String error) {
        Snackbar.make(mDraggableFrame, getString(R.string.snack_infor), Snackbar
                .LENGTH_INDEFINITE).setAction("重试", v -> getData()).show();
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

    private void enterAnimation() {

        AnimUtils.Builder.create(mToolbar)
                .setTranY(-200*density,0)
                .setFloat(0f,1f)
                .onUpdateFloat(animator -> setAlpha(animator,mToolbar,mNest))
                .setDuration(400)
                .setInterpolator(new LinearInterpolator())
                .animate();
    }


    private void setAlpha(ValueAnimator animator, View... views) {
        float alpha = (float) animator.getAnimatedValue();
        for (View view : views) {
            view.setAlpha(alpha);
        }
    }

    @Override
    public void updateListItem(NewsDetailBean newsList) {
        mProgress.setVisibility(View.GONE);
        mHtNewsContent.setHtmlFromString(newsList==null?"数据获取失败":newsList.getBody(), new HtmlTextView.LocalImageGetter());
    }

    public static void startActivity(
            Activity activity,
            NewsBean item,
            View itemView,
            ImageView photoView) {

        Intent intent = new Intent(activity, TopNewsDescribeActivity.class);
        intent.putExtra(ID, item.getDocid());
        intent.putExtra(TITLE, item.getTitle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final Pair<View, String>[] pairs = Help.createSafeTransitionParticipants(
                    activity,
                    false,
                    new Pair<>(photoView, activity.getString(R.string.transition_topnew)),
                    new Pair<>(itemView, activity.getString(R.string.transition_topnew_linear)));
            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(activity, pairs);
            activity.startActivity(intent, options.toBundle());
        } else {
            activity.startActivity(intent);

        }
    }
}

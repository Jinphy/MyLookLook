package com.example.jinphy.mylooklook;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.util.SimpleArrayMap;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jinphy.mylooklook.activity.AboutActivity;
import com.example.jinphy.mylooklook.activity.BaseActivity;
import com.example.jinphy.mylooklook.fragment.MeiziFragment;
import com.example.jinphy.mylooklook.fragment.TopNewsFragment;
import com.example.jinphy.mylooklook.fragment.ZhihuFragment;
import com.example.jinphy.mylooklook.presenter.implPresenter.MainPresenterImpl;
import com.example.jinphy.mylooklook.presenter.implView.IMain;
import com.example.jinphy.mylooklook.util.AnimUtils;
import com.example.jinphy.mylooklook.util.SharePreferenceUtil;
import com.example.jinphy.mylooklook.util.ViewUtils;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity implements IMain {

    @BindView(R.id.fragment_container)
    FrameLayout mFragmentContainer;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.nav_view)
    NavigationView navView;

    @BindView(R.id.drawer)
    DrawerLayout drawer;


    private SimpleArrayMap<Integer, String> fragmentIdAndTitleMap = new SimpleArrayMap<>();

    private long exitTime = 0;
    private SwitchCompat mThemeSwitch;
    private MenuItem currentMenuItem;
    private Fragment currentFragment;
    private int mainColor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        // 依赖注入
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        animateTitle();
        toolbar.setOnMenuItemClickListener(this::onMenuItemClick);

        MainPresenterImpl IMainPresenter = new MainPresenterImpl(this, this);
        IMainPresenter.getBackground();

        mapIdsWithTitlesOfFragment();

        drawer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        // 初始化设置fragment
        initFragment(savedInstanceState);


        navView.setNavigationItemSelectedListener(this::onItemSelected);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawer.setOnApplyWindowInsetsListener(this::onApplyWindowInsets);
        }

        int[][] state = new int[][]{
                new int[]{-android.R.attr.state_checked}, // unchecked
                new int[]{android.R.attr.state_checked}  // pressed
        };

        int[] color = new int[]{
                Color.BLACK, Color.GREEN};
        int[] iconColor = new int[]{
                Color.GRAY, Color.GREEN};
        navView.setItemTextColor(new ColorStateList(state, color));
        navView.setItemIconTintList(new ColorStateList(state, iconColor));

        //主题变色
        MenuItem item = navView.getMenu().findItem(R.id.nav_theme);
        mThemeSwitch = (SwitchCompat) item.getActionView();
        mThemeSwitch.setOnCheckedChangeListener(this::onCheckedChanged);

    }

    // 菜单中的主题开关回调函数
    private void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mThemeSwitch.setChecked(isChecked);
        if (isChecked) {
            setThemeColor(Color.DKGRAY);
        } else {
            setThemeColor(getResources().getColor(R.color.colorPrimaryDark));
        }
    }

    // 导航菜单项被选中的回调函数
    private boolean onItemSelected(MenuItem item) {

        if(currentMenuItem!=null&&item.getItemId()==R.id.menu_about)
        {
            Intent intent = new Intent(getApplication(), AboutActivity.class);
            MainActivity.this.startActivity(intent);
            return true;
        }


        if (currentMenuItem != item && currentMenuItem != null) {
            currentMenuItem.setChecked(false);
            int id = item.getItemId();
            SharePreferenceUtil.putNevigationItem(MainActivity.this, id);
            currentMenuItem = item;
            currentMenuItem.setChecked(true);
            switchFragment(getFragmentById(currentMenuItem.getItemId()), fragmentIdAndTitleMap.get(currentMenuItem.getItemId()));
        }
        drawer.closeDrawer(GravityCompat.END, true);
        return true;
    }

    // 初始化fragment 界面
    private void initFragment(Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            int navigationId = SharePreferenceUtil.getNevigationItem(this);
            if (navigationId != -1) {
                currentMenuItem = navView.getMenu().findItem(navigationId);
            }
            if (currentMenuItem == null) {
                currentMenuItem = navView.getMenu().findItem(R.id.zhihuitem);
            }
            if (currentMenuItem != null) {
                currentMenuItem.setChecked(true);
                Fragment fragment = getFragmentById(currentMenuItem.getItemId());
                String title = fragmentIdAndTitleMap.get(currentMenuItem.getItemId());
                if (fragment != null) {
                    switchFragment(fragment, title);
                }
            }
        } else {
            if (currentMenuItem != null) {
                Fragment fragment = getFragmentById(currentMenuItem.getItemId());
                String title = fragmentIdAndTitleMap.get((Integer) currentMenuItem.getItemId());
                if (fragment != null) {
                    switchFragment(fragment, title);
                }
            } else {
                switchFragment(new ZhihuFragment(), " ");
                currentMenuItem = navView.getMenu().findItem(R.id.zhihuitem);

            }
        }

    }

    private void setThemeColor(int color) {
        getWindow().setStatusBarColor(color);
        toolbar.setBackgroundColor(color);
    }

    private void setStatusColor() {
        Bitmap bm = BitmapFactory.decodeResource(getResources(),
                R.drawable.nav_icon);
        Palette palette = new Palette.Builder(bm).generate();
        if (palette.getLightVibrantSwatch() != null) {
            mainColor = palette.getLightVibrantSwatch().getRgb();
            getWindow().setStatusBarColor(mainColor);
            toolbar.setBackgroundColor(palette.getLightVibrantSwatch().getRgb());
        }
    }

    // 通过id获取相应的fragment
    private Fragment getFragmentById(int id) {
        Fragment fragment;
        switch (id) {
            case R.id.zhihuitem:
                fragment = new ZhihuFragment();
                break;
            case R.id.topnewsitem:
                fragment = new TopNewsFragment();
                break;
            default:
                fragment = new MeiziFragment();
                break;
        }
        return fragment;
    }

    // 把对应的fragment的id和标题映射起来
    private void mapIdsWithTitlesOfFragment() {
        fragmentIdAndTitleMap.put(R.id.zhihuitem, getResources().getString(R.string.zhihu));
        fragmentIdAndTitleMap.put(R.id.topnewsitem, getResources().getString(R.string.topnews));
        fragmentIdAndTitleMap.put(R.id.meiziitem, getResources().getString(R.string.meizi));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END);
        } else {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(MainActivity.this, "再点一次，退出", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                super.onBackPressed();
            }
        }
    }

    // 选择要显示的fragment
    private void switchFragment(Fragment fragment, String title) {

        if (fragment != null) {
            if (currentFragment == null || currentFragment!=fragment)
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment)
                        .commit();
            currentFragment = fragment;
            toolbar.setTitle(title);
        }
    }

    // 标题动画
    private void animateTitle() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        // this is gross but toolbar doesn't expose it's children to animate them :(
        View t = toolbar.getChildAt(0);
        if (t != null && t instanceof TextView) {
            TextView title = (TextView) t;

            // fade in and space out the title.  Animating the letterSpacing performs horribly so
            // fake it by setting the desired letterSpacing then animating the scaleX ¯\_(ツ)_/¯
            title.setAlpha(0f);
            title.setScaleX(0.8f);

            title.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .setStartDelay(500)
                    .setDuration(900)
                    .setInterpolator(AnimUtils.getFastOutSlowInInterpolator(this)).start();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void getPic() {
        View headerLayout = navView.getHeaderView(0);
        LinearLayout llImage =  headerLayout.findViewById(R.id.side_image);
        File file = new File(getFilesDir().getPath() + "/bg.jpg");
        if (file!=null && file.exists()) {
            BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(),file.getAbsolutePath());
            llImage.setBackground(bitmapDrawable);
        }
    }

    //    when recycle view scroll bottom,need loading more date and show the more view.
    public interface LoadingMore {

        void onStartLoading();

        void onFinishLoading();
    }

    // toolbar 中的menu item的点击事件
    public boolean onMenuItemClick(MenuItem menuItem) {
        // 打开抽屉导航栏
        drawer.openDrawer(GravityCompat.END);
        return true;

    }

    public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
        // inset the toolbar down by the status bar height
        ViewGroup.MarginLayoutParams lpToolbar = (ViewGroup.MarginLayoutParams) toolbar
                .getLayoutParams();
        lpToolbar.topMargin += insets.getSystemWindowInsetTop();
        lpToolbar.rightMargin += insets.getSystemWindowInsetRight();
        toolbar.setLayoutParams(lpToolbar);

        // inset the grid top by statusbar+toolbar & the bottom by the navbar (don't clip)
        mFragmentContainer.setPadding(mFragmentContainer.getPaddingLeft(),
                insets.getSystemWindowInsetTop() + ViewUtils.getActionBarSize
                        (MainActivity.this),
                mFragmentContainer.getPaddingRight() + insets.getSystemWindowInsetRight(), // landscape
                mFragmentContainer.getPaddingBottom() + insets.getSystemWindowInsetBottom());

        // we place a background behind the status bar to combine with it's semi-transparent
        // color to get the desired appearance.  Set it's height to the status bar height
        View statusBarBackground = findViewById(R.id.status_bar_background);
        FrameLayout.LayoutParams lpStatus = (FrameLayout.LayoutParams)
                statusBarBackground.getLayoutParams();
        lpStatus.height = insets.getSystemWindowInsetTop();
        statusBarBackground.setLayoutParams(lpStatus);

        // inset the filters list for the status bar / navbar
        // need to set the padding end for landscape case

        // clear this listener so insets aren't re-applied
        drawer.setOnApplyWindowInsetsListener(null);
        return insets.consumeSystemWindowInsets();
    }

}




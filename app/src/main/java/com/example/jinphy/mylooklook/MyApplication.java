package com.example.jinphy.mylooklook;

import android.app.Application;
import android.content.Context;
import android.support.v7.app.AppCompatDelegate;

import com.example.jinphy.mylooklook.activity.BaseActivity;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by xinghongfei on 16/8/12.
 */
public class MyApplication extends Application {

    public final static String TAG = "BaseApplication";
    public final static boolean DEBUG = true;
    private static MyApplication myApplication;
    private static int mainTid;

    /**
     * Activity集合，来管理所有的Activity
     */
    private static List<BaseActivity> activities;

    static {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public static Application getContext() {
        return myApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
        activities = new LinkedList<>();
        mainTid = android.os.Process.myTid();
    }

    /**
     * 获取application
     *
     * @return
     */
    public static Context getApplication() {
        return myApplication;
    }

    /**
     * 获取主线程ID
     *
     * @return
     */
    public static int getMainTid() {
        return mainTid;
    }

    /**
     * 添加一个Activity
     *
     * @param activity
     */
    public void addActivity(BaseActivity activity) {
        activities.add(activity);
    }

    /**
     * 结束一个Activity
     *
     * @param activity
     */
    public void removeActivity(BaseActivity activity) {
        activities.remove(activity);

    }

    /**
     * 结束当前所有Activity
     */
    public static void clearActivities() {
        activities.forEach(activity->{
            if (activity!=null){
                activity.finish();
            }
        });
    }

    /**
     * 退出应运程序
     */
    public static void quiteApplication() {
        clearActivities();
        System.exit(0);
    }

}

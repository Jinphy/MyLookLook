package com.example.jinphy.mylooklook.presenter.implPresenter;

import android.content.Context;

import com.example.jinphy.mylooklook.api.ApiManager;
import com.example.jinphy.mylooklook.bean.zhihu.ZhihuDaily;
import com.example.jinphy.mylooklook.bean.zhihu.ZhihuDailyItem;
import com.example.jinphy.mylooklook.config.Config;
import com.example.jinphy.mylooklook.presenter.IZhihuPresenter;
import com.example.jinphy.mylooklook.presenter.implView.IZhihuFragment;
import com.example.jinphy.mylooklook.util.CacheUtil;
import com.google.gson.Gson;

import org.json.JSONObject;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by 蔡小木 on 2016/4/23 0023.
 */
public class ZhihuPresenterImpl extends BasePresenterImpl implements IZhihuPresenter {

    private IZhihuFragment mZhihuFragment;
    private CacheUtil mCacheUtil;
    private Gson gson = new Gson();

    public ZhihuPresenterImpl(Context context,IZhihuFragment zhihuFragment) {

        mZhihuFragment = zhihuFragment;
        mCacheUtil = CacheUtil.get(context);
    }

    @Override
    public void getLastZhihuNews() {
        mZhihuFragment.showProgressDialog();
        Subscription subscription = ApiManager.getInstence().getZhihuApiService().getLastDaily()
                .map(this::dacerote)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNextNews,this::onError);

        addSubscription(subscription);
    }


    private ZhihuDaily dacerote(ZhihuDaily daily) {
        String date = daily.getDate();
        for (ZhihuDailyItem item : daily.getStories()) {
            item.setDate(date);
        }
        return daily;
    }


    private void onError(Throwable e) {
        mZhihuFragment.hidProgressDialog();
        mZhihuFragment.showError(e.getMessage());
    }

    private void onNextNews(ZhihuDaily daily) {
        mZhihuFragment.hidProgressDialog();
        mCacheUtil.put(Config.ZHIHU, gson.toJson(daily));
        mZhihuFragment.updateList(daily);
    }

    private void onNextDaily(ZhihuDaily daily) {
        mZhihuFragment.hidProgressDialog();
        mZhihuFragment.updateList(daily);

    }

    @Override
    public void getTheDaily(String date) {
        Subscription subscription = ApiManager.getInstence().getZhihuApiService().getTheDaily(date)
                .map(this::dacerote)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNextDaily,this::onError);

        addSubscription(subscription);
    }

    @Override
    public void getLastFromCache() {
        JSONObject jsonObject = mCacheUtil.getAsJSONObject(Config.ZHIHU);
        if (jsonObject != null) {
            ZhihuDaily zhihuDaily = gson.fromJson(jsonObject.toString(), ZhihuDaily.class);
            mZhihuFragment.updateList(zhihuDaily);
        }
    }
}

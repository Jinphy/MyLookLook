package com.example.jinphy.mylooklook.presenter.implPresenter;

import android.content.Context;

import com.example.jinphy.mylooklook.api.ApiManager;
import com.example.jinphy.mylooklook.bean.meizi.MeiziData;
import com.example.jinphy.mylooklook.bean.meizi.VedioData;
import com.example.jinphy.mylooklook.config.Config;
import com.example.jinphy.mylooklook.presenter.IMeiziPresenter;
import com.example.jinphy.mylooklook.presenter.implView.IMeiziFragment;
import com.example.jinphy.mylooklook.util.CacheUtil;
import com.google.gson.Gson;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by 蔡小木 on 2016/4/23 0023.
 */
public class MeiziPresenterImpl extends BasePresenterImpl implements IMeiziPresenter {

    private IMeiziFragment mMeiziFragment;
    private CacheUtil mCacheUtil;
    private Gson gson = new Gson();

    public MeiziPresenterImpl(Context context, IMeiziFragment mMeiziFragment) {

        this.mMeiziFragment = mMeiziFragment;
        mCacheUtil = CacheUtil.get(context);
    }

    @Override
    public void getMeiziData(int t) {
        mMeiziFragment.showProgressDialog();
        Subscription subscription = ApiManager.getInstence().getGankService().getMeizhiData(t)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNext1,this::onError1);
        addSubscription(subscription);
    }


    private void onNext1(MeiziData meiziData){
        mMeiziFragment.hidProgressDialog();
        mCacheUtil.put(Config.ZHIHU, gson.toJson(meiziData));
        mMeiziFragment.updateMeiziData(meiziData.getResults());
    }

    private void onError1(Throwable e) {
        mMeiziFragment.hidProgressDialog();
        mMeiziFragment.showError(e.getMessage());
    }


    @Override
    public void getVedioData(int t) {
        Subscription subscription = ApiManager.getInstence().getGankService().getVedioData(t)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNext2,this::onError2);
        addSubscription(subscription);
    }



    public void onNext2(VedioData vedioData) {
        mMeiziFragment.hidProgressDialog();
        mMeiziFragment.updateVedioData(vedioData.getResults());
    }


    public void onError2(Throwable e) {
        e.printStackTrace();
        mMeiziFragment.hidProgressDialog();
        mMeiziFragment.showError(e.getMessage());
    }

}

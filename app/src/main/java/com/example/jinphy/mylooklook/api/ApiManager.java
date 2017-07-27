package com.example.jinphy.mylooklook.api;


import com.example.jinphy.mylooklook.MyApplication;
import com.example.jinphy.mylooklook.util.FileUtils;
import com.example.jinphy.mylooklook.util.NetWorkUtil;

import java.io.File;
import java.io.IOException;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by xinghongfei on 16/8/12.
 */
public class ApiManager {

    private static ApiManager apiManager = new ApiManager();

    private static final Interceptor REWRITE_CACHE_CONTROL_INTERCEPTOR = ApiManager::intercept;
    private static File httpCacheDirectory = FileUtils.getCacheDir(MyApplication.getContext(),"zhihuCache");
    private static int cacheSize = 10 * 1024 * 1024; // 10 MiB
    private static Cache cache = new Cache(httpCacheDirectory, cacheSize);
    private static OkHttpClient client = getClient();

    private Object lock = new Object();

    public static ApiManager getInstence() {
        return apiManager;
    }

    // 网络请求拦截回调函数
    private static Response intercept(Interceptor.Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());
        if (NetWorkUtil.isNetWorkAvailable(MyApplication.getContext())) {
            int maxAge = 60; // 在线缓存在1分钟内可读取
            return originalResponse.newBuilder()
                    .removeHeader("Pragma")
                    .removeHeader("Cache-Control")
                    .header("Cache-Control", "public, max-age=" + maxAge)
                    .build();
        } else {
            int maxStale = 60 * 60 * 24 * 28; // 离线时缓存保存4周
            return originalResponse.newBuilder()
                    .removeHeader("Pragma")
                    .removeHeader("Cache-Control")
                    .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                    .build();
        }
    }

    private static OkHttpClient getClient() {
        return new OkHttpClient.Builder()
                .addNetworkInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR)
                .addInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR)
                .cache(cache)
                .build();
    }


    private ZhihuApi zhihuApi;
    public ZhihuApi getZhihuApiService() {
        if (zhihuApi == null) {
            synchronized (lock) {
                if (zhihuApi == null) {
                    zhihuApi = new Retrofit.Builder()
                            .baseUrl("http://news-at.zhihu.com")
                            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build().create(ZhihuApi.class);
                }
            }
        }

        return zhihuApi;
    }

    private TopNews topNews;
    public TopNews getTopNewsService() {
        if (topNews == null) {
            synchronized (lock) {
                if (topNews == null) {
                    topNews = new Retrofit.Builder()
                            .baseUrl("http://c.m.163.com")
                            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build().create(TopNews.class);

                }
            }
        }

        return topNews;
    }

    private GankApi ganK;
    public GankApi getGankService(){
        if (ganK==null){
            synchronized (lock){
                if (ganK==null){
                    ganK=new Retrofit.Builder()
                            .baseUrl("http://gank.io")
                            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build().create(GankApi.class);

                }
            }
        }
        return ganK;
    }
}

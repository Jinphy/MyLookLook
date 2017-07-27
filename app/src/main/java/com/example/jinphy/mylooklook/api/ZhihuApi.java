package com.example.jinphy.mylooklook.api;


import com.example.jinphy.mylooklook.bean.image.ImageResponse;
import com.example.jinphy.mylooklook.bean.zhihu.ZhihuDaily;
import com.example.jinphy.mylooklook.bean.zhihu.ZhihuStory;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;


public interface ZhihuApi {

    @GET("/api/4/news/latest")
    Observable<ZhihuDaily> getLastDaily();

    @GET("/api/4/news/before/{date}")
    Observable<ZhihuDaily> getTheDaily(@Path("date") String date);

    @GET("/api/4/news/{id}")
    Observable<ZhihuStory> getZhihuStory(@Path("id") String id);

    @GET("http://lab.zuimeia.com/wallpaper/category/1/?page_size=1")
    Observable<ImageResponse> getImage();

}

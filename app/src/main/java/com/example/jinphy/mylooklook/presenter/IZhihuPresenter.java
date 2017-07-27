package com.example.jinphy.mylooklook.presenter;



public interface IZhihuPresenter extends BasePresenter{
    void getLastZhihuNews();

    void getTheDaily(String date);

    void getLastFromCache();
}

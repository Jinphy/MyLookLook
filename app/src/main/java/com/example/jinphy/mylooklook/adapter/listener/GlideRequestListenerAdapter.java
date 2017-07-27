package com.example.jinphy.mylooklook.adapter.listener;

import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

/**
 * Created by jinphy on 2017/7/27.
 */

public abstract class GlideRequestListenerAdapter<T,R> implements RequestListener<T,R> {
    @Override
    public boolean onException(Exception e, T model, Target<R> target, boolean isFirstResource) {
        return false;
    }

    @Override
    public boolean onResourceReady(R resource, T model, Target<R> target, boolean
            isFromMemoryCache, boolean isFirstResource) {
        return false;
    }
}

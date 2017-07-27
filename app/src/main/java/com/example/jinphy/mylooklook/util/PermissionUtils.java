package com.example.jinphy.mylooklook.util;

import android.Manifest;
import android.app.Activity;

import com.tbruyelle.rxpermissions.RxPermissions;

/**
 * Created by jinphy on 2017/7/27.
 */

public class PermissionUtils {

    private PermissionUtils(){}


    public static boolean has(Activity activity, String... permissions) {
        final Result result = new Result();
        RxPermissions rxPermissions = new RxPermissions(activity);
        rxPermissions.request(permissions)
                .subscribe(granted-> {
                    result.result = granted;
                });
        return result.result;
    }

    private static class Result{
        boolean result;
    }
}












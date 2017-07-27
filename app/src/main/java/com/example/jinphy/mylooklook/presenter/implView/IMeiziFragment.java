package com.example.jinphy.mylooklook.presenter.implView;


import com.example.jinphy.mylooklook.bean.meizi.Gank;
import com.example.jinphy.mylooklook.bean.meizi.Meizi;

import java.util.ArrayList;

/**
 * Created by xinghongfei on 16/8/20.
 */
public interface IMeiziFragment extends IBaseFragment {
     void updateMeiziData(ArrayList<Meizi> list);
     void updateVedioData(ArrayList<Gank> list);
}

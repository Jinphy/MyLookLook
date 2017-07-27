package com.example.jinphy.mylooklook.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.jinphy.mylooklook.R;
import com.example.jinphy.mylooklook.adapter.GirlAdapter;
import com.example.jinphy.mylooklook.bean.meizi.Gank;
import com.example.jinphy.mylooklook.bean.meizi.Meizi;
import com.example.jinphy.mylooklook.presenter.implPresenter.MeiziPresenterImpl;
import com.example.jinphy.mylooklook.presenter.implView.IMeiziFragment;
import com.example.jinphy.mylooklook.util.Once;
import com.example.jinphy.mylooklook.widget.WrapContentLinearLayoutManager;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by xinghongfei on 16/8/20.
 */
public class MeiziFragment extends BaseFragment implements IMeiziFragment {

    @BindView(R.id.recycle_meizi)
     RecyclerView mRecycleMeizi;
    @BindView(R.id.prograss)
     ProgressBar mPrograss;

    private WrapContentLinearLayoutManager linearLayoutManager;
    private GirlAdapter meiziAdapter;
    private RecyclerView.OnScrollListener loadMoreListener;
    private MeiziPresenterImpl mMeiziPresenter;

    private boolean isLoading;

    private int index = 1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.meizi_fragment_layout, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    private static final String TAG = "MeiziFragment";
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        mMeiziPresenter = new MeiziPresenterImpl(getContext(), this);

        meiziAdapter = new GirlAdapter(getContext());
        linearLayoutManager = new WrapContentLinearLayoutManager(getContext());

        loadMoreListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    //向下滚动
                    int visibleItemCount = linearLayoutManager.getChildCount();
                    int totalItemCount = linearLayoutManager.getItemCount();
                    int firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();

                    if (!isLoading && (visibleItemCount + firstVisibleItem) >= totalItemCount) {
                        isLoading = true;
                        index += 1;
                        loadMoreDate();
                    }
                }
            }
        };

        mRecycleMeizi.setLayoutManager(linearLayoutManager);
        mRecycleMeizi.setAdapter(meiziAdapter);
        mRecycleMeizi.addOnScrollListener(loadMoreListener);

        new Once(getContext()).show("tip_guide_6", () ->
                Snackbar.make(mRecycleMeizi, getString(R.string.meizitips), Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.meiziaction, view1 -> {})
                .show()
        );
        mRecycleMeizi.setItemAnimator(new DefaultItemAnimator());

        loadDate();

        super.onViewCreated(view, savedInstanceState);
    }

    private void loadDate() {
        if (meiziAdapter.getItemCount() > 0) {
            meiziAdapter.clearData();
        }
        mMeiziPresenter.getMeiziData(index);

    }

    private void loadMoreDate() {
        meiziAdapter.onStartLoading();
        mMeiziPresenter.getMeiziData(index);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMeiziPresenter.unsubscrible();
    }

    @Override
    public void updateMeiziData(ArrayList<Meizi> list) {

        meiziAdapter.onFinishLoading();
        isLoading = false;
        meiziAdapter.addItems(list);
        mMeiziPresenter.getVedioData(index);
    }

    @Override
    public void updateVedioData(ArrayList<Gank> list) {
        meiziAdapter.addVideoData(list);
    }

    @Override
    public void showProgressDialog() {
        mPrograss.setVisibility(View.VISIBLE);
    }

    @Override
    public void hidProgressDialog() {
        mPrograss.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showError(String error) {
        mPrograss.setVisibility(View.INVISIBLE);
        if (mRecycleMeizi != null) {
            Snackbar.make(mRecycleMeizi, getString(R.string.snack_infor), Snackbar.LENGTH_SHORT).setAction("重试", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMeiziPresenter.getMeiziData(index);
                }
            }).show();

        }
    }
}


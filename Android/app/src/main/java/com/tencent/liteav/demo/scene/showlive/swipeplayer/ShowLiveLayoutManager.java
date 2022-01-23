package com.tencent.liteav.demo.scene.showlive.swipeplayer;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

public class ShowLiveLayoutManager extends LinearLayoutManager
        implements RecyclerView.OnChildAttachStateChangeListener {

    private int     mDrift;//位移，用来判断移动方向，是向上还是向下滑，大于0向上滑，小于0向下滑
    private boolean mCanScroll = true;

    //PagerSnapHelper 可以使Recyclerview实现跟viewpager一样的效果，每次滑一个item
    private PagerSnapHelper     mPagerSnapHelper;
    private OnViewPagerListener mOnViewPagerListener;
    private int                 mCurrentPosition = -1;

    public ShowLiveLayoutManager(Context context) {
        super(context);
    }

    public ShowLiveLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        mPagerSnapHelper = new PagerSnapHelper();
    }


    @Override
    public void onAttachedToWindow(RecyclerView view) {
        view.addOnChildAttachStateChangeListener(this);
        mPagerSnapHelper.attachToRecyclerView(view);
        super.onAttachedToWindow(view);
    }

    public void setCanScroll(boolean mCanScroll) {
        this.mCanScroll = mCanScroll;
    }

    @Override
    public boolean canScrollVertically() {
        if (!mCanScroll) {
            return false;
        }
        return super.canScrollVertically();
    }

    /*当item滑出时，会调用这个方法*/
    @Override
    public void onChildViewDetachedFromWindow(@NonNull View view) {
        int position = getPosition(view);
        if (mOnViewPagerListener == null || mDrift == 0) {
            return;
        }
        mOnViewPagerListener.onPageRelease(mDrift > 0, position);
        mCurrentPosition = position;
    }

    /*对滑动状态监听，主要是手指释放时，找到惯性滑动停止后的item*/
    @Override
    public void onScrollStateChanged(int state) {
        switch (state) {
            case RecyclerView.SCROLL_STATE_IDLE:
                View view = mPagerSnapHelper.findSnapView(this);//获取到当前的item
                int position = getPosition(view);
                if (mOnViewPagerListener != null && mCurrentPosition != position) {
                    mOnViewPagerListener.onPageSelected(position, position == getItemCount() - 1);
                    mCurrentPosition = position;
                }
                break;
            default:
                break;
        }
        super.onScrollStateChanged(state);
    }


    public void setOnViewPagerListener(OnViewPagerListener mOnViewPagerListener) {
        this.mOnViewPagerListener = mOnViewPagerListener;
    }

    //这里主要是为了判断是向上滑还是向下滑
    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        this.mDrift = dy;
        return super.scrollVerticallyBy(dy, recycler, state);
    }

    @Override
    public void onChildViewAttachedToWindow(@NonNull View view) {
        int position = getPosition(view);
        /*
        这里主要是为了第一次进入时，没进行滑动时，让第一个item播放
        */
        if (0 == position) {
            if (mOnViewPagerListener != null) {
                mOnViewPagerListener.onPageSelected(getPosition(view), false);
            }

        }
    }

    public interface OnViewPagerListener {
        /*item滑出释放的监听*/
        void onPageRelease(boolean isNext, int position);

        /*item滑入的监听以及判断是否滑动到底部*/
        void onPageSelected(int position, boolean isBottom);
    }
}

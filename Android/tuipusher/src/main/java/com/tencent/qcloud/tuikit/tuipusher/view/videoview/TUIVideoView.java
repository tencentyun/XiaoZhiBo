package com.tencent.qcloud.tuikit.tuipusher.view.videoview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.constraintlayout.widget.Guideline;

import com.tencent.qcloud.tuikit.tuipusher.R;
import com.tencent.rtmp.ui.TXCloudVideoView;

public class TUIVideoView extends RelativeLayout {
    public  View             mViewRoot;
    private ConstraintLayout mLayoutRoot;
    private TXCloudVideoView mPushVideoView;
    private TXCloudVideoView mPKVideoView;
    private RelativeLayout   mPKVideoContainer;
    private Guideline        mGLHorizontal;
    private Guideline        mGLHorizontal25;
    private Guideline        mGLHorizontal75;
    private Guideline        mGLVertical;
    private TXCloudVideoView mLinkVideoView;
    private RelativeLayout   mLinkVideoContainer;
    private ImageView        mImagePKLayer;
    private ImageView        mImageClsoe;
    private OnCloseListener  mListener;

    public TUIVideoView(Context context) {
        this(context, null);
    }

    public TUIVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public void setListener(OnCloseListener listener) {
        mListener = listener;
    }

    private void initView() {
        mViewRoot = LayoutInflater.from(getContext()).inflate(R.layout.tuipusher_video_view, this, true);
        mLayoutRoot = mViewRoot.findViewById(R.id.cl_root);
        mPushVideoView = mViewRoot.findViewById(R.id.txc_push_video_view);
        mPKVideoView = mViewRoot.findViewById(R.id.txc_pk_video_view);
        mPKVideoContainer = mViewRoot.findViewById(R.id.tl_pk_video_container);
        mGLHorizontal = mViewRoot.findViewById(R.id.gl_horizontal);
        mGLHorizontal25 = mViewRoot.findViewById(R.id.gl_horizontal25);
        mGLHorizontal75 = mViewRoot.findViewById(R.id.gl_horizontal75);
        mGLVertical = mViewRoot.findViewById(R.id.gl_vertical);
        mLinkVideoView = mViewRoot.findViewById(R.id.txc_link_video_view);
        mLinkVideoContainer = mViewRoot.findViewById(R.id.rl_link_video_container);
        mImageClsoe = mViewRoot.findViewById(R.id.iv_close);
        mImagePKLayer = mViewRoot.findViewById(R.id.iv_pk_layer);
        mImagePKLayer.setVisibility(INVISIBLE);
        mImageClsoe.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClose();
                }
            }
        });
    }

    public TXCloudVideoView getPushVideoView() {
        return mPushVideoView;
    }

    public void showPKMode(boolean flag) {
        ConstraintSet set = new ConstraintSet();
        if (flag) {
            mLinkVideoContainer.setVisibility(GONE);
            mPKVideoContainer.setVisibility(VISIBLE);
            set.clone(mLayoutRoot);
            set.connect(mPushVideoView.getId(), ConstraintSet.END, mGLVertical.getId(), ConstraintSet.END);
            set.connect(mPushVideoView.getId(), ConstraintSet.BOTTOM, mGLHorizontal75.getId(), ConstraintSet.BOTTOM);
            set.connect(mPushVideoView.getId(), ConstraintSet.TOP, mGLHorizontal25.getId(), ConstraintSet.BOTTOM);
        } else {
            mPKVideoContainer.setVisibility(GONE);
            set.clone(mLayoutRoot);
            set.connect(mPushVideoView.getId(), ConstraintSet.END, mLayoutRoot.getId(), ConstraintSet.END);
            set.connect(mPushVideoView.getId(), ConstraintSet.BOTTOM, mLayoutRoot.getId(), ConstraintSet.BOTTOM);
            set.connect(mPushVideoView.getId(), ConstraintSet.TOP, mLayoutRoot.getId(), ConstraintSet.TOP);
        }
        set.applyTo(mLayoutRoot);
        if (flag) {
            mImagePKLayer.setVisibility(VISIBLE);
        } else {
            mImagePKLayer.setVisibility(INVISIBLE);
        }
    }

    public TXCloudVideoView getPKVideoView() {
        return mPKVideoView;
    }

    public TXCloudVideoView getLinkVideoView() {
        return mLinkVideoView;
    }

    public void showLinkMode(boolean flag) {
        if (flag) {
            mLinkVideoContainer.setVisibility(VISIBLE);
        } else {
            mLinkVideoContainer.setVisibility(GONE);
        }
    }

    public interface OnCloseListener {
        void onClose();
    }
}

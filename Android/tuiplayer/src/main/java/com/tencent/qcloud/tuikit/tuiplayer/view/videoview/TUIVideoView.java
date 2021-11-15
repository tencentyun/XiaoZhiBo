package com.tencent.qcloud.tuikit.tuiplayer.view.videoview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;

import com.tencent.qcloud.tuikit.tuiplayer.R;
import com.tencent.rtmp.ui.TXCloudVideoView;

/**
 * 统一管理推拉流视频渲染View
 */
public class TUIVideoView extends RelativeLayout {
    public  View             mViewRoot;
    private ConstraintLayout mLayoutRoot;
    private TXCloudVideoView mMainVideoView;
    private TXCloudVideoView mTXCloudLinkVideoView;
    private RelativeLayout   mLayoutLinkVideoContainer;
    private Guideline        mGLHorizontal;
    private Guideline        mGLVertical;


    public TUIVideoView(Context context) {
        this(context, null);
    }

    public TUIVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        mViewRoot = LayoutInflater.from(getContext()).inflate(R.layout.tuiplayer_video_view, this, true);
        mLayoutRoot = mViewRoot.findViewById(R.id.cl_root);
        mMainVideoView = mViewRoot.findViewById(R.id.txc_play_video_view);
        mTXCloudLinkVideoView = mViewRoot.findViewById(R.id.txc_link_video_view);
        mLayoutLinkVideoContainer = mViewRoot.findViewById(R.id.rl_link_video_container);
        mGLHorizontal = mViewRoot.findViewById(R.id.gl_horizontal);
        mGLVertical = mViewRoot.findViewById(R.id.gl_vertical);
    }

    public TXCloudVideoView getMainVideoView() {
        return mMainVideoView;
    }

    public void showLinkMode(boolean flag) {
        if (flag) {
            mLayoutLinkVideoContainer.setVisibility(VISIBLE);
        } else {
            mLayoutLinkVideoContainer.setVisibility(GONE);
        }
    }

    public TXCloudVideoView getLinkVideoView() {
        return mTXCloudLinkVideoView;
    }
}

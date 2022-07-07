package com.tencent.liteav.showlive.ui.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ToastUtils;
import com.tencent.liteav.basic.ImageLoader;
import com.tencent.liteav.basic.RTCubeUtils;
import com.tencent.liteav.basic.UserModelManager;
import com.tencent.liteav.showlive.R;
import com.tencent.liteav.showlive.model.services.RoomService;
import com.tencent.liteav.showlive.model.services.room.bean.AudienceInfo;
import com.tencent.liteav.showlive.model.services.room.callback.CommonCallback;
import com.tencent.liteav.showlive.model.services.room.callback.RoomMemberInfoCallback;
import com.tencent.liteav.showlive.model.services.room.im.impl.IMRoomManager;
import com.tencent.liteav.showlive.ui.dialog.FollowAnchorDialog;
import com.tencent.liteav.showlive.ui.dialog.HourRankDialog;
import com.tencent.liteav.showlive.ui.dialog.MoreLiveDialog;
import com.tencent.liteav.showlive.ui.dialog.OnlineAudienceDialog;
import com.tencent.qcloud.tuikit.tuiplayer.view.TUIPlayerView;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 秀场直播 - 观众页面等功能性View，包含：
 * <p>
 * - 顶部房间信息的View；
 * - 底部退出直播间的View;
 * </p>
 */
public class ShowAudienceFunctionView extends FrameLayout implements View.OnClickListener {
    private View               mViewRoot;
    private ImageView          mImageClose;
    private ImageView          mBtnReport;
    private ImageView          mImagesAnchorHead;      // 显示房间主播头像
    private TextView           mTextAnchorName;        // 显示房间主播名称
    private TextView           mTextRoomId;            // 显示当前房间号
    private TextView           mTextAudienceAmount;      // 显示在线观众人数
    private TextView           mTextMoreLive;            // 显示更多直播
    private TextView           mTextHourRank;          // 显示小时榜
    private ImageView          mImageAdvert;           // 显示外链广告
    private ImageView          mImageAudience1;        // 显示第一名观众
    private ImageView          mImageAudience2;        // 显示第二名观众
    private ImageView          mImageAudience3;        // 显示第三名观众
    private ImageView          mImageAudience4;        // 显示第四名观众
    private TextView           mTextFollowAnchor;        // 关注主播
    private LinearLayout       mAudienceList;          // 显示观众列表
    private RelativeLayout     mRelativeAnchorInfo;    // 显示房间信息
    private TUIPlayerView      mTUIPlayerView;
    private String             mRoomId;
    private String             mAnchorName;
    private String             mAnchorAvatar;
    private OnFunctionListener mOnFunctionListener;
    private String             mOwnerId;
    private boolean            mIsFollowed = false;

    private List<AudienceInfo> mAudienceInfoList = new ArrayList<>();

    public ShowAudienceFunctionView(@NonNull Context context) {
        this(context, null);
    }

    public ShowAudienceFunctionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mViewRoot = LayoutInflater.from(getContext()).inflate(R.layout.showlive_show_audience_function_view, this,
                true);
        initView();
        initOnclickListener();
        setMemberChangeListener();
    }

    private void initView() {
        mImagesAnchorHead = mViewRoot.findViewById(R.id.iv_anchor_head);
        mTextAnchorName = mViewRoot.findViewById(R.id.tv_anchor_name);
        mTextRoomId = mViewRoot.findViewById(R.id.tv_room_id);
        mTextAudienceAmount = mViewRoot.findViewById(R.id.iv_audience_amount);
        mTextMoreLive = mViewRoot.findViewById(R.id.tv_live_more);
        mAudienceList = mViewRoot.findViewById(R.id.ll_audience_live_audience_list);
        mTextHourRank = mViewRoot.findViewById(R.id.tv_audience_hour_rank);
        mImageAdvert = mViewRoot.findViewById(R.id.iv_audience_ad);
        mTextFollowAnchor = mViewRoot.findViewById(R.id.tv_follow_anchor);
        mRelativeAnchorInfo = mViewRoot.findViewById(R.id.relativeLayout);
        mImageAudience1 = mViewRoot.findViewById(R.id.iv_audience_1);
        mImageAudience2 = mViewRoot.findViewById(R.id.iv_audience_2);
        mImageAudience3 = mViewRoot.findViewById(R.id.iv_audience_3);
        mImageAudience4 = mViewRoot.findViewById(R.id.iv_audience_4);
        mImageAudience1.setVisibility(GONE);
        mImageAudience2.setVisibility(GONE);
        mImageAudience3.setVisibility(GONE);
        mImageAudience4.setVisibility(GONE);
        mImageClose = mViewRoot.findViewById(R.id.iv_close);
        mBtnReport = mViewRoot.findViewById(R.id.btn_report);
        mBtnReport.setVisibility(RTCubeUtils.isRTCubeApp(getContext()) ? View.VISIBLE : View.GONE);
    }

    private void initAudienceList() {
        RoomService.getInstance(getContext()).getRoomAudienceList(mRoomId, new RoomMemberInfoCallback() {
            @Override
            public void onCallback(int code, String msg, List<AudienceInfo> list) {
                mAudienceInfoList = list;
                updateAudienceListInfo(mAudienceInfoList);
            }
        });
    }

    private void setMemberChangeListener() {
        IMRoomManager.getInstance().setMemberChangeListener(new IMRoomManager.OnMemberChangeListener() {
            @Override
            public void onMemberEnter(String groupId, List<AudienceInfo> list) {
                if (TextUtils.isEmpty(groupId) || !groupId.equals(mRoomId)) {
                    return;
                }
                if (getContext() == null || ((Activity) getContext()).isFinishing()) {
                    return;
                }
                if (mAudienceInfoList == null) {
                    return;
                }
                for (int i = 0; i < list.size(); i++) {
                    if (!mAudienceInfoList.contains(list.get(i))) {
                        mAudienceInfoList.add(list.get(i));
                    }
                }
                if (mAudienceInfoList.size() > 3) {
                    mTextAudienceAmount.setText(String.valueOf(mAudienceInfoList.size()));
                } else {
                    updateAudienceListInfo(mAudienceInfoList);
                }
            }

            @Override
            public void onMemberLeave(String groupId, AudienceInfo info) {
                if (TextUtils.isEmpty(groupId) || !groupId.equals(mRoomId)) {
                    return;
                }
                if (getContext() == null || ((Activity) getContext()).isFinishing()) {
                    return;
                }
                mAudienceInfoList.remove(info);
                if (mAudienceInfoList.size() > 3) {
                    mTextAudienceAmount.setText(String.valueOf(mAudienceInfoList.size()));
                } else {
                    updateAudienceListInfo(mAudienceInfoList);
                }
            }
        });
    }

    private void updateAudienceListInfo(List<AudienceInfo> list) {
        mTextAudienceAmount.setText(String.valueOf(list.size()));
        if (list.size() >= 4) {
            mImageAudience1.setVisibility(VISIBLE);
            mImageAudience2.setVisibility(VISIBLE);
            mImageAudience3.setVisibility(VISIBLE);
            mImageAudience4.setVisibility(VISIBLE);
            ImageLoader.loadImage(getContext(), mImageAudience1, list.get(0).getAvatar(), R.drawable.showlive_bg_cover);
            ImageLoader.loadImage(getContext(), mImageAudience2, list.get(1).getAvatar(), R.drawable.showlive_bg_cover);
            ImageLoader.loadImage(getContext(), mImageAudience3, list.get(2).getAvatar(), R.drawable.showlive_bg_cover);
            ImageLoader.loadImage(getContext(), mImageAudience4, list.get(3).getAvatar(), R.drawable.showlive_bg_cover);
        } else if (list.size() == 3) {
            mImageAudience1.setVisibility(VISIBLE);
            mImageAudience2.setVisibility(VISIBLE);
            mImageAudience3.setVisibility(VISIBLE);
            mImageAudience4.setVisibility(GONE);
            ImageLoader.loadImage(getContext(), mImageAudience1, list.get(0).getAvatar(), R.drawable.showlive_bg_cover);
            ImageLoader.loadImage(getContext(), mImageAudience2, list.get(1).getAvatar(), R.drawable.showlive_bg_cover);
            ImageLoader.loadImage(getContext(), mImageAudience3, list.get(2).getAvatar(), R.drawable.showlive_bg_cover);
        } else if (list.size() == 2) {
            mImageAudience1.setVisibility(VISIBLE);
            mImageAudience2.setVisibility(VISIBLE);
            mImageAudience3.setVisibility(GONE);
            mImageAudience4.setVisibility(GONE);
            ImageLoader.loadImage(getContext(), mImageAudience1, list.get(0).getAvatar(), R.drawable.showlive_bg_cover);
            ImageLoader.loadImage(getContext(), mImageAudience2, list.get(1).getAvatar(), R.drawable.showlive_bg_cover);
        } else if (list.size() == 1) {
            mImageAudience1.setVisibility(VISIBLE);
            mImageAudience2.setVisibility(GONE);
            mImageAudience3.setVisibility(GONE);
            mImageAudience4.setVisibility(GONE);
            ImageLoader.loadImage(getContext(), mImageAudience1, list.get(0).getAvatar(), R.drawable.showlive_bg_cover);
        } else {
            mImageAudience1.setVisibility(GONE);
            mImageAudience2.setVisibility(GONE);
            mImageAudience3.setVisibility(GONE);
            mImageAudience4.setVisibility(GONE);
        }
    }

    public void setListener(OnFunctionListener listener) {
        mOnFunctionListener = listener;
    }

    public void setAnchorInfo(String avatarUrl, String anchorName, int roomId, String ownerId) {
        mOwnerId = ownerId;
        ImageLoader.loadImage(getContext(), mImagesAnchorHead, avatarUrl, R.drawable.showlive_bg_cover);
        mTextAnchorName.setText(anchorName);
        mTextRoomId.setText(getContext().getString(R.string.showlive_room_id) + roomId);
        mRoomId = roomId + "";
        mAnchorName = anchorName;
        mAnchorAvatar = avatarUrl;
        RoomService.getInstance(getContext()).checkFriend(mRoomId, new CommonCallback() {
            @Override
            public void onCallback(int code, String msg) {
                if (getContext() == null || ((Activity) getContext()).isFinishing()) {
                    return;
                }
                if (code == 0 && Integer.parseInt(msg) == 1) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            mTextFollowAnchor.setVisibility(GONE);
                        }
                    });
                    mIsFollowed = true;
                }
            }
        });
        initAudienceList();
    }

    public void setTUIPlayerView(TUIPlayerView view) {
        mTUIPlayerView = view;
    }

    private void initOnclickListener() {
        mImageClose.setOnClickListener(this);
        mImageAdvert.setOnClickListener(this);
        mTextHourRank.setOnClickListener(this);
        mAudienceList.setOnClickListener(this);
        mTextMoreLive.setOnClickListener(this);
        mTextFollowAnchor.setOnClickListener(this);
        mRelativeAnchorInfo.setOnClickListener(this);
        mBtnReport.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_close) {
            if (mOnFunctionListener != null) {
                mOnFunctionListener.onClose();
            }
        } else if (v.getId() == R.id.iv_audience_ad) {
            Intent intent = new Intent(getContext(), WebViewActivity.class);
            intent.putExtra("url", "https://comm.qq.com/trtc-app-finding-page/");
            intent.putExtra("title", "");
            getContext().startActivity(intent);
        } else if (v.getId() == R.id.tv_audience_hour_rank) {
            HourRankDialog dialog = new HourRankDialog(getContext(), mRoomId);
            dialog.show();
        } else if (v.getId() == R.id.ll_audience_live_audience_list) {
            OnlineAudienceDialog dialog = new OnlineAudienceDialog(getContext(), mRoomId);
            dialog.show();
        } else if (v.getId() == R.id.tv_live_more) {
            MoreLiveDialog moreLiveDialog = new MoreLiveDialog();
            moreLiveDialog.show(((AppCompatActivity) getContext()).getSupportFragmentManager(), null);
        } else if (v.getId() == R.id.tv_follow_anchor) {
            if (mIsFollowed) {
                unFollowTheAnchor();
            } else {
                followTheAnchor();
            }
        } else if (v.getId() == R.id.relativeLayout) {
            FollowAnchorDialog dialog = new FollowAnchorDialog(getContext(),
                    mIsFollowed,
                    mAnchorName,
                    mAnchorAvatar);
            dialog.setListener(new FollowAnchorDialog.OnFollowClickListener() {
                @Override
                public void onClick(boolean isFollow) {
                    if (isFollow) {
                        followTheAnchor();
                    } else {
                        unFollowTheAnchor();
                    }
                }
            });
            dialog.show();
        } else if (v.getId() == R.id.btn_report) {
            showReportDialog();
        }
    }

    private void followTheAnchor() {
        RoomService.getInstance(getContext()).addFriend(UserModelManager.getInstance().getUserModel().userId,
                mRoomId, new CommonCallback() {
                    @Override
                    public void onCallback(int code, String msg) {
                        if (getContext() == null || ((Activity) getContext()).isFinishing()) {
                            return;
                        }
                        if (code == 0) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    mTextFollowAnchor.setVisibility(GONE);
                                    ToastUtils.showShort(getContext().getResources()
                                            .getString(R.string.showlive_follow_success));
                                }
                            });
                            mIsFollowed = true;
                        }
                    }
                });
    }

    private void unFollowTheAnchor() {
        RoomService.getInstance(getContext()).deleteFromFriendList(mRoomId, new CommonCallback() {
            @Override
            public void onCallback(int code, String msg) {
                if (getContext() == null || ((Activity) getContext()).isFinishing()) {
                    return;
                }
                if (code == 0) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            mTextFollowAnchor.setVisibility(VISIBLE);
                            ToastUtils.showShort(getContext().getResources()
                                    .getString(R.string.showlive_unfollow_success));
                        }
                    });
                    mIsFollowed = false;
                }
            }
        });
    }

    private void showReportDialog() {
        try {
            Class clz = Class.forName("com.tencent.liteav.demo.report.ReportDialog");
            Method method = clz.getDeclaredMethod("showReportDialog", Context.class, String.class, String.class);
            method.invoke(null, getContext(), mRoomId, mOwnerId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface OnFunctionListener {
        void onClose();
    }
}


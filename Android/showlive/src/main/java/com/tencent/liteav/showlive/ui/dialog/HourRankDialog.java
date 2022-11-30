package com.tencent.liteav.showlive.ui.dialog;

import static com.tencent.liteav.showlive.model.services.room.http.impl.HttpRoomManager.TYPE_MLVB_SHOW_LIVE;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.tencent.liteav.basic.ImageLoader;
import com.tencent.liteav.showlive.R;
import com.tencent.liteav.showlive.model.services.RoomService;
import com.tencent.liteav.showlive.model.services.room.bean.RoomInfo;
import com.tencent.liteav.showlive.model.services.room.callback.RoomInfoCallback;
import com.tencent.liteav.showlive.model.services.room.http.impl.HttpRoomManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 小时榜
 */
public class HourRankDialog extends BottomSheetDialog {
    private static final String TAG = "HourRankDialog";

    private Context             mContext;
    private String              mRoomId;
    private HourRankListAdapter mAdapter;
    private RecyclerView        mRecyclerHourRank;
    private List<RoomInfo>      mRoomInfoList;
    private ImageView           mImageAnchorAvatar;
    private TextView            mTextAnchorRanking;
    private TextView            mTextAnchorName;
    private TextView            mTextAnchorValue;

    public HourRankDialog(Context context, String roomId) {
        super(context, R.style.ShowLiveMoreDialogTheme);
        mRoomId = roomId;
        mContext = context;
        mRoomInfoList = new ArrayList<>();
        setContentView(R.layout.showlive_hour_rank_dialog);
        mRecyclerHourRank = findViewById(R.id.recycler_hour_rank);
        mTextAnchorName = findViewById(R.id.tv_my_name);
        mTextAnchorRanking = findViewById(R.id.tv_my_ranking);
        mImageAnchorAvatar = findViewById(R.id.iv_my_avatar);
        mTextAnchorValue = findViewById(R.id.tv_my_value);
        mAdapter = new HourRankListAdapter(mContext, mRoomInfoList);
        mRecyclerHourRank.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerHourRank.setAdapter(mAdapter);
        getRoomList();
    }

    private void getRoomList() {
        RoomService.getInstance().getRoomList(TYPE_MLVB_SHOW_LIVE,
                HttpRoomManager.RoomOrderType.TOTAL_JOINED, new RoomInfoCallback() {
                    @Override
                    public void onCallback(int code, String msg, List<RoomInfo> list) {
                        if (mContext == null || ((Activity) mContext).isFinishing()) {
                            return;
                        }
                        if (code == 0) {
                            int ranking = 0;
                            for (RoomInfo info :
                                    list) {
                                ranking++;
                                if (info.roomId.equals(mRoomId)) {
                                    if (ranking == 1) {
                                        mTextAnchorRanking.setTextColor(mContext.getResources()
                                                .getColor(R.color.showlive_ranking_text_first));
                                    } else if (ranking == 2) {
                                        mTextAnchorRanking.setTextColor(mContext.getResources()
                                                .getColor(R.color.showlive_ranking_text_second));
                                    } else if (ranking == 3) {
                                        mTextAnchorRanking.setTextColor(mContext.getResources()
                                                .getColor(R.color.showlive_ranking_text_third));
                                    }
                                    mTextAnchorRanking.setText(String.valueOf(ranking));
                                    mTextAnchorName.setText(info.roomName);
                                    ImageLoader.loadImage(mContext, mImageAnchorAvatar,
                                            info.coverUrl, R.drawable.showlive_bg_cover);
                                    String totalJoined;
                                    if (info.totalJoined >= 10000) {
                                        totalJoined = info.totalJoined / 10000f + "w";
                                    } else if (info.totalJoined >= 1000) {
                                        totalJoined = info.totalJoined / 1000f + "k";
                                    } else {
                                        totalJoined = info.totalJoined + "";
                                    }
                                    mTextAnchorValue.setText(totalJoined);
                                    break;
                                }
                            }
                            mRoomInfoList.clear();
                            mRoomInfoList.addAll(list);
                            mAdapter.notifyDataSetChanged();
                            Log.i(TAG, "HourRank list" + list.toString());
                        } else {
                            Log.d(TAG, msg);
                        }
                    }
                });
    }
}

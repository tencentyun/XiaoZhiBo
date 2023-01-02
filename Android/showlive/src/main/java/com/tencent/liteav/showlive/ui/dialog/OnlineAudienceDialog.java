package com.tencent.liteav.showlive.ui.dialog;

import android.content.Context;
import android.util.Log;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.tencent.liteav.showlive.R;
import com.tencent.liteav.showlive.model.services.RoomService;
import com.tencent.liteav.showlive.model.services.room.bean.AudienceInfo;
import com.tencent.liteav.showlive.model.services.room.callback.RoomMemberInfoCallback;

import java.util.ArrayList;
import java.util.List;


/**
 * 在线观众
 */
public class OnlineAudienceDialog extends BottomSheetDialog {
    private static final String TAG = "OnlineAudienceDialog";

    private Context             mContext;
    private String              mRoomId;
    private AudienceListAdapter mAdapter;
    private RecyclerView       mRecyclerOnlineAudience;
    private List<AudienceInfo> mAudienceInfoList = new ArrayList<>();

    public OnlineAudienceDialog(Context context, String roomId) {
        super(context, R.style.ShowLiveMoreDialogTheme);
        setContentView(R.layout.showlive_online_audience_dialog);
        mRoomId = roomId;
        mContext = context;
        mRecyclerOnlineAudience = findViewById(R.id.recycler_online_audience);
        mRecyclerOnlineAudience.setLayoutManager(new LinearLayoutManager(mContext));
        mAdapter = new AudienceListAdapter(mContext, mAudienceInfoList);
        mRecyclerOnlineAudience.setAdapter(mAdapter);
        getAudienceList();
    }


    private void getAudienceList() {
        RoomService.getInstance().getRoomAudienceList(mRoomId, new RoomMemberInfoCallback() {
            @Override
            public void onCallback(int code, String msg, List<AudienceInfo> list) {
                if (code == 0) {
                    mAudienceInfoList.clear();
                    mAudienceInfoList.addAll(list);
                    mAdapter.notifyDataSetChanged();
                    Log.i(TAG, "audience list" + list.toString());
                } else {
                    Log.d(TAG, msg);
                }
            }
        });
    }
}

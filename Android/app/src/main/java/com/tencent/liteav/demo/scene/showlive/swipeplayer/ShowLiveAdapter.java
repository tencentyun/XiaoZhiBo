package com.tencent.liteav.demo.scene.showlive.swipeplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.liteav.basic.ImageLoader;
import com.tencent.liteav.demo.R;
import com.tencent.liteav.demo.scene.showlive.view.ShowAudienceFunctionView;
import com.tencent.liteav.demo.services.room.bean.RoomInfo;
import com.tencent.liteav.demo.utils.URLUtils;
import com.tencent.qcloud.tuicore.util.ToastUtil;
import com.tencent.qcloud.tuikit.tuiplayer.view.TUIPlayerView;
import com.tencent.qcloud.tuikit.tuiplayer.view.listener.TUIPlayerViewListener;

import java.util.List;

public class ShowLiveAdapter extends RecyclerView.Adapter<ShowLiveAdapter.RoomViewHolder> {
    private Context        mContext;
    private List<RoomInfo> mRoomInfoList;

    public ShowLiveAdapter(Context context, List<RoomInfo> roomInfoList) {
        mContext = context;
        mRoomInfoList = roomInfoList;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.app_show_live_recycler_item, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ShowLiveAdapter.RoomViewHolder holder, int position) {
        if (position != getItemCount() - 1) {
            holder.setIsRecyclable(false);
        }
    }

    @Override
    public int getItemCount() {
        return mRoomInfoList.size();
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder {
        TUIPlayerView            mTUIPlayerView;
        ShowAudienceFunctionView mShowAudienceFunctionView;


        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            mTUIPlayerView = itemView.findViewById(R.id.recycler_item_player_view);
            mShowAudienceFunctionView = itemView.findViewById(R.id.recycler_item_function_view);
            mShowAudienceFunctionView.setTUIPlayerView(mTUIPlayerView);
            mShowAudienceFunctionView.setVisibility(View.GONE);
        }
    }
}



package com.tencent.liteav.demo.scene.showlive.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.liteav.basic.ImageLoader;
import com.tencent.liteav.demo.R;
import com.tencent.liteav.demo.services.room.bean.RoomInfo;

import java.util.List;


public class HourRankListAdapter extends RecyclerView.Adapter<HourRankListAdapter.ViewHolder> {
    private Context        mContext;
    private List<RoomInfo> mRoomInfoList;

    public HourRankListAdapter(Context context, List<RoomInfo> list) {
        mContext = context;
        mRoomInfoList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.app_item_dialog_recycler, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position == 0) {
            holder.mTextRanking.setTextColor(mContext.getResources().getColor(R.color.app_ranking_text_first));
        } else if (position == 1) {
            holder.mTextRanking.setTextColor(mContext.getResources().getColor(R.color.app_ranking_text_second));
        } else if (position == 2) {
            holder.mTextRanking.setTextColor(mContext.getResources().getColor(R.color.app_ranking_text_third));
        } else {
            holder.mTextRanking.setTextColor(mContext.getResources().getColor(R.color.black));
        }
        RoomInfo info = mRoomInfoList.get(position);
        holder.mTextRanking.setText(String.valueOf(position + 1));
        ImageLoader.loadImage(mContext, holder.mImageAudienceAvatar, info.coverUrl, R.drawable.app_bg_cover);
        holder.mTextAudienceName.setText(info.roomName);
        String totalJoined;
        if (info.totalJoined >= 10000) {
            totalJoined = info.totalJoined / 10000f + "w";
        } else if (info.totalJoined >= 1000) {
            totalJoined = info.totalJoined / 1000f + "k";
        } else {
            totalJoined = info.totalJoined + "";
        }
        holder.mTextValue.setText(totalJoined);
    }

    @Override
    public int getItemCount() {
        return mRoomInfoList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView  mTextRanking;
        ImageView mImageAudienceAvatar;
        TextView  mTextAudienceName;
        TextView  mTextValue;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextRanking = itemView.findViewById(R.id.tv_ranking);
            mImageAudienceAvatar = itemView.findViewById(R.id.iv_item_avatar);
            mTextAudienceName = itemView.findViewById(R.id.tv_item_name);
            mTextValue = itemView.findViewById(R.id.tv_item_value);
        }
    }
}

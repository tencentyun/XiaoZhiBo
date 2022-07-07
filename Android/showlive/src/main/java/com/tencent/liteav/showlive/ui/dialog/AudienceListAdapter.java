package com.tencent.liteav.showlive.ui.dialog;

import static android.view.View.GONE;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.liteav.basic.ImageLoader;
import com.tencent.liteav.showlive.R;
import com.tencent.liteav.showlive.model.services.room.bean.AudienceInfo;

import java.util.List;

public class AudienceListAdapter extends RecyclerView.Adapter<AudienceListAdapter.ViewHolder> {
    private Context            mContext;
    private List<AudienceInfo> mAudienceInfoList;

    public AudienceListAdapter(Context context, List<AudienceInfo> list) {
        mContext = context;
        mAudienceInfoList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.showlive_item_dialog_recycler, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AudienceListAdapter.ViewHolder holder, int position) {
        if (position == 0) {
            holder.mTextRanking.setTextColor(mContext.getResources().getColor(R.color.showlive_ranking_text_first));
        } else if (position == 1) {
            holder.mTextRanking.setTextColor(mContext.getResources().getColor(R.color.showlive_ranking_text_second));
        } else if (position == 2) {
            holder.mTextRanking.setTextColor(mContext.getResources().getColor(R.color.showlive_ranking_text_third));
        } else {
            holder.mTextRanking.setTextColor(mContext.getResources().getColor(R.color.black));
        }
        AudienceInfo info = mAudienceInfoList.get(position);
        holder.mTextRanking.setText(String.valueOf(position + 1));
        ImageLoader.loadImage(mContext, holder.mImageAudienceAvatar, info.getAvatar(), R.drawable.showlive_bg_cover);
        holder.mTextAudienceName.setText(info.getName());
    }

    @Override
    public int getItemCount() {
        return mAudienceInfoList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView  mTextRanking;
        ImageView mImageAudienceAvatar;
        TextView  mTextAudienceName;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextRanking = itemView.findViewById(R.id.tv_ranking);
            mImageAudienceAvatar = itemView.findViewById(R.id.iv_item_avatar);
            mTextAudienceName = itemView.findViewById(R.id.tv_item_name);
            itemView.findViewById(R.id.tv_item_value).setVisibility(GONE);
        }
    }
}

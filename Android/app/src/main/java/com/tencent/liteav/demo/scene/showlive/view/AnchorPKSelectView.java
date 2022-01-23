package com.tencent.liteav.demo.scene.showlive.view;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.liteav.basic.ImageLoader;
import com.tencent.liteav.demo.R;
import com.tencent.liteav.demo.common.view.SpaceDecoration;
import com.tencent.liteav.demo.services.RoomService;
import com.tencent.liteav.demo.services.room.bean.RoomInfo;
import com.tencent.liteav.demo.services.room.callback.RoomInfoCallback;
import com.tencent.liteav.demo.services.room.http.impl.HttpRoomManager;

import java.util.ArrayList;
import java.util.List;

import static com.tencent.liteav.demo.services.room.http.impl.HttpRoomManager.TYPE_MLVB_SHOW_LIVE;

public class AnchorPKSelectView extends RelativeLayout {
    private Context              mContext;
    private RecyclerView         mPusherListRv;
    private List<RoomInfo>       mRoomInfos;
    private RoomListAdapter      mRoomListAdapter;
    private OnPKSelectedCallback mOnPKSelectedCallback;
    private TextView             mPusherTagTv;
    private TextView             mTextCancel;
    private String               mSelfRoomId;

    public AnchorPKSelectView(Context context) {
        this(context, null);
    }

    public AnchorPKSelectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

    private void initView(Context context) {
        mContext = context;
        setBackgroundColor(Color.TRANSPARENT);
        inflate(context, R.layout.app_view_pk_select, this);
        mPusherListRv = (RecyclerView) findViewById(R.id.rv_anchor_list);
        mRoomInfos = new ArrayList<>();
        mRoomListAdapter = new RoomListAdapter(mContext, mRoomInfos, new RoomListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (mRoomInfos == null || mOnPKSelectedCallback == null) {
                    return;
                }
                mOnPKSelectedCallback.onSelected(mRoomInfos.get(position));
            }
        });
        mPusherListRv.setLayoutManager(new LinearLayoutManager(mContext));
        mPusherListRv.addItemDecoration(
                new SpaceDecoration(getResources().getDimensionPixelOffset(R.dimen.app_small_image_left_margin),
                        1));
        mPusherListRv.setAdapter(mRoomListAdapter);

        mPusherTagTv = (TextView) findViewById(R.id.tv_pusher_tag);

        mTextCancel = (TextView) findViewById(R.id.tv_cancel);
        mTextCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setVisibility(GONE);
                mOnPKSelectedCallback.onCancel();
            }
        });
    }

    public void setSelfRoomId(String roomId) {
        mSelfRoomId = roomId;
    }

    public void setOnPKSelectedCallback(OnPKSelectedCallback onPKSelectedCallback) {
        mOnPKSelectedCallback = onPKSelectedCallback;
    }

    public void refreshView() {
        mPusherTagTv.setText(getResources().getString(R.string.app_loading));
        RoomService.getInstance(getContext()).getRoomList(TYPE_MLVB_SHOW_LIVE,
                HttpRoomManager.RoomOrderType.CREATE_UTC, new RoomInfoCallback() {
                    @Override
                    public void onCallback(int code, String msg, List<RoomInfo> list) {
                        mPusherTagTv.setText(getResources().getString(R.string.app_pk_invite));
                        mRoomInfos.clear();
                        List<RoomInfo> tempList = new ArrayList<>();
                        if (list != null && list.size() > 0 && !TextUtils.isEmpty(mSelfRoomId)) {
                            for (RoomInfo info : list) {
                                if (!mSelfRoomId.equals(info.roomId)) {
                                    tempList.add(info);
                                }
                            }
                        }
                        mRoomInfos.addAll(tempList);
                        mRoomListAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == VISIBLE) {
            refreshView();
        }
    }

    public interface OnPKSelectedCallback {
        void onSelected(RoomInfo roomInfo);

        void onCancel();
    }


    public static class RoomListAdapter extends
            RecyclerView.Adapter<RoomListAdapter.ViewHolder> {

        private static final String TAG = RoomListAdapter.class.getSimpleName();

        private Context             context;
        private List<RoomInfo>      list;
        private OnItemClickListener onItemClickListener;

        public RoomListAdapter(Context context, List<RoomInfo> list,
                               OnItemClickListener onItemClickListener) {
            this.context = context;
            this.list = list;
            this.onItemClickListener = onItemClickListener;
        }


        static class ViewHolder extends RecyclerView.ViewHolder {
            private TextView  mUserNameTv;
            private TextView  mRoomNameTv;
            private Button    mButtonInvite;
            private ImageView mImageAvatar;

            ViewHolder(View itemView) {
                super(itemView);
                initView(itemView);
            }

            private void initView(@NonNull final View itemView) {
                mUserNameTv = itemView.findViewById(R.id.tv_user_name);
                mRoomNameTv = itemView.findViewById(R.id.tv_room_name);
                mButtonInvite = itemView.findViewById(R.id.btn_invite_anchor);
                mImageAvatar = itemView.findViewById(R.id.iv_avatar);
            }

            public void bind(Context context, final RoomInfo model,
                             final OnItemClickListener listener) {
                if (model == null) {
                    return;
                }
                if (!TextUtils.isEmpty(model.roomName)) {
                    mRoomNameTv.setText(model.roomName);
                }
                if (TextUtils.isEmpty(model.ownerName)) {
                    mUserNameTv.setText(model.ownerId);
                } else {
                    mUserNameTv.setText(model.ownerName);
                }

                ImageLoader.loadImage(context, mImageAvatar, model.coverUrl, R.drawable.app_bg_cover);
                mButtonInvite.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onItemClick(getLayoutPosition());
                    }
                });
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            View view = inflater.inflate(R.layout.app_item_select_pusher, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            RoomInfo item = list.get(position);
            holder.bind(context, item, onItemClickListener);
        }


        @Override
        public int getItemCount() {
            return list.size();
        }

        public interface OnItemClickListener {
            void onItemClick(int position);
        }
    }
}

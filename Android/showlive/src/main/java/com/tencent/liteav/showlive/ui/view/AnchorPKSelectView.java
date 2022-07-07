package com.tencent.liteav.showlive.ui.view;

import static com.tencent.liteav.showlive.model.services.room.http.impl.HttpRoomManager.TYPE_MLVB_SHOW_LIVE;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.liteav.basic.ImageLoader;
import com.tencent.liteav.basic.UserModelManager;
import com.tencent.liteav.showlive.R;
import com.tencent.liteav.showlive.model.services.RoomService;
import com.tencent.liteav.showlive.model.services.room.bean.RoomInfo;
import com.tencent.liteav.showlive.model.services.room.callback.RoomInfoCallback;
import com.tencent.liteav.showlive.model.services.room.http.impl.HttpRoomManager;
import com.tencent.liteav.showlive.model.services.room.im.listener.RoomInfoListCallback;
import com.tencent.qcloud.tuicore.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

public class AnchorPKSelectView extends RelativeLayout {
    private Context              mContext;
    private RecyclerView    mPusherListRv;
    private List<RoomInfo>  mRoomInfos;
    private RoomListAdapter mRoomListAdapter;
    private OnPKSelectedCallback mOnPKSelectedCallback;
    private TextView             mPusherTagTv;
    private TextView             mTextCancel;
    private String               mSelfRoomId;
    private EditText             mEditRoomId;
    private TextView             mTextEnterRoom;
    private ConstraintLayout     mPkByIdLayout;

    private final TextWatcher mEditTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mTextEnterRoom.setEnabled(!TextUtils.isEmpty(mEditRoomId.getText().toString()));
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

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
        inflate(context, R.layout.showlive_view_pk_select, this);
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
                new SpaceDecoration(getResources().getDimensionPixelOffset(R.dimen.showlive_small_image_left_margin),
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
        mPkByIdLayout = findViewById(R.id.layout_invite_pk_by_id);
        mEditRoomId = findViewById(R.id.et_input_room_id);
        mTextEnterRoom = findViewById(R.id.tv_enter_room_by_id);
        mEditRoomId.addTextChangedListener(mEditTextWatcher);
        if (UserModelManager.getInstance().haveBackstage()) {
            mPusherListRv.setVisibility(VISIBLE);
        } else {
            mPkByIdLayout.setVisibility(VISIBLE);
        }
        mTextEnterRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RoomService.getInstance(getContext()).getGroupInfo(mEditRoomId.getText().toString().trim(),
                        new RoomInfoListCallback() {
                            @Override
                            public void onCallback(int code, String msg, List<RoomInfo> list) {
                                if (getContext() == null || ((Activity) getContext()).isFinishing()) {
                                    return;
                                }
                                if (code == 0 && list.size() != 0) {
                                    mOnPKSelectedCallback.onSelected(list.get(0));
                                } else {
                                    ToastUtil.toastShortMessage(msg);
                                }
                            }
                        });
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
        mPusherTagTv.setText(getResources().getString(R.string.showlive_loading));
        RoomService.getInstance(getContext()).getRoomList(TYPE_MLVB_SHOW_LIVE,
                HttpRoomManager.RoomOrderType.CREATE_UTC, new RoomInfoCallback() {
                    @Override
                    public void onCallback(int code, String msg, List<RoomInfo> list) {
                        mPusherTagTv.setText(getResources().getString(R.string.showlive_pk_invite));
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
            if (UserModelManager.getInstance().haveBackstage()) {
                refreshView();
            } else {
                mPusherTagTv.setText(getResources().getString(R.string.showlive_pk_invite));
            }
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

                ImageLoader.loadImage(context, mImageAvatar, model.coverUrl, R.drawable.showlive_bg_cover);
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

            View view = inflater.inflate(R.layout.showlive_item_select_pusher, parent, false);

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

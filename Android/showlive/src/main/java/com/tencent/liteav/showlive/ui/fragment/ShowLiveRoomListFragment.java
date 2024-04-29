package com.tencent.liteav.showlive.ui.fragment;

import static com.tencent.liteav.showlive.model.services.room.http.impl.HttpRoomManager.TYPE_MLVB_SHOW_LIVE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.tencent.liteav.basic.ImageLoader;
import com.tencent.liteav.basic.RTCubeUtils;
import com.tencent.liteav.basic.UserModelManager;
import com.tencent.liteav.showlive.R;
import com.tencent.liteav.showlive.model.services.RoomService;
import com.tencent.liteav.showlive.model.services.room.bean.RoomInfo;
import com.tencent.liteav.showlive.model.services.room.callback.RoomInfoCallback;
import com.tencent.liteav.showlive.model.services.room.http.impl.HttpRoomManager;
import com.tencent.liteav.showlive.model.services.room.im.listener.RoomInfoListCallback;
import com.tencent.liteav.showlive.ui.ShowLiveAnchorActivity;
import com.tencent.liteav.showlive.ui.ShowLiveAudienceActivity;
import com.tencent.liteav.showlive.ui.common.TCConstants;
import com.tencent.liteav.showlive.ui.floatwindow.FloatWindow;
import com.tencent.liteav.showlive.ui.view.RoundCornerImageView;
import com.tencent.liteav.showlive.ui.view.SpaceDecoration;
import com.tencent.qcloud.tuicore.util.SPUtils;
import com.tencent.qcloud.tuicore.util.ToastUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 秀场直播 - 列表页面
 */
public class ShowLiveRoomListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String SP_VERIFY     = "sp_verify";
    private static final String VERIFY_STATUS = "sp_verify";

    private RecyclerView       mRecyclerRoomList;                  //显示当前直播间列表的滑动控件
    private TextView           mTextRoomListEmpty;                 //显示直播间列表为空时的提示消息
    private View               mButtonCreateRoom;                  //用来创建直播间的按钮
    private SwipeRefreshLayout mLayoutSwipeRefresh;                //一个滑动刷新的组件，用来更新直播间列表
    private RoomListAdapter    mRoomListViewAdapter;               //mRecyclerRoomList控件的适配器
    private boolean            mIsUseCDNPlay = false;              //用来表示当前是否CDN模式（区别于TRTC模式）
    private List<RoomInfo>     mRoomInfoList = new ArrayList<>();  //保存从网络侧加载到的直播间信息
    private EditText           mEditRoomId;
    private TextView           mTextEnterRoom;
    private ConstraintLayout   mLayoutEnterById;

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

    public static ShowLiveRoomListFragment newInstance() {
        Bundle args = new Bundle();
        ShowLiveRoomListFragment fragment = new ShowLiveRoomListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.showlive_fragment_live_room_list, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getRoomList();
    }

    private void initView(@NonNull final View itemView) {
        mRecyclerRoomList = itemView.findViewById(R.id.rv_room_list);
        mTextRoomListEmpty = itemView.findViewById(R.id.tv_list_empty);
        mEditRoomId = itemView.findViewById(R.id.et_input_room_id);
        mTextEnterRoom = itemView.findViewById(R.id.tv_enter_room_by_id);
        mLayoutEnterById = itemView.findViewById(R.id.layout_enter_room_by_id);

        mButtonCreateRoom = itemView.findViewById(R.id.container_create_room);
        mButtonCreateRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FloatWindow.mIsShowing) {
                    FloatWindow.getInstance().destroy();
                }
                createRoom();
            }
        });
        mEditRoomId.addTextChangedListener(mEditTextWatcher);
        mTextEnterRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RoomService.getInstance().getGroupInfo(mEditRoomId.getText().toString().trim(),
                        new RoomInfoListCallback() {
                            @Override
                            public void onCallback(int code, String msg, List<RoomInfo> list) {
                                if (getContext() == null || ((Activity) getContext()).isFinishing()) {
                                    return;
                                }
                                if (code == 0 && list.size() != 0) {
                                    enterRoom(list.get(0));
                                } else {
                                    ToastUtil.toastShortMessage(msg);
                                }
                            }
                        });
            }
        });

        mLayoutSwipeRefresh = itemView.findViewById(R.id.swipe_refresh_layout_list);
        if (UserModelManager.getInstance().haveBackstage()) {
            mLayoutSwipeRefresh.setVisibility(View.VISIBLE);
        } else {
            mLayoutEnterById.setVisibility(View.VISIBLE);
        }
        mLayoutSwipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        mLayoutSwipeRefresh.setOnRefreshListener(this);

        mRoomListViewAdapter = new RoomListAdapter(getContext(), mRoomInfoList,
                new RoomListAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        RoomInfo info = mRoomInfoList
                                .get(position);
                        if (FloatWindow.mIsShowing) {
                            FloatWindow.getInstance().destroy();
                        }
                        enterRoom(info);
                    }
                });
        mRecyclerRoomList.setLayoutManager(new GridLayoutManager(getContext(), 2));
        mRecyclerRoomList.setAdapter(mRoomListViewAdapter);
        mRecyclerRoomList.addItemDecoration(
                new SpaceDecoration(getResources().getDimensionPixelOffset(R.dimen.showlive_small_image_left_margin),
                        2));
        mRoomListViewAdapter.notifyDataSetChanged();
        mIsUseCDNPlay = SPUtils.getInstance().getBoolean(TCConstants.USE_CDN_PLAY, false);
    }

    private void createRoom() {
        if (!RTCubeUtils.isRTCubeApp(getContext())
                || !Locale.CHINA.equals(getResources().getConfiguration().locale)
                || SPUtils.getInstance(SP_VERIFY).getBoolean(VERIFY_STATUS, false)) {
            Intent intent = new Intent(getContext(), ShowLiveAnchorActivity.class);
            startActivity(intent);
        } else {
            try {
                Class clz = Class.forName("com.tencent.liteav.privacy.util.RTCubeAppLegalUtils");
                Method method = clz.getDeclaredMethod("showRealNameVerifyDialog", Context.class);
                method.invoke(null, getContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void refreshView() {
        mTextRoomListEmpty.setVisibility(mRoomInfoList.size() == 0 ? View.VISIBLE : View.GONE);
        mRecyclerRoomList.setVisibility(mRoomInfoList.size() == 0 ? View.GONE : View.VISIBLE);
        mRoomListViewAdapter.notifyDataSetChanged();
    }

    private void enterRoom(RoomInfo info) {
        Intent intent = new Intent(getActivity(), ShowLiveAudienceActivity.class);
        intent.putExtra(TCConstants.ROOM_TITLE, info.roomName);
        intent.putExtra(TCConstants.GROUP_ID, Integer.valueOf(info.roomId));
        intent.putExtra(TCConstants.USE_CDN_PLAY, mIsUseCDNPlay);
        intent.putExtra(TCConstants.PUSHER_ID, info.ownerId);
        intent.putExtra(TCConstants.PUSHER_NAME, info.roomName);
        intent.putExtra(TCConstants.COVER_PIC, info.coverUrl);
        intent.putExtra(TCConstants.PUSHER_AVATAR, info.coverUrl);
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        getRoomList();
    }

    /**
     * 刷新直播列表
     */
    private void getRoomList() {
        mLayoutSwipeRefresh.setRefreshing(true);
        // 从后台获取房间列表
        RoomService.getInstance().getRoomList(TYPE_MLVB_SHOW_LIVE,
                HttpRoomManager.RoomOrderType.CREATE_UTC, new RoomInfoCallback() {
                    @Override
                    public void onCallback(int code, String msg, List<RoomInfo> list) {
                        if (getActivity() == null || getActivity().isFinishing()) {
                            return;
                        }
                        if (code == 0) {
                            mRoomInfoList.clear();
                            mRoomInfoList.addAll(list);
                            mRoomListViewAdapter.notifyDataSetChanged();
                        } else {
                            ToastUtil.toastLongMessage(getString(R.string.showlive_toast_obtain_list_failed, msg));
                        }
                        mLayoutSwipeRefresh.setRefreshing(false);
                        refreshView();
                    }
                });
    }

    /**
     * 用于展示房间列表的item
     */
    public static class RoomListAdapter extends
            RecyclerView.Adapter<RoomListAdapter.ViewHolder> {

        private Context             mContext;
        private List<RoomInfo>      mList;
        private OnItemClickListener mOnItemClickListener;

        public RoomListAdapter(Context context, List<RoomInfo> list,
                               OnItemClickListener onItemClickListener) {
            this.mContext = context;
            this.mList = list;
            this.mOnItemClickListener = onItemClickListener;
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            private RoundCornerImageView mAnchorCoverImg;
            private TextView             mAnchorNameTv;
            private TextView             mRoomNameTv;
            private TextView             mMembersLive;

            public ViewHolder(View itemView) {
                super(itemView);
                initView(itemView);
            }

            private void initView(@NonNull final View itemView) {
                mAnchorCoverImg = (RoundCornerImageView) itemView.findViewById(R.id.img_anchor_cover);
                mAnchorNameTv = (TextView) itemView.findViewById(R.id.tv_anchor_name);
                mRoomNameTv = (TextView) itemView.findViewById(R.id.tv_room_name);
                mMembersLive = (TextView) itemView.findViewById(R.id.live_members);
            }

            public void bind(Context context, final RoomInfo model, final OnItemClickListener listener) {
                if (model == null) {
                    return;
                }
                ImageLoader.loadImage(context, mAnchorCoverImg, model.coverUrl, R.drawable.showlive_bg_cover);
                mAnchorNameTv.setText(TextUtils.isEmpty(model.ownerName) ? model.roomId : model.ownerName);
                mRoomNameTv.setText(model.roomName);
                mMembersLive.setText(context.getString(R.string.showlive_audience_members, model.memberCount));
                itemView.setOnClickListener(new View.OnClickListener() {
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
            View view = inflater.inflate(R.layout.showlive_item_room_list, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            RoomInfo item = mList.get(position);
            holder.bind(mContext, item, mOnItemClickListener);
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        public interface OnItemClickListener {
            void onItemClick(int position);
        }
    }
}

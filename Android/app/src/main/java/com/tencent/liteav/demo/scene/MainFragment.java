package com.tencent.liteav.demo.scene;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.tencent.imsdk.v2.V2TIMCallback;
import com.tencent.liteav.basic.UserModelManager;
import com.tencent.liteav.demo.R;
import com.tencent.liteav.demo.app.KeepAliveService;
import com.tencent.liteav.demo.common.view.ConfirmDialogFragment;
import com.tencent.liteav.demo.common.view.RoundCornerImageView;
import com.tencent.liteav.demo.scene.shoppinglive.ShoppingLiveEntranceActivity;
import com.tencent.liteav.demo.scene.showlive.ShowLiveEntranceActivity;
import com.tencent.liteav.demo.scene.showlive.floatwindow.FloatWindow;
import com.tencent.liteav.login.model.ProfileManager;
import com.tencent.liteav.login.ui.LoginActivity;
import com.tencent.liteav.login.ui.LoginWithoutServerActivity;
import com.tencent.qcloud.tuicore.TUILogin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MainFragment extends Fragment {

    private static final String TAG = MainFragment.class.getName();

    private TextView                mMainTitle;
    private List<LiveItemEntity>    mLiveItemEntityList;
    private RecyclerView            mRvList;
    private TRTCRecyclerViewAdapter mTRTCRecyclerViewAdapter;
    private TextView                mLogoutTv;
    private ConfirmDialogFragment   mAlertDialog;
    private Context                 mContext;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.app_fragment_trtc_main, container, false);
        mAlertDialog = new ConfirmDialogFragment();
        mMainTitle = rootView.findViewById(R.id.main_title);
        mMainTitle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        File logFile = getLogFile();
                        if (logFile != null) {
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("application/octet-stream");
                            //intent.setPackage("com.tencent.mobileqq");
                            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(logFile));
                            startActivity(Intent.createChooser(intent, getString(R.string.app_title_share_log)));
                        }
                    }
                });
                return false;
            }
        });
        mRvList = rootView.findViewById(R.id.main_recycler_view);
        mLiveItemEntityList = createTRTCItems();
        mTRTCRecyclerViewAdapter = new TRTCRecyclerViewAdapter(mContext,
                mLiveItemEntityList, new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                LiveItemEntity entity = mLiveItemEntityList.get(position);
                if (entity.mTargetClass == ShoppingLiveEntranceActivity.class) {
                    ToastUtils.showShort(R.string.app_more_function_please_wait);
                    return;
                }
                Intent intent = new Intent(mContext, entity.mTargetClass);
                intent.putExtra("TITLE", entity.mTitle);
                intent.putExtra("TYPE", entity.mType);
                MainFragment.this.startActivity(intent);
            }
        });
        mRvList.setLayoutManager(new GridLayoutManager(mContext, 1));
        mRvList.setAdapter(mTRTCRecyclerViewAdapter);
        mLogoutTv = rootView.findViewById(R.id.tv_login_out);
        mLogoutTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();
            }
        });
        return rootView;
    }

    private void showLogoutDialog() {
        if (mAlertDialog.isAdded()) {
            mAlertDialog.dismiss();
        }
        mAlertDialog.setMessage(mContext.getString(R.string.app_dialog_log_out));
        mAlertDialog.setNegativeClickListener(new ConfirmDialogFragment.NegativeClickListener() {
            @Override
            public void onClick() {
                mAlertDialog.dismiss();
            }
        });
        mAlertDialog.setPositiveClickListener(new ConfirmDialogFragment.PositiveClickListener() {
            @Override
            public void onClick() {
                mAlertDialog.dismiss();
                if (!UserModelManager.getInstance().haveBackstage()) {
                    TUILogin.logout(new V2TIMCallback() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "logout success ");
                            KeepAliveService.stop(mContext);
                            if (FloatWindow.mIsShowing) {
                                FloatWindow.getInstance().destroy();
                            }
                            startLoginWithoutServerActivity();
                        }

                        @Override
                        public void onError(int code, String msg) {
                            Log.d(TAG, "logout fail : code = " + code + " , msg = " + msg);
                        }
                    });
                }
                ProfileManager.getInstance().logout(new ProfileManager.ActionCallback() {
                    @Override
                    public void onSuccess() {
                        KeepAliveService.stop(mContext);
                        if (FloatWindow.mIsShowing) {
                            FloatWindow.getInstance().destroy();
                        }
                        startLoginActivity();
                    }

                    @Override
                    public void onFailed(int code, String msg) {
                    }
                });
            }
        });
        mAlertDialog.show(getActivity().getFragmentManager(), "confirm_fragment");
    }

    private void startLoginActivity() {
        Intent intent = new Intent(mContext, LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    private void startLoginWithoutServerActivity() {
        Intent intent = new Intent(mContext, LoginWithoutServerActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    protected List<LiveItemEntity> createTRTCItems() {
        List<LiveItemEntity> list = new ArrayList<>();
        list.add(new LiveItemEntity(getString(R.string.app_item_live_show),
                getString(R.string.app_tv_live_show_tips),
                R.drawable.app_live_show, 1, ShowLiveEntranceActivity.class));
        list.add(new LiveItemEntity(getString(R.string.app_item_live_shopping),
                getString(R.string.app_tv_live_shopping_tips),
                R.drawable.app_live_shopping, 1, ShoppingLiveEntranceActivity.class));
        return list;
    }

    private File getLogFile() {
        String path = mContext.getExternalFilesDir(null).getAbsolutePath() + "/log/liteav";
        List<String> logs = new ArrayList<>();
        File directory = new File(path);
        if (directory != null && directory.exists() && directory.isDirectory()) {
            long lastModify = 0;
            File[] files = directory.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    if (file.getName().endsWith("xlog")) {
                        logs.add(file.getAbsolutePath());
                    }
                }
            }
        }
        String zipPath = path + "/liteavLog.zip";
        return zip(logs, zipPath);
    }

    private File zip(List<String> files, String zipFileName) {
        File zipFile = new File(zipFileName);
        zipFile.deleteOnExit();
        InputStream is = null;
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(new FileOutputStream(zipFile));
            zos.setComment("LiteAV log");
            for (String path : files) {
                File file = new File(path);
                try {
                    if (file.length() == 0 || file.length() > 8 * 1024 * 1024) {
                        continue;
                    }
                    is = new FileInputStream(file);
                    zos.putNextEntry(new ZipEntry(file.getName()));
                    byte[] buffer = new byte[8 * 1024];
                    int length = 0;
                    while ((length = is.read(buffer)) != -1) {
                        zos.write(buffer, 0, length);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        is.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (FileNotFoundException e) {
            Log.w(TAG, "zip log error");
            zipFile = null;
        } finally {
            try {
                zos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return zipFile;
    }

    public class LiveItemEntity {
        public String mTitle;
        public String mContent;
        public int    mIconId;
        public Class  mTargetClass;
        public int    mType;

        public LiveItemEntity(String title, String content, int iconId, int type, Class targetClass) {
            mTitle = title;
            mContent = content;
            mIconId = iconId;
            mTargetClass = targetClass;
            mType = type;
        }
    }


    public class TRTCRecyclerViewAdapter extends
            RecyclerView.Adapter<TRTCRecyclerViewAdapter.ViewHolder> {

        private Context              context;
        private List<LiveItemEntity> list;
        private OnItemClickListener  onItemClickListener;

        public TRTCRecyclerViewAdapter(Context context, List<LiveItemEntity> list,
                                       OnItemClickListener onItemClickListener) {
            this.context = context;
            this.list = list;
            this.onItemClickListener = onItemClickListener;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private ImageView            mItemImg;
            private TextView             mTitleTv;
            private TextView             mDescription;
            private ConstraintLayout     mClItem;
            private View                 mBottomLine;
            private RoundCornerImageView mImageBigIcon;

            public ViewHolder(View itemView) {
                super(itemView);
                initView(itemView);
            }

            public void bind(final LiveItemEntity model,
                             final OnItemClickListener listener) {
                mTitleTv.setText(model.mTitle);
                mDescription.setText(model.mContent);
                if (model.mIconId != 0) {
                    mItemImg.setImageResource(model.mIconId);
                }

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onItemClick(getLayoutPosition());
                    }
                });
                mBottomLine.setVisibility(((getLayoutPosition() == list.size() - 1) ? View.VISIBLE : View.GONE));
            }

            private void initView(final View itemView) {
                mItemImg = itemView.findViewById(R.id.img_item);
                mTitleTv = itemView.findViewById(R.id.tv_title);
                mClItem = itemView.findViewById(R.id.item_cl);
                mDescription = itemView.findViewById(R.id.tv_description);
                mBottomLine = itemView.findViewById(R.id.bottom_line);
                mImageBigIcon = itemView.findViewById(R.id.img_big_bg);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.app_module_trtc_entry_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            LiveItemEntity item = list.get(position);
            holder.bind(item, onItemClickListener);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

    }


    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}

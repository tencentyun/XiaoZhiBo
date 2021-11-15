package com.tencent.qcloud.tuikit.tuibarrage.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.tencent.qcloud.tuicore.TUILogin;
import com.tencent.qcloud.tuikit.tuibarrage.R;
import com.tencent.qcloud.tuikit.tuibarrage.model.TUIBarrageConstants;
import com.tencent.qcloud.tuikit.tuibarrage.model.TUIBarrageModel;
import com.tencent.qcloud.tuikit.tuibarrage.presenter.ITUIBarragePresenter;
import com.tencent.qcloud.tuikit.tuibarrage.presenter.TUIBarrageCallBack;
import com.tencent.qcloud.tuikit.tuibarrage.presenter.TUIBarragePresenter;

/**
 * 弹幕展开按钮
 */
public class TUIBarrageButton extends FrameLayout implements ITUIBarrageButton {
    private static final String TAG = "TUIBarrageButton";

    private Context              mContext;
    private String               mGroupId;         //用户组ID(房间ID)
    private TUIBarrageSendView   mBarrageSendView; //弹幕发送组件,配合输入法弹出框输入弹幕内容,并发送
    private ITUIBarrageListener  mBarrageListener;
    private ITUIBarragePresenter mPresenter;

    public TUIBarrageButton(Context context) {
        super(context);
    }

    public TUIBarrageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TUIBarrageButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public TUIBarrageButton(Context context, String groupId) {
        this(context);
        this.mContext = context;
        this.mGroupId = groupId;
        initView(context);
        initPresenter();
    }

    private void initPresenter() {
        mPresenter = TUIBarragePresenter.getInstance();
        mPresenter.init(mContext, mGroupId);
        mPresenter.initSendView(this);
    }

    public void setBarrageListener(ITUIBarrageListener barrageListener) {
        mBarrageListener = barrageListener;
    }

    private void initView(final Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.tuibarrage_view_send, this);
        mBarrageSendView = new TUIBarrageSendView(context);
        findViewById(R.id.iv_linkto_send_dialog).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBarrageSendView.isShowing()) {
                    showSendDialog();
                }
            }
        });
        mBarrageSendView.setOnTextSendListener(new TUIBarrageSendView.OnTextSendListener() {
            @Override
            public void onTextSend(String msg) {
                TUIBarrageModel model = createBarrageModel(msg);
                sendBarrage(model);
            }
        });
    }

    //弹幕发送弹框显示,宽度自适应屏幕
    private void showSendDialog() {
        Window window = mBarrageSendView.getWindow();
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);
        mBarrageSendView.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        mBarrageSendView.show();
    }

    @Override
    public void sendBarrage(final TUIBarrageModel model) {
        if (model == null) {
            return;
        }
        
        if (mPresenter == null) {
            initPresenter();
        }

        mPresenter.sendBarrage(model, new TUIBarrageCallBack.BarrageSendCallBack() {
            @Override
            public void onSuccess(int code, String msg, TUIBarrageModel model) {
                if (mBarrageListener != null) {
                    mBarrageListener.onSuccess(code, msg, model);
                }
            }

            @Override
            public void onFailed(int code, String msg) {
                if (mBarrageListener != null) {
                    mBarrageListener.onFailed(code, msg);
                }
            }
        });
    }

    private TUIBarrageModel createBarrageModel(String message) {
        TUIBarrageModel model = new TUIBarrageModel();
        model.message = message;
        model.extInfo.put(TUIBarrageConstants.KEY_USER_ID, TUILogin.getUserId());
        model.extInfo.put(TUIBarrageConstants.KEY_USER_NAME, TUILogin.getNickName());
        model.extInfo.put(TUIBarrageConstants.KEY_USER_AVATAR, TUILogin.getFaceUrl());
        return model;
    }
}

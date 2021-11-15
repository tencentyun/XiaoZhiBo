package com.tencent.qcloud.tuikit.tuipusher.view.custom;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.qcloud.tuikit.tuipusher.R;

public class CountDownView extends RelativeLayout {
    private static final int START_COUNTING = 1;
    private static final int COUNT_NUMBER   = 3;

    private TextView          mTextSecond;
    public  View              mViewRoot;
    private MyHandler         mHandler = new MyHandler();
    private OnTimeEndListener mListener;

    public void setListener(OnTimeEndListener listener) {
        mListener = listener;
    }

    public CountDownView(Context context) {
        this(context, null);
    }

    public CountDownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mViewRoot = LayoutInflater.from(getContext()).inflate(R.layout.tuipusher_count_down_view, this, true);
        mTextSecond = mViewRoot.findViewById(R.id.tv_count_down);
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case START_COUNTING:
                    int count = (int) msg.obj;
                    mTextSecond.setText(count + "");
                    if (count > 0) {
                        Message lastMsg = obtainMessage();
                        lastMsg.what = START_COUNTING;
                        lastMsg.obj = count - 1;
                        if (count == 3) {
                            sendMessageDelayed(lastMsg, 0);
                        } else {
                            sendMessageDelayed(lastMsg, 1000);
                        }

                    } else {
                        mHandler.removeCallbacksAndMessages(null);
                        if (mListener != null) {
                            mListener.onTimeEnd();
                        }
                    }
                    break;

                default:
                    break;
            }
        }
    }

    public void start() {
        Message msg = mHandler.obtainMessage();
        msg.what = START_COUNTING;
        msg.obj = COUNT_NUMBER;
        mHandler.sendMessageDelayed(msg, 1000);
    }

    public interface OnTimeEndListener {
        void onTimeEnd();
    }
}

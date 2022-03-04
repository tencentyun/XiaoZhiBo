package com.tencent.qcloud.tuikit.tuibarrage.model;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.tencent.imsdk.v2.V2TIMGroupMemberInfo;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMMessage;
import com.tencent.imsdk.v2.V2TIMMessageManager;
import com.tencent.imsdk.v2.V2TIMSimpleMsgListener;
import com.tencent.imsdk.v2.V2TIMValueCallback;
import com.tencent.qcloud.tuikit.tuibarrage.presenter.ITUIBarragePresenter;
import com.tencent.qcloud.tuikit.tuibarrage.presenter.TUIBarrageCallBack;

import java.util.HashMap;

/**
 * IM服务类,调用IM SDK接口进行弹幕消息的发送和接收
 * sendBarrage              发送弹幕信息
 * onRecvGroupCustomMessage 接收弹幕信息
 */
public class TUIBarrageIMService {
    private static final String TAG = "TUIBarrageIMService";

    private SimpleListener       mSimpleListener;
    private ITUIBarragePresenter mPresenter;
    private String               mGroupId;

    public TUIBarrageIMService(ITUIBarragePresenter presenter) {
        initIMListener();
        mPresenter = presenter;
    }

    public void setGroupId(String groupId) {
        mGroupId = groupId;
    }

    //初始化IM监听
    private void initIMListener() {
        V2TIMMessageManager messageManager = V2TIMManager.getMessageManager();
        if (mSimpleListener == null) {
            mSimpleListener = new SimpleListener();
        }
        V2TIMManager.getInstance().addSimpleMsgListener(mSimpleListener);
    }

    public void unInitImListener() {
        V2TIMManager.getInstance().setGroupListener(null);
        V2TIMManager.getInstance().removeSimpleMsgListener(mSimpleListener);
    }

    public void sendBarrage(TUIBarrageModel model, final TUIBarrageCallBack.ActionCallback callback) {
        String data = getCusMsgJsonStr(model);
        if (TextUtils.isEmpty(data)) {
            Log.d(TAG, "sendBarrage data is empty");
            return;
        }
        Log.d(TAG, "sendBarrage: data = " + data.toString() + " , mGroupId = " + mGroupId);
        V2TIMManager.getInstance().sendGroupCustomMessage(data.getBytes(), mGroupId, V2TIMMessage.V2TIM_PRIORITY_HIGH,
                new V2TIMValueCallback<V2TIMMessage>() {
                    @Override
                    public void onSuccess(V2TIMMessage v2TIMMessage) {
                        if (callback != null) {
                            callback.onCallback(0, "send group message success.");
                            Log.e(TAG, "sendGroupCustomMessage success");
                        }
                    }

                    @Override
                    public void onError(int code, String desc) {
                        Log.e(TAG, "sendGroupCustomMessage error " + code + " errorMessage:" + desc);
                        if (callback != null) {
                            callback.onCallback(code, desc);
                        }
                    }
                });
    }

    private class SimpleListener extends V2TIMSimpleMsgListener {
        @Override
        public void onRecvGroupTextMessage(String msgID, String groupID, V2TIMGroupMemberInfo sender, String text) {
        }

        @Override
        public void onRecvGroupCustomMessage(String msgID, String groupID, V2TIMGroupMemberInfo sender, byte[] customData) {
            Log.d(TAG, "onRecvGroupCustomMessage: msgID = " + msgID + " , groupID = " + groupID +
                    " , mGroupId = " + mGroupId + " , sender = " + sender + " , customData = " + customData.toString());
            if (groupID == null || !groupID.equals(mGroupId)) {
                return;
            }
            String customStr = new String(customData);
            if (TextUtils.isEmpty(customStr)) {
                Log.d(TAG, "onRecvGroupCustomMessage customData is empty");
                return;
            }

            try {
                Gson gson = new Gson();
                TUIBarrageJson json = gson.fromJson(customStr, TUIBarrageJson.class);
                if (!TUIBarrageConstants.VALUE_VERSION.equals(json.getVersion())) {
                    Log.e(TAG, "protocol version is not match, ignore msg");
                }

                //如果不是弹幕消息,则不处理
                if (!TUIBarrageConstants.VALUE_BUSINESS_ID.equals(json.getBusinessID())) {
                    Log.d(TAG, "onRecvGroupCustomMessage error, this is not barrage msg");
                    return;
                }

                TUIBarrageJson.Data data = json.getData();
                TUIBarrageJson.Data.ExtInfo extInfo = data.getExtInfo();

                HashMap<String, String> userMap = new HashMap<>();
                userMap.put(TUIBarrageConstants.KEY_USER_ID, extInfo.getUserID());
                userMap.put(TUIBarrageConstants.KEY_USER_NAME, extInfo.getNickName());
                userMap.put(TUIBarrageConstants.KEY_USER_AVATAR, extInfo.getAvatarUrl());

                TUIBarrageModel model = new TUIBarrageModel();
                model.message = data.getMessage();
                model.extInfo = userMap;

                if (mPresenter != null) {
                    mPresenter.receiveBarrage(model);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //自定义弹幕发送数据
    public static String getCusMsgJsonStr(TUIBarrageModel model) {
        if (model == null) {
            return null;
        }
        TUIBarrageJson sendJson = new TUIBarrageJson();
        sendJson.setBusinessID(TUIBarrageConstants.VALUE_BUSINESS_ID);
        sendJson.setPlatform(TUIBarrageConstants.VALUE_PLATFORM);
        sendJson.setVersion(TUIBarrageConstants.VALUE_VERSION);

        TUIBarrageJson.Data data = new TUIBarrageJson.Data();
        data.setMessage(model.message);

        //扩展信息
        TUIBarrageJson.Data.ExtInfo extInfo = new TUIBarrageJson.Data.ExtInfo();
        extInfo.setUserID(model.extInfo.get(TUIBarrageConstants.KEY_USER_ID));
        extInfo.setNickName(model.extInfo.get(TUIBarrageConstants.KEY_USER_NAME));
        extInfo.setAvatarUrl(model.extInfo.get(TUIBarrageConstants.KEY_USER_AVATAR));

        data.setExtInfo(extInfo);
        sendJson.setData(data);

        Gson gson = new Gson();
        return gson.toJson(sendJson);
    }
}

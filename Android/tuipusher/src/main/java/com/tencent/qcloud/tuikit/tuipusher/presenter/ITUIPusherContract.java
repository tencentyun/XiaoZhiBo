package com.tencent.qcloud.tuikit.tuipusher.presenter;

import android.graphics.Bitmap;

import com.tencent.qcloud.tuikit.tuipusher.model.listener.ITUIPusherSignallingListener;
import com.tencent.qcloud.tuikit.tuipusher.view.TUIPusherView;
import com.tencent.rtmp.ui.TXCloudVideoView;

/**
 * 封装UI调用的接口 以及 通知UI更新的接口
 */
public interface ITUIPusherContract {

    interface ITUIPusherPresenter {

        /**
         * 开启本地视频的预览画面
         *
         * @param isFront true：前置摄像头；false：后置摄像头。
         * @param view    承载视频画面的控件
         */
        void startPreview(boolean isFront, TXCloudVideoView view);

        /**
         * 开启图片推流
         *
         * @param bitmap 推流图片
         */
        void startVirtualCamera(Bitmap bitmap);

        /**
         * 关闭图片推流
         */
        void stopVirtualCamera();

        /**
         * 开始推流
         * <p>
         * 推流流程：
         * 1、创建 IM直播群组
         * 2、调用 StartPush
         *
         * @param url
         */
        void startPush(String url);

        /**
         * 停止推流
         */
        void stopPush();

        /**
         * 请求跨房 PK
         * 主播A 和 主播B，他们之间的跨房 PK 流程如下：
         * 1. 【主播 A】调用 {@link ITUIPusherPresenter#requestPK } 向主播 B 发起连麦请求。
         * 2. 【主播 B】会收到 {@link ITUIPusherSignallingListener#onRequestPK} 回调通知。
         * 3. 【主播 B】调用 {@link ITUIPusherPresenter#responsePK } 决定是否接受主播 A 的 PK 请求。
         * 接受时：3.1: startPlay()播放主播A 的流
         * 3.2: setMixTranscodingConfig() 与主播A的混流
         * 不接受：此次PK终止
         * 4. 【主播 A】会收到 {@link ITUIPusherSignallingListener#onResponsePK} 回调通知，该通知会携带来自主播 B 的处理结果。
         * 接受时：4.1: startPlay()播放主播B 的流
         * 4.2：setMixTranscodingConfig() 与主播B的混流
         * 不接受：此次PK终止
         *
         * @param userId
         */
        boolean requestPK(String userId);

        /**
         * 取消PK
         */
        void cancelPK();

        /**
         * 响应 请求垮房 PK
         *
         * @param agree
         * @param reason
         * @param timeout
         */
        void responsePK(boolean agree, String reason, int timeout);

        /**
         * 停止PK
         */
        void stopPK();

        /**
         * 响应 连麦请求
         *
         * @param agree
         * @param reason
         * @param timeout
         */
        void responseLink(boolean agree, String reason, int timeout);

        /**
         * 销毁所有资源
         */
        void destory();

        /**
         * 处理接受PK的逻辑
         *
         * @param pkVideoView
         */
        void startPK(TXCloudVideoView pkVideoView);

        /**
         * 设置镜像
         */
        void setMirror(boolean isMirror);

        /**
         * 设置分辨率
         *
         * @param resolution
         */
        void setResolution(int resolution);

        /**
         * 切换摄像头
         *
         * @param isFrontCamera
         */
        void switchCamera(boolean isFrontCamera);

        /**
         * 开始连麦
         */
        void startLink(TXCloudVideoView linkVideoView);

        /**
         * 停止连麦
         */
        void stopLink(int timeout);
    }


    interface ITUIPusherView {
        /**
         * UI 吐司提示
         *
         * @param message
         */
        void onToastMessage(String message);

        /**
         * UI 状态改变通知
         *
         * @param state
         */
        void onNotifyState(TUIPusherView.State state);

        /**
         * PK 状态改变通知
         *
         * @param pkState
         * @param pkUserId
         * @param reason
         */
        void onNotifyPKState(TUIPusherView.PKState pkState, String pkUserId, String reason);

        /**
         * 连麦 状态改变通知
         *
         * @param linkState
         */
        void onNotifyLinkState(TUIPusherView.LinkState linkState, String linkUserId, String reason);

        /**
         * 开始连麦回调
         */
        void onStartLink();
    }
}

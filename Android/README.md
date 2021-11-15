本文档主要介绍如何运行小直播

## 工程动态
小直播首次发布，其功能模块主要包含
- 登录注册
- 秀场直播
  - 主播列表:获取正在直播的主播房间信息
  - 创建房间：以主播角色创建房间，房间里的功能包含：推流、连麦、PK、美颜效果设置、音效设置、弹幕模块、礼物模块等等
  - 进入房间：以观众角色进入房间，房间里功能包含：拉流、连麦、弹幕模块，礼物模块等等
- 发现页面
- 个人中心（我的页面）


## 准备工作

### 目录说明
```
MLVB-APP-Source
├─ app                  //小直播主目录
└─ basic                //基础公用目录
└─ debug                //测试调试目录
└─ login                //登录模块
└─ tuiaudioeffect       //音效组件
└─ tuibarrage           //弹幕组件
└─ tuibeauty            //美颜组件
└─ tuigift              //礼物组件
└─ tuiplayer            //拉流组件
└─ tuipusher            //推流组件
```

### 环境说明
- 最低兼容 Android 4.1（SDK API Level 16），建议使用 Android 5.0 （SDK API Level 21）及以上版本
- Android Studio 3.5及以上版本
- App 要求 Android 4.1及以上设备

### 控制台说明
#### 前提条件
您已 [注册腾讯云](https://cloud.tencent.com/document/product/378/17985) 账号，并完成 [实名认证](https://cloud.tencent.com/document/product/378/3629)。

#### 申请 SDKAPPID 和 SECRETKEY
1. 登录实时音视频控制台，选择【开发辅助】>【[快速跑通Demo](https://console.cloud.tencent.com/trtc/quickstart)】。
2. 单击【立即开始】，输入您的应用名称，例如`TestTRTC`，单击【创建应用】。
<img src="https://main.qcloudimg.com/raw/169391f6711857dca6ed8cfce7b391bd.png" width="650" height="295"/>
3. 创建应用完成后，单击【我已下载，下一步】，可以查看 SDKAppID 和密钥信息。

#### 开通移动直播服务
1. [开通直播服务并绑定域名](https://console.cloud.tencent.com/live/livestat) 如果还没开通，点击申请开通，之后在域名管理中配置推流域名和拉流域名
2. [获取SDK的测试License](https://console.cloud.tencent.com/live/license)
3. [配置推拉流域名](https://console.cloud.tencent.com/live/domainmanage)

## 运行示例

### 下载代码

```
    git clone
```

### 工程导入
- 使用 Android Studio（3.5及以上的版本）打开源码工程`MLVB-APP-Source`。

### 工程配置
1. 找到并打开`MLVB-APP-Source/debug/src/main/java/com/tencent/liteav/debug/GenerateTestUserSig.java`文件。
2. 设置`GenerateTestUserSig.java`文件中的相关参数：
  - `SDKAPPID`：默认为 PLACEHOLDER ，请设置为实际的 SDKAppID；
  - `SECRETKEY`：默认为空字符串，请设置为实际的密钥信息；
  - `LICENSEURL`：默认为 PLACEHOLDER ，请设置为实际的License Url信息；
  - `LICENSEURLKEY`：默认为 PLACEHOLDER ，请设置为实际的License Key信息；
  - `PLAY_DOMAIN`：默认为 PLACEHOLDER ，请设置为实际的拉流域名；
3. 修改 app模块下的 `build.gradle` 文件中 `applicationId` 字段 为License 信息所对应的包名

### 编译运行
用 Android Studio 打开该项目，连上Android设备，编译并运行。


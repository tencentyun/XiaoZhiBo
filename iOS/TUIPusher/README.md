# TUIPusher iOS 示例工程快速跑通
本文档主要介绍如何运行TUIPusher组件的，以及如何在别的工程中集成TUIPusher组件

## 目录结构

```
iOS
├── Example                     // Demo 工程
├── README.md                   // 运行文档
├── Resources                   // TUIPusher所需的图片、国际化字符串资源文件夹
├── Source                      // TUIPusher核心业务逻辑代码文件夹
└── TUIPusher.podspec           // 工程依赖的基础组件
```

## 环境准备
- Xcode 11.0及以上版本
- 最低支持系统：iOS 11.0
- 请确保您的项目已设置有效的开发者签名

## 运行示例

### 前提条件
您已 [注册腾讯云](https://cloud.tencent.com/document/product/378/17985) 账号，并完成 [实名认证](https://cloud.tencent.com/document/product/378/3629)。

### 第一步：创建TRTC的应用
1. 一键进入腾讯云实时音视频控制台的[应用管理](https://console.cloud.tencent.com/trtc/app)界面，选择创建应用，输入应用名称，例如 `TUIKitDemo` ，单击 **创建**；
2. 点击对应应用条目后**应用信息**，具体位置如下下图所示：
    <img src="https://qcloudimg.tencent-cloud.cn/raw/62f58d310dde3de2d765e9a460b8676a.png" width="900">
3. 进入应用信息后，按下图操作，记录SDKAppID和密钥：
    <img src="https://qcloudimg.tencent-cloud.cn/raw/bea06852e22a33c77cb41d287cac25db.png" width="900">
4. 进入腾讯云直播[LICENSE管理](https://console.cloud.tencent.com/live/license)节面，创建应用并绑定LICENSE
![](https://qcloudimg.tencent-cloud.cn/raw/886dbc5cf9cea301a69a7c06c80390d4.png)
创建成功后请记录 ` License Key `和 `License URL`，便于在运行时使用。
![](https://qcloudimg.tencent-cloud.cn/raw/5bca99c4b00f23eaa763310dc475ec1e.png)

### 第二步：下载源码，配置工程
1. 克隆或者直接下载此仓库源码，**欢迎 Star**，感谢~~
2. SDK集成方式默认使用`Cocoapods`，工程目录下`Podfile`文件内已帮您添加了SDK的依赖`pod 'TXLiteAVSDK_Live'`，您只需要打开终端进入到工程目录下执行`pod install`，SDK就会自动集成。

```
pod install
```

3. 使用Xcode(11.0及以上)打开源码工程`iOS/Example/TUIPusherApp.xcworkspace`，工程内找到`Debug/GenerateTestUserSig.swift`文件 。
4. 设置`GenerateTestUserSig.swift`文件中的相关参数：
  - `SDKAPPID`：默认为 PLACEHOLDER ，请设置为实际的 SDKAppID；
  - `SECRETKEY`：默认为空字符串，请设置为实际的密钥信息；
  - `LICENSEURL`：默认为 PLACEHOLDER ，请设置为实际的License Url信息；
  - `LICENSEURLKEY`：默认为 PLACEHOLDER ，请设置为实际的License Key信息；
  - `PUSH_DOMAIN`：可选：RTMP协议推流域名，有使用RTMP协议推流时配置即可
  - `PLAY_DOMAIN`：可选：配置的拉流域名
  - `LIVE_URL_KEY`：可选：如果开通鉴权配置的鉴权Key


### 第三步：编译运行

使用 Xcode（11.0及以上的版本）打开源码工程 `iOS/Example/TUIPusherApp.xcworkspace`，单击【运行】即可开始调试本 App。

### 第四步：示例体验
Tips：TUIPusher 组件仅可体验推流端效果，如需观看拉流效果请使用 [TUIPlayer](https://github.com/LiteAV-TUIKit/TUIPlayer)组件：

设备 A

步骤 1：在欢迎页，输入用户名(请确保用户名唯一性，不能与其他用户重复)，比如111；
步骤 2：点击开始直播，即可体验；

设备 B

步骤 1：在欢迎页，输入用户名(请确保用户名唯一性，不能与其他用户重复)，比如222；
步骤 2：点击开始直播，即可体验；
步骤 3：输入要PK的房间号（用户名）如111，点击 `PK` ，等待用户 111 同意后，即可与用户 111 进行 `PK`；

我们在自己的[小直播](https://github.com/tencentyun/XiaoZhiBo)工程中也使用了该TUIPlayer组件，可以参考。

## 问题答疑
1、我们官网文档[常见问题](https://cloud.tencent.com/document/product/454/7998)中整理了一些常见的问题，如果遇到相同的问题，可以参考上面的解决方案
2、可以加入我们的 TUIKIT 答疑群，在群里我们有专人进行答疑


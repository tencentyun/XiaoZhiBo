本文档主要介绍如何运行小直播

## 目录说明
```
.
├─ app                  //小直播主目录
└─ basic                //基础公用目录
└─ debug                //配置目录
└─ login                //登录模块
└─ tuiaudioeffect       //音效组件
└─ tuibarrage           //弹幕组件
└─ tuibeauty            //美颜组件
└─ tuigift              //礼物组件
└─ tuiplayer            //拉流组件
└─ tuipusher            //推流组件
```

### 准备工作

- 腾讯云服务的开启：点击这里[https://cloud.tencent.com/document/product/454/38625]

- 后台程序的运行：点击这里[https://cloud.tencent.com/document/product/454/38625]

## 运行示例

### 环境说明
- 最低兼容 Android 4.1（SDK API Level 16），建议使用 Android 5.0 （SDK API Level 21）及以上版本
- Android Studio 3.5及以上版本
- App 要求 Android 4.1及以上设备

### 下载代码

```
git clone https://github.com/tencentyun/XiaoZhiBo
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


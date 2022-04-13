本文档主要介绍如何运行小直播的

## 准备工作
### 目录说明
```
iOS
├─ APP              
│   ├── App         // 主要文件
│   ├── Base        // 扩展类文件
│   ├── Debug       // 配置文件
│   ├── Podfile     // pod 安装文件
│   ├── Resource    // 资源文件
│   ├── Scene       // 场景业务文件
│   └── Services    // 服务类文件
├─ TUIAudioEffect   // 音效组件
├─ TUIBarrage       // 弹幕组件
├─ TUIBeauty        // 美颜组件
├─ TUIGift          // 礼物组件
├─ TUIPlayer        // 拉流组件
└─ TUIPusher        // 推流组件

```


### 准备工作

- 腾讯云服务的开启：点击这里[https://cloud.tencent.com/document/product/454/38625]

- 后台程序的运行：点击这里[https://cloud.tencent.com/document/product/454/38625]

## 运行示例
### 环境说明
- iOS 13.0，Xcode 11.0 以上

### 代码下载
在`终端`执行命令`git clone `来下载源代码

```
git clone https://github.com/tencentyun/XiaoZhiBo
```

### 工程导入
1. 使用`终端`，cd到工程文件`XiaoZhiBoApp.xcodeproj`的目录，执行命令 "pod install"
2. 使用 Xcode（11.0及以上的版本）打开源码工程`XiaoZhiBoApp.xcworkspace`

### 工程配置

1. 使用`终端`，cd到工程文件`XiaoZhiBoApp.xcodeproj`的目录，执行命令 "pod install"。
2. 使用 Xcode（11.0及以上的版本）打开源码工程`XiaoZhiBoApp.xcworkspace`。
3. 找到并打开 `XiaoZhiBo/iOS/APP/Debug/GenerateGlobalConfig.swift` 文件，按照上述步骤中记录的关键信息，设置此文件中的相关参数：

| 参数 | 说明 |
|---------|---------|
|SERVERLESSURL(可选)|默认为 "PLACEHOLDER" , 如需配置此项，请设置为[后台服务部署](https://cloud.tencent.com/document/product/454/38625) 成功后记录下的 URL，例如：<code>https://service-xxxyyzzz-1001234567.gz.apigw.tencentcs.com</code> |
|LICENSEURL|默认为 PLACEHOLDER ，请设置为实际的 License URL 信息|
|LICENSEURLKEY|默认为 PLACEHOLDER ，请设置为实际的 License Key 信息|
|PLAY_DOMAIN|默认为 PLACEHOLDER ，请设置为实际的拉流域名|
|XMagicLicenseURL|默认为 PLACEHOLDER ，请设置为腾讯特效 LicenseURL 信息，请参考: [腾讯特效License指引](https://cloud.tencent.com/document/product/616/65878)|
|XMagicLicenseKey|默认为 PLACEHOLDER ，请设置为腾讯特效 LicenseKey 信息，请参考: [腾讯特效License指引](https://cloud.tencent.com/document/product/616/65878)|

4. 找到并打开 `XiaoZhiBo/iOS/APP/Debug/GenerateTestUserSig.swift` 文件，按照上述步骤中记录的关键信息，设置此文件中的相关参数：

| 参数 | 说明 |
|---------|---------|
|SDKAPPID|默认为 0 ，请设置为实际的 SDKAppID |
|SECRETKEY|默认为 PLACEHOLDER ，请设置为实际的密钥信息|

![](https://qcloudimg.tencent-cloud.cn/raw/e58a4ec175b62f329428366d1c1d6572.png)

5. 修改工程的 `Bundle identifier` 字段为 License 信息所对应的包名；

>? 关于TUIBeauty的美颜特效依赖库编译失败问题，请前往 [美颜特效-SDK集成指引iOS](https://cloud.tencent.com/document/product/616/65887)，下载并解压[Demo包](https://mediacloud-76607.gzc.vod.tencent-cloud.com/TencentEffect/iOS/2.4.1vcube/MLVB-API-Example.zip)，将SDK目录中的 `libpag.framework、XMagic.framework、YTCommonXMagic.framework`导入到小直播TUIBeauty下的Frameworks目录，重新执行命令 "pod install"即可 。

### 编译运行
用 Xcode 打开该项目，连上 iOS 设备，编译并运行。

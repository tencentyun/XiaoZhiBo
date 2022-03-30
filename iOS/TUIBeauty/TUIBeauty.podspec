Pod::Spec.new do |spec|
  spec.name         = 'TUIBeauty'
  spec.version      = '1.0.0'
  spec.platform     = :ios
  spec.ios.deployment_target = '9.0'
  spec.license      = { :type => 'Proprietary',
      :text => <<-LICENSE
        copyright 2017 tencent Ltd. All rights reserved.
        LICENSE
       }
  spec.homepage     = 'https://cloud.tencent.com/document/product/269/3794'
  spec.documentation_url = 'https://cloud.tencent.com/document/product/269/9147'
  spec.authors      = 'tencent video cloud'
  spec.summary      = 'TUIBeauty'
  spec.xcconfig     = { 'VALID_ARCHS' => 'armv7 arm64 x86_64' }

  spec.dependency 'Masonry'
  spec.dependency 'TUICore'
  spec.dependency 'SSZipArchive'
  
  spec.requires_arc = true
  spec.static_framework = true
  spec.source = { :git => '', :tag => "#{spec.version}" }
  spec.pod_target_xcconfig = {
    'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'arm64'
  }
  spec.user_target_xcconfig = { 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'arm64' }
  spec.default_subspec = 'Live'

  spec.subspec 'Live' do |live|
    live.dependency 'TXLiteAVSDK_Live'
    live.source_files = 'Source/Localized/**/*.{h,m,mm}', 'Source/PublicHeader/*', 'Source/Model/**/*.{h,m,mm,c}', 'Source/Server/**/*.{h,m,mm}', 'Source/UI/**/*.{h,m,mm}', 'Source/TUIExtension/**/*.{h,m,mm}', 'Source/Common/**/*.{h,m,mm}', 'Source/Presenter/**/*.{h,m,mm}', 'Source/TUIBeautyKit_Professional/*.{h,m,mm}','Frameworks/**/Headers/*.h'
    live.public_header_files ='Source/**/Headers/*.h'
    live.ios.framework = ['AVFoundation', 'Accelerate', 'AssetsLibrary','CoreML', 'JavaScriptCore', 'CoreFoundation', 'MetalPerformanceShaders','CoreTelephony']
    live.library = 'c++', 'resolv', 'sqlite3'
    live.resource_bundles = {
      'TUIBeautyKitBundle' => ['Resources/Localized/**/*.strings','Resources/Xmagic/*.bundle', 'Resources/*.xcassets', 'Resources/*.bundle', 'Resources/*.mp4', 'Resources/*.json', 'Resources/BeautyResource/*','Resources/Xmagic/BeautyRes/*']
    }
    live.vendored_frameworks =
    'Frameworks/YTCommonXMagic.framework',
    'Frameworks/libpag.framework',
    'Frameworks/XMagic.framework'
  end
 
  spec.subspec 'Professional' do |professional|
    professional.ios.deployment_target = '9.0'
    professional.dependency 'TXLiteAVSDK_Professional'
    professional.source_files = 'Source/Localized/**/*.{h,m,mm}', 'Source/PublicHeader/*', 'Source/Model/**/*.{h,m,mm,c}', 'Source/Server/**/*.{h,m,mm}', 'Source/UI/**/*.{h,m,mm}', 'Source/TUIExtension/**/*.{h,m,mm}', 'Source/Common/**/*.{h,m,mm}', 'Source/Presenter/**/*.{h,m,mm}', 'Source/TUIBeautyKit_Professional/*.{h,m,mm}','Frameworks/**/Headers/*.h'
    professional.public_header_files ='Source/**/Headers/*.h'
    professional.ios.framework = ['AVFoundation', 'Accelerate', 'AssetsLibrary','CoreML', 'JavaScriptCore', 'CoreFoundation', 'MetalPerformanceShaders','CoreTelephony']
    professional.library = 'c++', 'resolv', 'sqlite3'
    professional.resource_bundles = {
      'TUIBeautyKitBundle' => ['Resources/Localized/**/*.strings','Resources/Xmagic/*.bundle', 'Resources/*.xcassets', 'Resources/*.bundle', 'Resources/*.mp4', 'Resources/*.json', 'Resources/BeautyResource/*','Resources/Xmagic/BeautyRes/*']
    }
    professional.vendored_frameworks =
    'Frameworks/YTCommonXMagic.framework',
    'Frameworks/libpag.framework',
    'Frameworks/XMagic.framework'
    
  end
  
  spec.subspec 'Enterprise' do |enterprise|
    enterprise.dependency 'TXLiteAVSDK_Enterprise'
    framework_path="../../SDK/TXLiteAVSDK_Enterprise.framework"
    enterprise.pod_target_xcconfig={
      'HEADER_SEARCH_PATHS'=>["$(PODS_TARGET_SRCROOT)/#{framework_path}/Headers"]
    }
    enterprise.xcconfig = { 'HEADER_SEARCH_PATHS' => '${SRCROOT}/../SDK/TXLiteAVSDK_Enterprise.framework/Headers/'}
    enterprise.source_files = 'Source/Localized/**/*.{h,m,mm}', 'Source/PublicHeader/*', 'Source/Model/**/*.{h,m,mm,c}', 'Source/Server/**/*.{h,m,mm}', 'Source/UI/**/*.{h,m,mm}', 'Source/TUIExtension/**/*.{h,m,mm}', 'Source/Common/**/*.{h,m,mm}', 'Source/Presenter/**/*.{h,m,mm}', 'Source/TUIBeautyKit_Enterprise/*.{h,m,mm}'
    enterprise.ios.framework = ['AVFoundation', 'Accelerate', 'AssetsLibrary']
    enterprise.library = 'c++', 'resolv', 'sqlite3'
    enterprise.resource_bundles = {
      'TUIBeautyKitBundle' => ['Resources/Localized/**/*.strings', 'Resources/*.xcassets', 'Resources/*.bundle', 'Resources/*.mp4', 'Resources/*.json', 'Resources/BeautyResource/*']
    }
  end

  
end


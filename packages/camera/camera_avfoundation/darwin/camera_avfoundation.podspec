#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html
#
Pod::Spec.new do |s|
  s.name             = 'camera_avfoundation'
  s.version          = '0.0.1'
  s.summary          = 'Flutter Camera'
  s.description      = <<-DESC
A Flutter plugin to use the camera from your Flutter app.
                       DESC
  s.homepage         = 'https://github.com/flutter/packages'
  s.license          = { :type => 'BSD', :file => '../LICENSE' }
  s.author           = { 'Flutter Dev Team' => 'flutter-dev@googlegroups.com' }
  s.source           = { :http => 'https://github.com/flutter/packages/tree/main/packages/camera_avfoundation' }
  s.documentation_url = 'https://pub.dev/packages/camera_avfoundation'
  s.source_files = 'Classes/**/*.{h,m}'
  s.public_header_files = 'Classes/**/*.h'
  s.module_map = 'Classes/CameraPlugin.modulemap'
  s.ios.dependency 'Flutter'
  s.macos.dependency 'FlutterMacOS'
  s.ios.deployment_target = '11.0'
  s.macos.deployment_target = '10.15'
  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES' }
end

require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "react-native-incode-sdk"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = package["homepage"]
  s.license      = package["license"]
  s.authors      = package["author"]

  s.platforms    = { :ios => "11.0" }
  s.source       = { :git => "https://github.com/IncodeTechnologies/Incode-Welcome-Example-React.git", :tag => "#{s.version}" }
  s.pod_target_xcconfig = {
      'ARCHS' => 'arm64, x86_64',
      'ARCHS[sdk=iphoneos*]' => 'arm64',
      'ARCHS[sdk=iphonesimulator*]' => 'x86_64',
      'VALID_ARCHS' => 'arm64, x86_64', # support for Xcode 11
      'VALID_ARCHS[sdk=iphoneos*]' => 'arm64', # support for Xcode 11
      'VALID_ARCHS[sdk=iphonesimulator*]' => 'x86_64', # support for Xcode 11
      'ONLY_ACTIVE_ARCH' => 'YES',
      'BUILD_LIBRARY_FOR_DISTRIBUTION' => 'YES',
      'SKIP_INSTALL' => 'YES',
      'DEFINES_MODULE' => 'YES',
      'DEBUG_INFORMATION_FORMAT' => 'DWARF',
      'OTHER_LDFLAGS'  => '$(inherited) -all_load -ObjC -l "stdc++" -weak_framework "CryptoKit" -l "iconv" -framework "VideoToolbox"'
  }
  
  s.source_files = "ios/**/*.{h,m,mm,swift}"
  s.vendored_frameworks = 'Frameworks/IncdOnboarding.xcframework', 'Frameworks/opencv2.framework', 'Frameworks/OpenTok.framework'
  s.dependency "React-Core"
end

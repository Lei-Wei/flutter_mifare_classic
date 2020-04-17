#import "MifareClassicPlugin.h"
#if __has_include(<mifare_classic/mifare_classic-Swift.h>)
#import <mifare_classic/mifare_classic-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "mifare_classic-Swift.h"
#endif

@implementation MifareClassicPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftMifareClassicPlugin registerWithRegistrar:registrar];
}
@end

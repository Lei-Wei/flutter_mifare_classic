import 'dart:async';

import 'package:flutter/services.dart';

class MifareClassic {
  static const MethodChannel _channel =
      const MethodChannel('mifare_classic');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}

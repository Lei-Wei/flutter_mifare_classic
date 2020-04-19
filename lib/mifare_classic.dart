import 'dart:async';

import 'package:flutter/services.dart';

class MifareClassic {
  static const MethodChannel _channel = const MethodChannel('mifare_classic');

  static Future<bool> get nfcState async {
    return await _channel.invokeMethod('getNfcState');
  }

  static Future<bool> changeNfcState() async {
    return await _channel.invokeMethod('changeNfcState');
  }
}

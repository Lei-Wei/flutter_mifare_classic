import 'dart:async';

import 'package:flutter/services.dart';

class MifareClassic {
  static var changeController = StreamController<IdChangedEvent>();
  static Stream<IdChangedEvent> get onChange => changeController.stream;

  static const MethodChannel _methodhannel =
      const MethodChannel('method_channel');

  static const BasicMessageChannel<String> _messageChannel =
      BasicMessageChannel('message_channel', StringCodec());

  static init() {
    _messageChannel.setMessageHandler((msg) => Future<String>(() {
          changeController.add(IdChangedEvent(msg));
        }));
  }

  static Future<bool> get available async {
    return await _methodhannel.invokeMethod('available');
  }

  static Future<bool> get enabled async {
    return await _methodhannel.invokeMethod('enabled');
  }

  static Future<bool> get nfcState async {
    return await _methodhannel.invokeMethod('getNfcState');
  }

  static Future<bool> changeNfcState() async {
    return await _methodhannel.invokeMethod('changeNfcState');
  }
}

class IdChangedEvent {
  String eventData;
  IdChangedEvent(this.eventData);
}

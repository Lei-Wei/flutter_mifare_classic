import 'dart:async';

import 'package:flutter/services.dart';

class MifareClassic {
  static var changeController = StreamController<IdChangedEvent>();
  Stream<IdChangedEvent> get onChange => changeController.stream;

  static const MethodChannel _methodhannel =
      const MethodChannel('method_channel');

  static const BasicMessageChannel<String> _messageChannel =
      BasicMessageChannel('message_channel', StringCodec());

  MifareClassic() {
    _messageChannel.setMessageHandler((msg) => Future<String>(() {
      changeController.add(IdChangedEvent(msg));
    }));
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

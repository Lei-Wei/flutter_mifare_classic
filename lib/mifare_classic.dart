import 'dart:async';

import 'package:flutter/services.dart';

class MifareClassic {
  var changeController = StreamController<IdChangedEvent>();
  Stream<IdChangedEvent> get onChange => changeController.stream;

  final MethodChannel _methodhannel = const MethodChannel('method_channel');

  final BasicMessageChannel<String> _messageChannel =
      BasicMessageChannel('message_channel', StringCodec());

  MifareClassic() {
    _messageChannel.setMessageHandler((msg) => Future<String>(() {
          changeController.add(IdChangedEvent(msg));
        }));
  }

  Future<bool> get available async {
    return await _methodhannel.invokeMethod('available');
  }

  Future<bool> get enabled async {
    return await _methodhannel.invokeMethod('enabled');
  }

  Future<bool> get nfcState async {
    return await _methodhannel.invokeMethod('getNfcState');
  }

  Future<bool> changeNfcState() async {
    return await _methodhannel.invokeMethod('changeNfcState');
  }
}

class IdChangedEvent {
  String eventData;
  IdChangedEvent(this.eventData);
}

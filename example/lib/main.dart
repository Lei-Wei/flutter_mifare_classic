import 'package:flutter/material.dart';
import 'dart:async';
import 'package:mifare_classic/mifare_classic.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String message = '';
  bool enabled = false;
  bool available = false;
  MifareClassic mfc;

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    if (!mounted) return;

    mfc = MifareClassic();
    mfc.onChange.listen((data) {
      if (isNumeric(data.eventData)) {
        setState(() {
          message = data.eventData;
        });
      } else {
        message = data.eventData;
      }
    });

    bool _enabled = await mfc.nfcState;
    bool _en = await mfc.enabled;

    setState(() {
      enabled = _enabled;
      available = _en;
    });
  }

  bool isNumeric(String s) {
    if (s == null) {
      return false;
    }
    return double.tryParse(s) != null;
  }

  changeNfcState() async {
    bool _enabled = await mfc.changeNfcState();
    setState(() {
      enabled = _enabled;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Test Mifare Classic'),
        ),
        body: available
            ? Center(
                child: RaisedButton(
                  child: Text(message),
                  color: enabled ? Colors.red : Colors.blue,
                  onPressed: () {
                    changeNfcState();
                  },
                ),
              )
            : Text('hardware is not ready. (enable NFC on your device first!)'),
      ),
    );
  }
}

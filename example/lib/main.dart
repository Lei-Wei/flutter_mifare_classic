import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:mifare_classic/mifare_classic.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String message = '';
  bool enabled = false;
  MifareClassic mfc = MifareClassic();

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    if (!mounted) return;

    mfc.onChange.listen((data) {
      if (isNumeric(data.eventData)) {
        setState(() {
          message = data.eventData;
        });
      } else {
        message = data.eventData;
      }
    });
    bool _enabled = await MifareClassic.nfcState;
    setState(() {
      enabled = _enabled;
    });
  }

  bool isNumeric(String s) {
    if (s == null) {
      return false;
    }
    return double.tryParse(s) != null;
  }

  changeNfcState() async {
    bool _enabled = await MifareClassic.changeNfcState();
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
        body: Center(
          child: RaisedButton(
            child: Text(message),
            color: enabled ? Colors.red : Colors.blue,
            onPressed: () {
              changeNfcState();
            },
          ),
        ),
      ),
    );
  }
}

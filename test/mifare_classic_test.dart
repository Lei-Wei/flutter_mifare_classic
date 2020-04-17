import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mifare_classic/mifare_classic.dart';

void main() {
  const MethodChannel channel = MethodChannel('mifare_classic');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await MifareClassic.platformVersion, '42');
  });
}

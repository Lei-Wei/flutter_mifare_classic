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

  test('startNfc', () async {
    expect(await MifareClassic.startNfc(), true);
  });
}

package com.vip.mifare_classic;

import androidx.annotation.NonNull;

import android.content.Context;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.util.Log;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import java.io.IOException;
import java.util.concurrent.ExecutionException;


/**
 * MifareClassicPlugin
 */
public class MifareClassicPlugin implements MethodCallHandler, PluginRegistry.NewIntentListener {
    static boolean nfcEnabled = false;

    static NfcAdapter mAdapter;
    static MethodChannel channel = null;
    static Intent savedIntent;

    static String[] keys = {"528C9DFFE28C", "3E3554AF0E12", "740E9A4F9AAF", "97184D136233"};
    static int[] sectors = {5, 6, 8, 9};

    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "mifare_classic");
        MifareClassicPlugin plugin = new MifareClassicPlugin();
        channel.setMethodCallHandler(plugin);

        Context context = registrar.context();
        registrar.addNewIntentListener(plugin);
        mAdapter = NfcAdapter.getDefaultAdapter(context);
        if (mAdapter == null || !mAdapter.isEnabled()) {
            nfcEnabled = false;
        }
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (call.method.equals("getNfcState")) {
            result.success(nfcEnabled);
        } else if (call.method.equals("changeNfcState")) {
            nfcEnabled = !nfcEnabled;
            result.success(nfcEnabled);
        } else {
            result.notImplemented();
        }
    }

    @Override
    public boolean onNewIntent(Intent intent) {
        if(savedIntent == intent) return false;
        savedIntent = intent;
        try {
            readCard(intent);
        } catch (IOException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void readCard(Intent intent) throws IOException, ExecutionException, InterruptedException {
        String action = intent.getAction();

        if (mAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || mAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || mAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            Tag tagFromIntent = intent.getParcelableExtra(mAdapter.EXTRA_TAG);
            MifareClassic mfc = MifareClassic.get(tagFromIntent);

            if (mfc == null) return;
            try {
                mfc.connect();
                for(int i=0;i<4;i++){
                    try{
                        boolean auth = mfc.authenticateSectorWithKeyA(sectors[i], hexStringToByteArray(keys[i]));
                    if (auth) {
                        String res = extractId(bytesToHexString(mfc.readBlock(mfc.sectorToBlock(sectors[i]))));
                        Log.d("stuId: ", new String(res));
                        break;
                    }
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                }
                
                mfc.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("0x");
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private String extractId(String hexStr) {
        short[] index = {5, 7, 9, 11, 13, 15, 17};
        String result = "";
        for (short i : index) {
            result += hexStr.charAt(i);
        }
        return new StringBuffer(result).reverse().toString();
    }
}

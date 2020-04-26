package com.vip.mifare_classic;

import androidx.annotation.NonNull;

import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.Application.ActivityLifecycleCallbacks;
import android.nfc.FormatException;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.util.Log;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.BasicMessageChannel;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.plugin.common.StringCodec;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * MifareClassicPlugin
 */
public class MifareClassicPlugin
        implements MethodCallHandler, PluginRegistry.NewIntentListener, ActivityLifecycleCallbacks {
    // don't actually disable NFC on device
    private static boolean nfcAppEnabled = false;
    private PendingIntent pendingIntent;
    private static NfcAdapter mAdapter;
    private static MethodChannel channel = null;
    private static Intent savedIntent;
    private static BasicMessageChannel<String> messageChannel;
    private IntentFilter[] nfcFilters;

    private static String[] keys = { "528C9DFFE28C", "3E3554AF0E12", "740E9A4F9AAF", "97184D136233" };
    private static int[] sectors = { 5, 6, 8, 9 };
    private Registrar registrar;

    public MifareClassicPlugin(Registrar registrar) {
        this.registrar = registrar;
        createPendingIntent();
        nfcFilters = new IntentFilter[] { new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
                new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED) };
    }

    public static void registerWith(Registrar registrar) {
        MifareClassicPlugin plugin = new MifareClassicPlugin(registrar);
        final MethodChannel methodChannel = new MethodChannel(registrar.messenger(), "method_channel");
        messageChannel = new BasicMessageChannel<String>(registrar.messenger(), "message_channel",
                StringCodec.INSTANCE);
        methodChannel.setMethodCallHandler(plugin);

        Context context = registrar.context();
        registrar.addNewIntentListener(plugin);

        registrar.activity().getApplication().registerActivityLifecycleCallbacks(plugin);

        mAdapter = NfcAdapter.getDefaultAdapter(context);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (call.method.equals("getNfcState")) {
            result.success(nfcAppEnabled);
        } else if (call.method.equals("changeNfcState")) {
            nfcAppEnabled = !nfcAppEnabled;
            result.success(nfcAppEnabled);
        } else if (call.method.equals("available")) {
            result.success(mAdapter != null);
        } else if (call.method.equals("enabled")) {
            result.success(mAdapter.isEnabled());
        } else {
            result.notImplemented();
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (mAdapter != null) {
            mAdapter.enableForegroundDispatch(registrar.activity(), pendingIntent, null, null);
        }
    }

    @Override
    public void onActivityDestroyed(Activity acticity) {

    }

    @Override
    public void onActivityStopped(Activity act) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity act, Bundle bun) {
    }

    @Override
    public void onActivityPaused(Activity act) {
    }

    @Override
    public void onActivityStarted(Activity act) {
    }

    @Override
    public void onActivityCreated(Activity act, Bundle bun) {

    }

    public void createPendingIntent() {
        if (pendingIntent == null) {
            Activity activity = registrar.activity();
            Intent intent = new Intent(activity, activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pendingIntent = PendingIntent.getActivity(activity, 0, intent, 0);
        }
    }

    @Override
    public boolean onNewIntent(Intent intent) {

        // there is a "run twice" bug if don't have this judgement
        if (savedIntent == intent)
            return false;
        savedIntent = intent;
        try {
            readCard(intent);
        } catch (IOException | ExecutionException | InterruptedException e) {
            messageChannel.send("Error on reading card!");
        }
        return true;
    }

    private void readCard(Intent intent) throws IOException, ExecutionException, InterruptedException {
        if (!nfcAppEnabled)
            return;
        String action = intent.getAction();

        if (mAdapter.ACTION_TAG_DISCOVERED.equals(action) || mAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || mAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            Tag tagFromIntent = intent.getParcelableExtra(mAdapter.EXTRA_TAG);
            MifareClassic mfc = MifareClassic.get(tagFromIntent);

            if (mfc == null)
                return;
            mfc.connect();
            for (int i = 0; i < 4; i++) {
                boolean auth = mfc.authenticateSectorWithKeyA(sectors[i], hexStringToByteArray(keys[i]));
                if (auth) {
                    String stuId = extractId(bytesToHexString(mfc.readBlock(mfc.sectorToBlock(sectors[i]))));
                    if (checkStr(stuId)) {
                        messageChannel.send(stuId);
                        break;
                    }
                }
            }
            mfc.close();
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
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private String extractId(String hexStr) {
        short[] index = { 5, 7, 9, 11, 13, 15, 17 };
        String result = "";
        for (short i : index) {
            result += hexStr.charAt(i);
        }
        return new StringBuffer(result).reverse().toString();
    }

    // check whether the result string contains the id number
    private boolean checkStr(String res) {
        if (res.length() != 7)
            return false;
        int temp;
        try {
            temp = Integer.parseInt(res);
            return true;
        } catch (final Exception e) {
            return false;
        }
    }
}

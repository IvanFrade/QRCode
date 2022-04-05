package com.example.qrcode;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

public class scanNFC extends AppCompatActivity {

    private NfcAdapter nfcAdapter; // hardware che permette di accedere all'NFC
    PendingIntent pendingIntent; // tipo promise

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_nfc);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent
                .getActivity(this,
                        0,
                        new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                        0);
    }

    /*
    Devo gestire anche quando la mia applicazione viene riaperta, va in pausa ecc.
    Quindi sovrascrivo i metodi onPause, onResume e onIntent
     */

    @Override
    protected void onResume() {
        super.onResume();

        nfcAdapter.enableForegroundDispatch(this,
                pendingIntent,
                null,
                null);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        resolveIntent(intent);

    }

    private void resolveIntent(Intent intent) {
        String action = intent.getAction(); // azione collegata all'NFC

        if (action.equals(NfcAdapter.ACTION_TAG_DISCOVERED) ||
                action.equals(NfcAdapter.ACTION_TECH_DISCOVERED) ||
                action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            Toast.makeText(this, readContentTag(tag), Toast.LENGTH_LONG).show();
        }
    }

    private String readContentTag(Tag tag) {
        String toReturn = "";
        String[] tagTech = tag.getTechList();

        for (int i = 0; i < tagTech.length; i++) {
            if (tagTech[i].equals(MifareUltralight.class.getName())) {
                MifareUltralight mifareUltralight = MifareUltralight.get(tag);

                try {
                    mifareUltralight.connect();

                    // primi 4 byte non sono da leggere
                    byte[] bs = mifareUltralight.readPages(4);
                    toReturn += new String(bs, 4, 12);

                    for (int k = 8; k < 28; k += 4) {
                        bs = mifareUltralight.readPages(k);

                        int l = 0;
                        while (bs[l] != -2 && l < bs.length) l++;

                        toReturn += new String(bs, 0, l);
                        if (l < bs.length) k = 40;
                    }
                } catch (IOException e) {
                    return e.getMessage();
                }
            }
        }

        return toReturn;
    }
}
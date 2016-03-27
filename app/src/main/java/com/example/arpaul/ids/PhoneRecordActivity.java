package com.example.arpaul.ids;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.arpaul.ids.Common.AppPreference;
import com.example.arpaul.ids.Utilities.ITelephony;
import com.example.arpaul.ids.Utilities.UnCaughtException;

import java.lang.reflect.Method;

public class PhoneRecordActivity extends AppCompatActivity {

    private EditText edtNumber;
    private Button btnSave;
    private AppPreference preference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new UnCaughtException(PhoneRecordActivity.this));
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_phone_record);

        /*************Activity controls fetched*******************/
        initializeControls();

        preference = new AppPreference(PhoneRecordActivity.this);
        String oldNumber = preference.getStringFromPreference(AppPreference.FORWARDING_NUMBER,"");
        if(!TextUtils.isEmpty(oldNumber))
            edtNumber.setText(oldNumber);


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number = edtNumber.getText().toString();
                if(!TextUtils.isEmpty(number) && number.length() > 9 && number.length() < 11){
                    preference.saveStringInPreference(AppPreference.FORWARDING_NUMBER,number);

                    Toast.makeText(PhoneRecordActivity.this, number, Toast.LENGTH_SHORT).show();

                    Toast.makeText(PhoneRecordActivity.this, "Test: "+preference.getStringFromPreference(AppPreference.FORWARDING_NUMBER,""), Toast.LENGTH_SHORT).show();
                }
            }
        });

        checkPermission();

        PhoneCallListener phoneListener = new PhoneCallListener();
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        //callforward();
    }

    Intent intentCallForward;
    private void callforward()
    {
        intentCallForward = new Intent(Intent.ACTION_CALL);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            Toast.makeText(PhoneRecordActivity.this,"Build.VERSION_CODES.M ",Toast.LENGTH_SHORT).show();
            int hasLocationPermission =  ContextCompat.checkSelfPermission(PhoneRecordActivity.this, android.Manifest.permission.CALL_PHONE);
            if( hasLocationPermission != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions((Activity) PhoneRecordActivity.this,new String[]{Manifest.permission.READ_CONTACTS,Manifest.permission.ACCESS_COARSE_LOCATION},11);
            } else {
                String forwardNumber = preference.getStringFromPreference(AppPreference.FORWARDING_NUMBER,"");

                Toast.makeText(PhoneRecordActivity.this," Forwarding to: "+forwardNumber,Toast.LENGTH_SHORT).show();

                try {

                    Uri mmiCode = Uri.fromParts("tel", "**21*"+forwardNumber+"#", "#");
                    /*Uri mmiCode = Uri.fromParts("tel", "**21*7382989305#", "#");*/
                    Toast.makeText(PhoneRecordActivity.this,"No: "+mmiCode.toString(),Toast.LENGTH_LONG).show();
                    intentCallForward.setData(mmiCode);
                    intentCallForward.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intentCallForward);
                } catch (Exception ex) {
                    Toast.makeText(PhoneRecordActivity.this,"Exception: "+ex.toString(),Toast.LENGTH_SHORT).show();
                }


                /*try {
                    TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    Toast.makeText(PhoneRecordActivity.this,"Get getTeleService...",Toast.LENGTH_SHORT).show();
                    Class c = Class.forName(tm.getClass().getName());
                    Method m = c.getDeclaredMethod("getITelephony");
                    m.setAccessible(true);
                    ITelephony telephonyService = (ITelephony) m.invoke(tm);
                    Bundle b = intent.getExtras();
                    incommingNumber = b.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                    if ( incommingNumber.equals(incno1) )
                    {
                        telephonyService = (ITelephony) m.invoke(tm);
                        telephonyService.silenceRinger();
                        telephonyService.endCall();
                        Toast.makeText(PhoneRecordActivity.this,"BYE BYE BYE",Toast.LENGTH_SHORT).show();
                    }
                    else{

                        telephonyService.answerRingingCall();
                        Toast.makeText(PhoneRecordActivity.this,"HELLO HELLO HELLO",Toast.LENGTH_SHORT).show();
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(PhoneRecordActivity.this,"FATAL ERROR: could not connect to telephony subsystem",Toast.LENGTH_SHORT).show();
                    Toast.makeText(PhoneRecordActivity.this,"Exception object: " + e,Toast.LENGTH_SHORT).show();
                }*/
            }
        } else {
            Toast.makeText(PhoneRecordActivity.this,"Less than Build.VERSION_CODES.M ",Toast.LENGTH_SHORT).show();

            String forwardNumber = preference.getStringFromPreference(AppPreference.FORWARDING_NUMBER,"");

            Toast.makeText(PhoneRecordActivity.this," Forwarding to: "+forwardNumber,Toast.LENGTH_SHORT).show();


            Uri mmiCode = Uri.fromParts("tel", "**21*"+forwardNumber+"#", "#");
            /*Uri mmiCode = Uri.fromParts("tel", "**21*7382989305#", "#");*/
            Toast.makeText(PhoneRecordActivity.this,"No: "+mmiCode.toString(),Toast.LENGTH_LONG).show();
            intentCallForward.setData(mmiCode);
            intentCallForward.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentCallForward);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    private class PhoneCallListener extends PhoneStateListener
    {
        private boolean isPhoneCalling = false;

        @Override
        public void onCallStateChanged(int state, String incomingNumber)
        {
            if (TelephonyManager.CALL_STATE_RINGING == state)
            {
                // phone ringing
                Toast.makeText(PhoneRecordActivity.this," Ringing ",Toast.LENGTH_SHORT).show();
                callforward();

            }

            if (TelephonyManager.CALL_STATE_OFFHOOK == state)
            {
                // active
                isPhoneCalling = true;
            }

            if (TelephonyManager.CALL_STATE_IDLE == state)
            {
                // run when class initial and phone call ended, need detect flag
                // from CALL_STATE_OFFHOOK
                if (isPhoneCalling)
                {
                    // restart app
                    /*Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    isPhoneCalling = false;*/
                }
            }
        }
    }

    private int checkPermission(){
        int hasCALL_PHONEPermission = 0;
        int hasREAD_PHONE_STATEPermission = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hasCALL_PHONEPermission = checkSelfPermission(Manifest.permission.CALL_PHONE );
            hasREAD_PHONE_STATEPermission = checkSelfPermission(Manifest.permission.READ_PHONE_STATE );
            if( hasCALL_PHONEPermission != PackageManager.PERMISSION_GRANTED ||
                    hasREAD_PHONE_STATEPermission != PackageManager.PERMISSION_GRANTED) {
                verifyPhoneCall();
            }
        }
        return hasCALL_PHONEPermission;
    }

    private void verifyPhoneCall(){
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CALL_PHONE,Manifest.permission.READ_PHONE_STATE},1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*************Content Provider Loader restarted*******************/
    }

    private void initializeControls(){
        /*************Initialising Screen controls*******************/
        edtNumber = (EditText) findViewById(R.id.edtNumber);
        btnSave = (Button) findViewById(R.id.btnSave);
    }
}

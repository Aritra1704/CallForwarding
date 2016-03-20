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
import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.arpaul.ids.Common.AppPreference;
import com.example.arpaul.ids.Utilities.UnCaughtException;

public class PhoneRecordActivity extends AppCompatActivity {

    private EditText edtNumber;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new UnCaughtException(PhoneRecordActivity.this));
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_phone_record);

        /*************Activity controls fetched*******************/
        initializeControls();

        String oldNumber = new AppPreference(PhoneRecordActivity.this).getStringFromPreference(AppPreference.FORWARDING_NUMBER,"");
        if(!TextUtils.isEmpty(oldNumber))
            edtNumber.setText(oldNumber);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number = edtNumber.getText().toString();
                if(!TextUtils.isEmpty(number) && number.length() > 9 && number.length() < 11){
                    new AppPreference(PhoneRecordActivity.this).saveStringInPreference(AppPreference.FORWARDING_NUMBER,number);

                    Toast.makeText(PhoneRecordActivity.this, number, Toast.LENGTH_SHORT).show();
                }
            }
        });

        checkPermission();

        callforward();
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
                String forwardNumber = new AppPreference(PhoneRecordActivity.this).getStringFromPreference(AppPreference.FORWARDING_NUMBER,"");

                Toast.makeText(PhoneRecordActivity.this," Forwarding to: "+forwardNumber,Toast.LENGTH_SHORT).show();

                PhoneCallListener phoneListener = new PhoneCallListener();
                TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);

                Uri mmiCode = Uri.fromParts("tel", "*21*"+forwardNumber+"#", "#");
                intentCallForward.setData(mmiCode);
                startActivity(intentCallForward);
            }
        } else {
            Toast.makeText(PhoneRecordActivity.this,"Less than Build.VERSION_CODES.M ",Toast.LENGTH_SHORT).show();

            String forwardNumber = new AppPreference(PhoneRecordActivity.this).getStringFromPreference(AppPreference.FORWARDING_NUMBER,"");

            Toast.makeText(PhoneRecordActivity.this," Forwarding to: "+forwardNumber,Toast.LENGTH_SHORT).show();

            PhoneCallListener phoneListener = new PhoneCallListener();
            TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);

            Uri mmiCode = Uri.fromParts("tel", "*21*"+forwardNumber+"#", "#");
            intentCallForward.setData(mmiCode);
            startActivity(intentCallForward);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            String forwardNumber = new AppPreference(PhoneRecordActivity.this).getStringFromPreference(AppPreference.FORWARDING_NUMBER,"");

            Toast.makeText(PhoneRecordActivity.this," Forwarding to: "+forwardNumber,Toast.LENGTH_SHORT).show();

            PhoneCallListener phoneListener = new PhoneCallListener();
            TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);

            Uri mmiCode = Uri.fromParts("tel", "*21*"+forwardNumber+"#", "#");
            intentCallForward.setData(mmiCode);
            startActivity(intentCallForward);
        }
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
                    Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    isPhoneCalling = false;
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
                verifyLocation();
            }
        }
        return hasCALL_PHONEPermission;
    }

    private void verifyLocation(){
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

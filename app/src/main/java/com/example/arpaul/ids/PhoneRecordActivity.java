package com.example.arpaul.ids;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.example.arpaul.ids.DataAccess.PhoneRecordCPConstants;
import com.example.arpaul.ids.Utilities.CalendarUtils;
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
                    Toast.makeText(PhoneRecordActivity.this,"No: "+mmiCode.toString(),Toast.LENGTH_LONG).show();
                    intentCallForward.setData(mmiCode);
                    intentCallForward.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intentCallForward);
                } catch (Exception ex) {
                    Toast.makeText(PhoneRecordActivity.this,"Exception: "+ex.toString(),Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(PhoneRecordActivity.this,"Less than Build.VERSION_CODES.M ",Toast.LENGTH_SHORT).show();

            String forwardNumber = preference.getStringFromPreference(AppPreference.FORWARDING_NUMBER,"");

            Toast.makeText(PhoneRecordActivity.this," Forwarding to: "+forwardNumber,Toast.LENGTH_SHORT).show();

            Uri mmiCode = Uri.fromParts("tel", "**21*"+forwardNumber+"#", "#");
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
        private boolean firstConnect = false;

        @Override
        public void onCallStateChanged(int state, String incomingNumber)
        {
            if (TelephonyManager.CALL_STATE_RINGING == state)
            {
                // phone ringing
                Toast.makeText(PhoneRecordActivity.this," Ringing ",Toast.LENGTH_SHORT).show();
                callforward();

                if(!firstConnect){
                    saveCallList(incomingNumber);
                    firstConnect = true;
                }
            }

            if (TelephonyManager.CALL_STATE_OFFHOOK == state)
            {
                // active
                firstConnect = false;
            }

            if (TelephonyManager.CALL_STATE_IDLE == state)
            {
                // run when class initial and phone call ended, need detect flag
                // from CALL_STATE_OFFHOOK
                firstConnect = false;
            }
        }
    }

    private void saveCallList(String incomingNumber){
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(PhoneRecordCPConstants.CONTENT_URI,new String[]{PhoneRecordCPConstants.COLUMN_CALL_DATETIME},
                    PhoneRecordCPConstants.COLUMN_CONTACT_NO + "= ?",
                    new String[]{incomingNumber},
                    "ORDER BY "+PhoneRecordCPConstants.COLUMN_CALL_DATETIME+" DESC LIMIT 1");

            int diffBetweenTime = 0;
            String currentTime = CalendarUtils.getCurrentDateTime();
            if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
                String lastTime = cursor.getString(cursor.getColumnIndex(PhoneRecordCPConstants.COLUMN_CALL_DATETIME));
                diffBetweenTime = CalendarUtils.getDiffBtwDatesInMinutes(lastTime,currentTime);
            }

            if(diffBetweenTime < 5) {
                callforward();
            }

            ContentValues contentValues = new ContentValues();
            contentValues.put(PhoneRecordCPConstants.COLUMN_CONTACT_NO, incomingNumber);
            contentValues.put(PhoneRecordCPConstants.COLUMN_CONTACT_TYPE, PhoneRecordCPConstants.get_Contact_Type_Call());
            contentValues.put(PhoneRecordCPConstants.COLUMN_CALL_DATETIME, currentTime);
            //Keeping ischecked 0 since its not checked yet.
            contentValues.put(PhoneRecordCPConstants.COLUMN_CHECHED, PhoneRecordCPConstants.get_Not_Checked());

            getContentResolver().insert(PhoneRecordCPConstants.CONTENT_URI, contentValues);

            notifyUser();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void notifyUser(){

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(PhoneRecordActivity.this, PhoneRecordActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //use the flag FLAG_UPDATE_CURRENT to override any notification already there
        PendingIntent contentIntent = PendingIntent.getActivity(PhoneRecordActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(PhoneRecordActivity.this);
        builder.setAutoCancel(false);
        builder.setTicker("New call received.");
        builder.setContentTitle("New call received.");
        builder.setContentText("You have a received a new call.");
        builder.setSmallIcon(R.drawable.call);
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        builder.setLargeIcon(icon);
        builder.setContentIntent(contentIntent);
        builder.setOngoing(true);
        //builder.setNumber(100);
        builder.build();

        Notification notification = builder.getNotification();
        notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND;
        notificationManager.notify(11, notification);

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

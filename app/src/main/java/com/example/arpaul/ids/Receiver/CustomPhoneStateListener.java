package com.example.arpaul.ids.Receiver;

import android.Manifest;
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
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.example.arpaul.ids.Common.AppPreference;
import com.example.arpaul.ids.DataAccess.PhoneRecordCPConstants;
import com.example.arpaul.ids.PhoneRecordActivity;
import com.example.arpaul.ids.R;
import com.example.arpaul.ids.Utilities.CalendarUtils;

/**
 * Created by ARPaul on 04-03-2016.
 */
public class CustomPhoneStateListener extends PhoneStateListener {

    Context context; //Context to make Toast if required
    private static boolean firstConnect = false;
    String str = "";
    private boolean isPhoneCalling = false;
    private AppPreference preference;
    Intent intentCallForward;

    public CustomPhoneStateListener(Context context) {
        super();
        this.context = context;
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        super.onCallStateChanged(state, incomingNumber);

        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
                //when Idle i.e no call
                str = "1";
                firstConnect = false;

                if (isPhoneCalling)
                {
                    // restart app
                    Intent i = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(i);
                    isPhoneCalling = false;
                }
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //when Off hook i.e in call
                str ="2";
                //Make intent and start your service here
                firstConnect = false;
                isPhoneCalling = true;
                break;
            case TelephonyManager.CALL_STATE_RINGING:

                if(!firstConnect){
                    saveCallList(incomingNumber);
                    firstConnect = true;
                }
                break;
            default:
                break;
        }
    }
    private static final int MY_PERMISSION_CALL_PHONE = 11;
    private void saveCallList(String incomingNumber){
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(PhoneRecordCPConstants.CONTENT_URI,new String[]{PhoneRecordCPConstants.COLUMN_CALL_DATETIME},
                    PhoneRecordCPConstants.COLUMN_CONTACT_NO + "= ?",
                    new String[]{incomingNumber},
                    "ORDER BY "+PhoneRecordCPConstants.COLUMN_CALL_DATETIME+"DESC LIMIT 1");

            int diffBetweenTime = 0;
            String currentTime = CalendarUtils.getCurrentDateTime();
            if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
                String lastTime = cursor.getString(cursor.getColumnIndex(PhoneRecordCPConstants.COLUMN_CALL_DATETIME));
                diffBetweenTime = CalendarUtils.getDiffBtwDatesInMinutes(lastTime,currentTime);
            }

            if(diffBetweenTime > 5) {
                preference = new AppPreference(context);
                intentCallForward = new Intent(Intent.ACTION_CALL);

                String forwardNumber = preference.getStringFromPreference(AppPreference.FORWARDING_NUMBER,"");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {
                    Toast.makeText(context,"Build.VERSION_CODES.M ",Toast.LENGTH_SHORT).show();
                    int hasLocationPermission =  ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE);
                    if( hasLocationPermission == PackageManager.PERMISSION_GRANTED ) {

                        Uri mmiCode = Uri.fromParts("tel", "**21*"+forwardNumber+"#", "#");
                        intentCallForward.setData(mmiCode);
                        intentCallForward.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intentCallForward);
                    }
                } else {
                    Uri mmiCode = Uri.fromParts("tel", "**21*"+forwardNumber+"#", "#");
                    intentCallForward.setData(mmiCode);
                    intentCallForward.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intentCallForward);
                }
            }

            ContentValues contentValues = new ContentValues();
            contentValues.put(PhoneRecordCPConstants.COLUMN_CONTACT_NO, incomingNumber);
            contentValues.put(PhoneRecordCPConstants.COLUMN_CONTACT_TYPE, PhoneRecordCPConstants.get_Contact_Type_Call());
            contentValues.put(PhoneRecordCPConstants.COLUMN_CALL_DATETIME, currentTime);
            //Keeping ischecked 0 since its not checked yet.
            contentValues.put(PhoneRecordCPConstants.COLUMN_CHECHED, PhoneRecordCPConstants.get_Not_Checked());

            context.getContentResolver().insert(PhoneRecordCPConstants.CONTENT_URI, contentValues);

            notifyUser();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void notifyUser(){

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(context, PhoneRecordActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //use the flag FLAG_UPDATE_CURRENT to override any notification already there
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(context);
        builder.setAutoCancel(false);
        builder.setTicker("New call received.");
        builder.setContentTitle("New call received.");
        builder.setContentText("You have a received a new call.");
        builder.setSmallIcon(R.drawable.call);
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        builder.setLargeIcon(icon);
        builder.setContentIntent(contentIntent);
        builder.setOngoing(true);
        //builder.setNumber(100);
        builder.build();

        Notification notification = builder.getNotification();
        notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND;
        notificationManager.notify(11, notification);

    }
}

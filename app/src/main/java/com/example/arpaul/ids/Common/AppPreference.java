package com.example.arpaul.ids.Common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by ARPaul on 19-03-2016.
 */
public class AppPreference {

    private SharedPreferences preferences;
    private SharedPreferences.Editor edit;

    public static final String FORWARDING_NUMBER                =	"FORWARDING_NUMBER";
    public static final String LAST_PHOTO 						=	"LAST_PHOTO";

    public AppPreference(Context context)
    {
        preferences		=	PreferenceManager.getDefaultSharedPreferences(context);
        edit			=	preferences.edit();
    }

    public void saveStringInPreference(String strKey,String strValue)
    {
        edit.putString(strKey, strValue);
        edit.commit();
    }

    public void removeFromPreference(String strKey)
    {
        edit.remove(strKey);
    }

    public void commitPreference()
    {
        edit.commit();
    }

    public String getStringFromPreference(String strKey,String defaultValue )
    {
        return preferences.getString(strKey, defaultValue);
    }
}

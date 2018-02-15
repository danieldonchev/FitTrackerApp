package com.daniel.FitTrackerApp.synchronization;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;

import com.daniel.FitTrackerApp.helpers.DBHelper;
import com.daniel.FitTrackerApp.helpers.PreferencesHelper;
import com.daniel.FitTrackerApp.provider.ProviderContract;

import javax.net.ssl.HttpsURLConnection;

public class SyncHelper {

    public static final String IS_USER_NEW = "is_new";

    public static void requestManualSync(Context context, boolean isNew)
    {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(IS_USER_NEW, isNew);

        Account syncAccount = null;
        Account[] accounts = AccountManager.get(context).getAccounts();
        for(Account account : accounts)
        {
            if(account.name.equals(PreferencesHelper.getInstance().getCurrentUserEmail(context)))
            {
                syncAccount = account;
            }
        }

        boolean isActive = ContentResolver.isSyncActive(syncAccount, ProviderContract.CONTENT_AUTHORITY);
        if(!isActive){
            ContentResolver.setIsSyncable(syncAccount, ProviderContract.CONTENT_AUTHORITY, 1);
            //ContentResolver.setSyncAutomatically(syncAccount, ProviderContract.CONTENT_AUTHORITY, true);
            ContentResolver.requestSync(syncAccount, ProviderContract.CONTENT_AUTHORITY, bundle);
        }
        //ContentResolver.cancelSync(syncAccount, ProviderContract.CONTENT_AUTHORITY);

    }

    public static void cancelSync(Context context){
        Account syncAccount = null;
        Account[] accounts = AccountManager.get(context).getAccounts();
        for(Account account : accounts)
        {
            if(account.name.equals(PreferencesHelper.getInstance().getCurrentUserEmail(context)))
            {
                syncAccount = account;
            }
        }

        ContentResolver.cancelSync(syncAccount, ProviderContract.CONTENT_AUTHORITY);
    }


    public static void shouldSync(Context context, HttpsURLConnection connection){
        if(connection.getHeaderField("Should-Sync") != null)
        {
            if(Boolean.parseBoolean(connection.getHeaderField("Should-Sync")))
            {
                SyncHelper.requestManualSync(context, false);
            } else {
                if(connection.getHeaderField("Sync-Time") != null){
                    updateLastSyncTime(context, Long.parseLong(connection.getHeaderField("Sync-Time")));
                }
            }

        } else {
            if(connection.getHeaderField("Sync-Time") != null){
                updateLastSyncTime(context, Long.parseLong(connection.getHeaderField("Sync-Time")));
            }
        }
    }

    private static void updateLastSyncTime(Context context, long timestamp)
    {
        DBHelper.getInstance().updateLastModifiedTime(context,
                PreferencesHelper.getInstance().getCurrentUserId(context),
                ProviderContract.SyncEntry.LAST_SYNC,
                timestamp);
    }
}

package com.daniel.FitTrackerApp.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.daniel.FitTrackerApp.App;
import com.daniel.FitTrackerApp.Mail;
import com.daniel.FitTrackerApp.helpers.PreferencesHelper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class AccountUtils
{
    public static final long getDefaultBirthday()
    {
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.YEAR, 2000);

        return calendar.getTimeInMillis();
    }

    public static int convertMsTimeToAge(long birthday)
    {
        Calendar currentDate = Calendar.getInstance();
        Calendar birthdayCalendar = Calendar.getInstance();
        birthdayCalendar.setTimeInMillis(birthday);

        int years = currentDate.get(Calendar.YEAR) - birthdayCalendar.get(Calendar.YEAR);
        if((currentDate.get(Calendar.MONTH) > birthdayCalendar.get(Calendar.MONTH)) ||
                (currentDate.get(Calendar.MONTH) == birthdayCalendar.get(Calendar.MONTH) && currentDate.get(Calendar.DAY_OF_MONTH) > birthdayCalendar.get(Calendar.DAY_OF_MONTH)))
        {
            years--;
        }
        return years;
    }

    public static void changeSplit(Context context, String unit, float split) {
        if (!unit.equals("")) {
            if (split > 0) {
                if (unit.equals("meters")) {
                    PreferencesHelper.getInstance().setDistanceSplit(context, split, unit);
                }
                if (unit.equals("km")) {
                    PreferencesHelper.getInstance().setDistanceSplit(context, split * 1000, unit);
                }
                if (unit.equals("feet")) {
                    PreferencesHelper.getInstance().setDistanceSplit(context, (float)(split * 0.3048), unit);
                }
                if (unit.equals("yards")) {
                    PreferencesHelper.getInstance().setDistanceSplit(context, (float) (split * 0.9144), unit);
                }
                if (unit.equals("miles")) {
                    PreferencesHelper.getInstance().setDistanceSplit(context, (float) (split * 1609.344), unit);
                }

            } else {
                Toast.makeText(context, "Invalid value", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "Enter value pls", Toast.LENGTH_SHORT).show();
        }
    }



    //AbstractWorkout
    public static void resetDefaultUncaughtExceptionHandler() {
        PreferencesHelper.getInstance().isDefaultHandler(false);
        Thread.setDefaultUncaughtExceptionHandler(App.defaultHandler);
    }

    public static void setDefaultUncaughtExceptionHandler() {
        PreferencesHelper.getInstance().isDefaultHandler(true);
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                Writer writer = new StringWriter();
                PrintWriter printWriter = new PrintWriter(writer);
                e.printStackTrace(printWriter);
                String error = writer.toString();
                SendMail sM = new SendMail(error);
                try {
                    sM.execute().get();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
//                Intent crashedIntent = new Intent(getApplicationContext(), CrashActivity.class);
//                crashedIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(crashedIntent);
                //Toast.makeText(App.this, "Make toast great again", Toast.LENGTH_LONG).show();
//                String string = SportActivityTrackingService.recordData.getAverageSpeedString() + "\r\n ";
//                string += Log.getStackTraceString(e);
//                String filesDir = getContext().getFilesDir().getAbsolutePath();
//                File file = new File(Environment.getExternalStorageDirectory() + "/errorrr.txt");
//                String files = Environment.getDataDirectory().getAbsolutePath();
//                try
//                {
//                    FileOutputStream outputStream = new FileOutputStream(file);
//                    outputStream.write(string.getBytes());
//                    outputStream.close();
//                }
//                catch (Exception ew)
//                {
//                    ew.printStackTrace();
//                }
//
//                e.printStackTrace();

            }
        });
    }

    private static class SendMail extends AsyncTask<Void, Void, Void> {
        String error;

        public SendMail(String error) {
            this.error = error;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Mail m = new Mail("testotestov666@gmail.com", "testotestov");

            String[] addr = {"didonne7@gmail.com"};
            m.setTo(addr);
            m.setFrom("woo@woo.com");
            m.setSubject("Error");
            m.setBody(error);
            try {
                m.send();
            } catch (Exception ex) {
                ex.printStackTrace();
                //android.os.Process.killProcess(android.os.Process.myPid());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //android.os.Process.killProcess(android.os.Process.myPid());
        }
    }
}

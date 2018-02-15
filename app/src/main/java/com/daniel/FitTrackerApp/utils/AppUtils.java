package com.daniel.FitTrackerApp.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.util.Base64;
import android.util.DisplayMetrics;

import com.daniel.FitTrackerApp.services.DownloadImageService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;

public class AppUtils
{
    public static boolean isServiceRunning(Context context, Class<?> serviceClass)
    {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (serviceClass.getName().equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return false;
    }

    public static String encodeTobase64(Bitmap image)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();

        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    public static Bitmap decodeBase64(String input)
    {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory
                .decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    public static boolean hasLollipop()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static SpannableString formatStyles(Context context, String value, String sub0, int style0, String sub1, int style1)
    {
        String tag0 = "{0}";
        int startLocation0 = value.indexOf(tag0);
        value = value.replace(tag0, sub0);

        String tag1 = "{1}";
        int startLocation1 = value.indexOf(tag1);
        if (sub1 != null && !sub1.equals(""))
        {
            value = value.replace(tag1, sub1);
        }
        SpannableString styledText = new SpannableString(value);
        styledText.setSpan(new TextAppearanceSpan(context, style0), startLocation0, startLocation0 + sub0.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (sub1 != null && !sub1.equals(""))
        {
            styledText.setSpan(new TextAppearanceSpan(context, style1), startLocation1, startLocation1 + sub1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return styledText;
    }

    public static void cacheBitmap(Context context, Bitmap bitmap, String name, String path)
    {
        if(bitmap != null)
        {
            try
            {
                File f = new File(context.getCacheDir(), path);
                if(!f.exists())
                {
                    if(f.mkdir())
                    {
                        File file = new File(f.getPath(), name);
                        FileOutputStream out = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                        out.close();
                    }
                }
                else
                {
                    File file = new File(f.getPath(), name);
                    FileOutputStream out = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.close();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static Bitmap getBitmapFromCache(Context context, String bitmapName, String path)
    {
        File cacheDir = new File(context.getCacheDir(), path);
        File cacheFile = new File(cacheDir, bitmapName);
        if(cacheFile.exists())
        {
            return BitmapFactory.decodeFile(cacheFile.getPath());
        }
        return null;
    }

    public static void startImageDownload(Context context, String url, String name, String path, IntentServiceResultReceiver.Receiver receiver)
    {
        IntentServiceResultReceiver mReceiver = new IntentServiceResultReceiver(new Handler());
        mReceiver.setReceiver(receiver);

        Intent intent = new Intent(context, DownloadImageService.class);

        intent.putExtra("url", url);
        intent.putExtra("name", name);
        intent.putExtra("path", path);
        intent.putExtra("receiver", mReceiver);
        intent.putExtra("requestId", 101);

        context.startService(intent);
    }

    public static void writeBitmap(ObjectOutputStream out, Bitmap bitmap) throws IOException
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] array = stream.toByteArray();
        out.writeInt(array.length);
        out.write(array);
    }

    public static Bitmap readBitmap(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        int bufferLength = in.readInt();

        byte[] byteArray = new byte[bufferLength];

        int pos = 0;
        do {
            int read = in.read(byteArray, pos, bufferLength - pos);

            if (read != -1) {
                pos += read;
            } else {
                break;
            }

        } while (pos < bufferLength);

        return BitmapFactory.decodeByteArray(byteArray, 0, bufferLength);
    }

    public static double roundToHalf(double d)
    {
        return  Math.round(d * 2) / 2.0;
    }



    public static String readStream(InputStream is)
    {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = is.read();
            while(i != -1) {
                bo.write(i);
                i = is.read();
            }
            return bo.toString();
        } catch (IOException e) {
            return "";
        }
    }

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null)
            return false;

        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static String doubleToString(double var) {
        return String.format(java.util.Locale.getDefault(), "%.2f", var);
    }

    public static byte[] readFully(InputStream var0, int var1, boolean var2) throws IOException {
//        byte[] var3 = new byte[0];
//        if(var1 == -1) {
//            var1 = 2147483647;
//        }
//
//        int var6;
//        for(int var4 = 0; var4 < var1; var4 += var6) {
//            int var5;
//            if(var4 >= var3.length) {
//                var5 = Math.min(var1 - var4, var3.length + 1024);
//                if(var3.length < var4 + var5) {
//                    var3 = Arrays.copyOf(var3, var4 + var5);
//                }
//            } else {
//                var5 = var3.length - var4;
//            }
//
//            var6 = var0.read(var3, var4, var5);
//            if(var6 < 0) {
//                if(var2 && var1 != 2147483647) {
//                    throw new EOFException("Detect premature EOF");
//                }
//
//                if(var3.length != var4) {
//                    var3 = Arrays.copyOf(var3, var4);
//                }
//                break;
//            }
//        }


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read = 0;
        while ((read = var0.read(buffer, 0, buffer.length)) != -1) {
            baos.write(buffer, 0, read);
        }

        return baos.toByteArray();
    }

    public static String convertSecondsToString(long seconds) {
        long h = seconds / 3600;
        long rem = seconds % 3600;
        long m = rem / 60;
        long s = rem % 60;
        return String.format(java.util.Locale.getDefault(), "%02d:%02d:%02d", h, m, s);
    }

    public static int pxToDp(Context context, int px) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static double stepLength(int height, char gender){
        if(gender == 'm'){
            return  height * 0.415 / 100;
        } else if(gender == 'f'){
            return height * 0.413 / 100;
        }
        return 0;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
}

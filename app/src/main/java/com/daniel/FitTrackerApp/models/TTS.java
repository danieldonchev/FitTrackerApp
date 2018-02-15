package com.daniel.FitTrackerApp.models;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

public class TTS implements TextToSpeech.OnInitListener
{
    private TextToSpeech textToSpeech;
    private static final float speechPitch = 0.69f;
    private static final float speechRate = 0.69f;
    private String speech;

    public TTS(Context context, String speech)
    {
        this.speech = speech;
        textToSpeech = new TextToSpeech(context, this);
    }

    @Override
    public void onInit(int status)
    {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.getDefault());
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
            {
                Log.e("DEBUG", "Language Not Supported");
            } else
            {
                textToSpeech.setSpeechRate(speechRate);
                textToSpeech.setPitch(speechPitch);
                textToSpeech.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
            }
        } else
        {
            Log.i("DEBUG", "MISSION FAILED");
        }
    }
}

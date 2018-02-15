package com.daniel.FitTrackerApp;

import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the AbstractWorkout Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {

        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);
        calendar.set(Calendar.MONTH, 4);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.YEAR, 2017);

        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY)
        {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }
        calendar.setTimeInMillis(System.currentTimeMillis());

        int a = calendar.get(Calendar.YEAR);
        int c = calendar.get(Calendar.MONTH);
        int b = calendar.get(Calendar.DAY_OF_MONTH);
        int d = calendar.get(Calendar.HOUR_OF_DAY);
        System.out.print(b);
        assertEquals(b, 26);
    }
}
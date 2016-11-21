package com.redeyesoftware.pronto;

import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by George on 20/11/2016.
 */

public class TimeStampConverter {

    public static String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        String dayOfMonth = DateFormat.format("dd", cal).toString();
        String month = DateFormat.format("MM", cal).toString();

        Calendar today = Calendar.getInstance(Locale.ENGLISH);
        String dayOfMonthTODAY = DateFormat.format("dd", today).toString();
        String monthTODAY = DateFormat.format("MM", today).toString();

        String date;
        if (dayOfMonth.equals(dayOfMonthTODAY) && monthTODAY.equals(month)) {
            date = DateFormat.format("hh:mm a", cal).toString();
        } else if (Integer.parseInt(dayOfMonth)+1==Integer.parseInt(dayOfMonthTODAY) && monthTODAY.equals(month)) {
            date = DateFormat.format("'Yesterday' hh:mm a", cal).toString();
        } else {
            date = DateFormat.format("MMM dd hh:mm a", cal).toString();
        }
        return date;
    }
}

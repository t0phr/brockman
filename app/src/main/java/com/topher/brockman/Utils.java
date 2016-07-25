package com.topher.brockman;

import android.text.format.DateUtils;
import com.jayway.jsonpath.JsonPath;
import com.topher.brockman.api.TSchau;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by topher on 16/07/16.
 */
public class Utils {

    private Utils() {}

    public static final DateFormat dateFormatIn =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.000'ZZZZ");
    public static final DateFormat dateFormatDuration =
            new SimpleDateFormat("mm:ss 'min'");
    public static final DateFormat dateFormatCards =
            new SimpleDateFormat("HH:mm", Locale.getDefault());
    public static final String[] WEEKDAYS = {
        "Sonntag", "Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag"
    };

    public static String loadJSONFromUrl(String location) {
        if(  location == "" ) {
            return null;
        }

        URL url;
        BufferedReader reader = null;
        HttpURLConnection urlConnection;
        StringBuffer json = new StringBuffer();

        try {
            url = new URL(location);
            urlConnection = (HttpURLConnection) url.openConnection();
            reader = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream()));
            for (String line; (line = reader.readLine()) != null;) {
                json.append(line);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return json.toString();
    }

    public static String getContentDescription(TSchau video) {
        String day;

        Calendar c1 = Calendar.getInstance(); // today
        c1.add(Calendar.DAY_OF_YEAR, -1); // yesterday

        Calendar c2 = Calendar.getInstance();
        c2.setTime(video.getDate()); // your date

        String weekday = WEEKDAYS[c2.get(Calendar.DAY_OF_WEEK) - 1];

        if (c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)) {
            day = "gestern";
        } else if (c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR) - 1) {
            day = "heute";
        }

        return weekday + ", " +
                Utils.dateFormatCards.format(video.getDate()) + " Uhr, " +
                Utils.dateFormatDuration.format(
                        new Date(1000 * video.getDuration()));
    }

    public static String extractLatestBroadcast(String location) {
        String json = loadJSONFromUrl(location);
        if (json != null) {
            String results = JsonPath.read(json,
                    "$..latestBroadcastsPerType[0].details");
            return results;
        }

        return null;
    }

    public static int convertLengthString(String s) {
        int duration = 0;
        String regex = "^([0-9]{2}):([0-9]{2}):([0-9]{2}).[0-9]{2}";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(s);

        if (m.matches()) {
            duration += Integer.parseInt(m.group(1)) * 3600;
            duration += Integer.parseInt(m.group(2)) * 60;
            duration += Integer.parseInt(m.group(3));
        }

        return duration;
    }
}

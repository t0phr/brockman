package com.topher.brockman;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.jayway.jsonpath.JsonPath;
import com.topher.brockman.api.Broadcast;
import com.topher.brockman.api.Tagesschau24;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by topher on 16/07/16.
 */
public class Utils {

    private Utils() {}

    public static final DateFormat dFBroadcastInput =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.000'ZZZZ");
    public static final DateFormat dFDuration =
            new SimpleDateFormat("mm:ss 'min'");
    public static final DateFormat dFBroadcastTime =
            new SimpleDateFormat("HH:mm", Locale.getDefault());
    public static final DateFormat dFLiveStreamInput =
            new SimpleDateFormat("HH:mm");
    public static final DateFormat dFLiveStreamBroadcastTime =
            new SimpleDateFormat("HH:mm",
                    Locale.getDefault());

    private static final String[] WEEKDAYS = {
        "Sonntag", "Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag"
    };

    public static <T> T parseJsonToClass(String url, Class<T> c)
            throws IOException {
        final String TAG = "parseJsonToClass";

        T t;
        Gson gson = new Gson();

        try {
            String json = Utils.loadJSONFromUrl(url);
            t = gson.fromJson(json, c);
        } catch (JsonSyntaxException e) {
            Log.w(TAG, "Answer from server for url "
                    + url
                    + " is not in JSON Syntax.");
            throw e;
        } catch (FileNotFoundException e) {
            Log.w(TAG, "File at url "
                    + url
                    + " not found.");
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
            Log.w(TAG, "Unknown error occurred while trying to access "
                    + url);
            throw e;
        }

        if (t != null) return t;
        else throw new IOException();

    }

    public static String loadJSONFromUrl(String location) throws IOException {
        final String TAG = "loadJSONFromUrl";

        if(  location == "" ) {
            return null;
        }

        URL url;
        BufferedReader reader = null;
        HttpURLConnection urlConnection;
        StringBuffer json = new StringBuffer();

        url = new URL(location);
        urlConnection = (HttpURLConnection) url.openConnection();
        reader = new BufferedReader(new InputStreamReader(
                urlConnection.getInputStream()));
        for (String line; (line = reader.readLine()) != null;) {
            json.append(line);
        }

        return json.toString();
    }

    public static String getContentDescription(Broadcast video) {
        String day;

        Calendar c1 = Calendar.getInstance(); // today
        c1.add(Calendar.DAY_OF_YEAR, -1); // yesterday

        Calendar c2 = Calendar.getInstance();
        c2.setTime(video.getDate()); // your date

        day = WEEKDAYS[c2.get(Calendar.DAY_OF_WEEK) - 1];

        if (c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)) {
            day = "gestern";
        } else if (c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR) - 1) {
            day = "heute";
        }

        return day + ", " +
                Utils.dFBroadcastTime.format(video.getDate()) + " Uhr, " +
                Utils.dFDuration.format(
                        new Date(1000 * video.getDuration()));
    }

    public static String getContentDescription(Tagesschau24 video) {
        return "Nächste Sendung " + Utils.dFLiveStreamBroadcastTime
                .format(video.getNextStreamDate()) + " Uhr";
    }

    public static String extractLatestBroadcast(String location)
            throws IOException {
        String json = loadJSONFromUrl(location);
        return JsonPath.read(json,
                "$..latestBroadcastsPerType[0].details");
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

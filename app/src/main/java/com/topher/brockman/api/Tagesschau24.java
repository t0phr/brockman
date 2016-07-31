package com.topher.brockman.api;

import android.util.Log;
import com.topher.brockman.Utils;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * Created by topher on 31/07/16.
 */
public class Tagesschau24 implements Playable {
    private static final String TAG = "Tagesschau24";

    public String type;
    public String title;
    public String start; // 17:00
    public String end; // 17:15
    public String imgUrl;
    public String videoUrl;

    public List<Link> mediadata;

    public class Link implements Serializable {
        public String m3u8_A_high;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVideoUrl() {
        for (Link l : mediadata) {
            if (l.m3u8_A_high != null)
                return l.m3u8_A_high;
        }

        return null;
    }

    @Override
    public int getDuration() {
        return -1;
    }

    @Override
    public boolean isLiveStream() {
        return true;
    }

    public Date getNextStreamDate() {
        try {
            return Utils.dFLiveStreamInput.parse(start);
        } catch (ParseException e) {
            Log.e(TAG, "Could not parse date string. " +  start);
        }

        return new Date(0);
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}

package com.topher.brockman.api;

import com.topher.brockman.Utils;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by topher on 17/07/16.
 */
public class TSchau implements Serializable {
    private String videoUrl;
    private String imgUrl;
    private String date;

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public Date getDate() {
        // "2016-07-20T16:00:00.000+02:00"

        try {
            return Utils.dateFormatIn.parse(date);
        } catch (ParseException e) {
            System.err.println("Could not parse date string! " + date);
        }

        return new Date(0);

    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getDuration() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    private int length;
}

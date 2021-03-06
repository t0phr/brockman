package com.topher.brockman.api;

import java.util.List;

/**
 * Created by topher on 17/07/16.
 */
public class BroadcastDetails {
    public String broadcastTitle;
    public String broadcastDate;
    public String type;
    public String details;
    public List<APIVideo> fullvideo;
    public List<APIVideo> videos;
    public List<APIImage> images;

    public String getImageUrl() {
        try {
            return images.get(0).getImageUrl();
        } catch (NullPointerException e) {
            System.err.println("Image URL not found in " +
                    "BroadcastDetails object!");
        }

        return null;
    }

    public String getVideoUrl() {
        try {
            return fullvideo.get(0).getLarge();
        } catch (NullPointerException e) {
            System.err.println("Video URL not found in " +
                    "BroadcastDetails object!");
        }

        return null;
    }

    public String getVideoDuration() {
        try {
            return fullvideo.get(0).out;
        } catch (NullPointerException e) {
            System.err.println("Duration string not found in " +
                    "BroadcastDetails object!");
        }

        return null;
    }
}

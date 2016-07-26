package com.topher.brockman.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by topher on 17/07/16.
 */
public class Video {
    public String headline;
    public String in;
    public String out;
    public String type;
    public String details;
    public String broadcastDate;

    public List<Mediadatum> mediadata;
    public List<Image> images;

    private MediadataList internalMediadata;

    public class Mediadatum {
        public String h264s;
        public String h264m;
        public String h264l;
        public String h264sm;
        public String h264ml;
        public String h264xl;
        public String podcastvideom;
        public String podcastvideo;
        public String adaptivestreaming;
    }

    public String getH264xl() {
        if (internalMediadata == null) {
            internalMediadata = new MediadataList();
            internalMediadata.addAll(mediadata);
        }

        return internalMediadata.getH264xl();
    }

    private class MediadataList extends ArrayList<Mediadatum> {
        public String getH264xl() {
            for (Mediadatum m : this) {
                if (m.h264xl != null)
                    return m.h264xl;
            }

            return null;
        }
    }
}

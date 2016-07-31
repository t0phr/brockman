package com.topher.brockman.api;

import java.io.Serializable;
import java.util.List;

/**
 * Created by topher on 16/07/16.
 */
public class API implements Serializable {
    public String type;
    public String details;
    public List<Media> multimedia;

    public class Media {
        List<Tagesschau24> livestreams;
    }

    public Tagesschau24 getTagesschau24() {
        return multimedia.get(0).livestreams.get(0);
    }
}
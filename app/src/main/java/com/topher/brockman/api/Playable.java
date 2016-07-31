package com.topher.brockman.api;

import java.io.Serializable;

/**
 * Created by topher on 31/07/16.
 */
public interface Playable extends Serializable {
    String getVideoUrl();

    String getImgUrl();

    String getTitle();

    int getDuration();

    boolean isLiveStream();
}

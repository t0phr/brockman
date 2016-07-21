package com.topher.brockman.api;

import java.io.Serializable;
import java.util.List;

/**
 * Created by topher on 16/07/16.
 */
public class API implements Serializable {
    public String broadcastType;
    public String broadcastTitle;
    public List<Object> broadcastsPerTypeUrls;
    public List<Broadcast> latestBroadcastsPerType;
}
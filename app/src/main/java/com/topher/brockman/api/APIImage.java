package com.topher.brockman.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by topher on 17/07/16.
 */
public class APIImage {
    public List<ImageVariant> variants;
    public String type;
    public String details;

    private ImageList internalImage;

    public class ImageVariant {
        /*
        public String modPremium;
        public String modPremiumHalb;
        public String teaserRelaunch;
        public String teaserM;
        public String klein16x9;
        public String mittel16x9;
        public String gross16x9;
        public String videowebs;
        public String videowebm; */
        public String videowebl;
    }

    public String getVideoWebL() {
        if (internalImage == null) {
            internalImage = new ImageList();
            internalImage.addAll(variants);
        }

        return internalImage.getVideoWebL();
    }

    private class ImageList extends ArrayList<ImageVariant> {
        public String getVideoWebL() {
            for (ImageVariant iv : this) {
                if (iv.videowebl != null) {
                    return iv.videowebl;
                }
            }

            return null;
        }
    }
}

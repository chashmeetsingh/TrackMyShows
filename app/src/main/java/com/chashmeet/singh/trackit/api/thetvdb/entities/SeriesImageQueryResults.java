package com.chashmeet.singh.trackit.api.thetvdb.entities;

import java.util.List;

public class SeriesImageQueryResults {

    public List<SeriesImageQueryResult> data;

    public class SeriesImageQueryResult {

        public int id;
        public String fileName;
        public RatingsInfo ratingsInfo;

        public class RatingsInfo {
            public Double average;
        }

    }
}

package com.example.my_movie_app.dto.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)

public class CinemaJson {

    private String name;
    private String address;
    private String description;
    private Double latitude;
    private Double longitude;

    private Region region;
    private Cineplex cineplex;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Region {
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Cineplex {
        private String name;
        private Logo logo;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Logo {
        private Sizes sizes;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Sizes {
        private String square;
    }

}
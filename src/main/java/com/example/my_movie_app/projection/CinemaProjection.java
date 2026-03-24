package com.example.my_movie_app.projection;

import java.util.UUID;

public interface CinemaProjection {
    UUID getId();
    String getName();
    String getAddress();
    Double getLatitude();
    Double getLongitude();
    Double getDistance();
    String getLogoUrl();
}

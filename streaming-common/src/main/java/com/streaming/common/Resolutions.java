package com.streaming.common;

import java.util.List;

/** The bitrate ladder. Add a rung here and the workflow + master playlist pick it up. */
public final class Resolutions {

    public static final List<Resolution> LADDER = List.of(
            new Resolution(1920, 1080, 2000, "1080p"),
            new Resolution(1280, 720, 1000, "720p"),
            new Resolution(960, 540, 500, "480p"),
            new Resolution(640, 360, 400, "360p"));

    private Resolutions() {}
}

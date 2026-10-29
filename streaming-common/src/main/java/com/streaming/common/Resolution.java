package com.streaming.common;

/** One rung of the bitrate ladder, e.g. 1280x720 @ 1000 kbps labelled "720p". */
public record Resolution(int width, int height, int bitRateKbps, String label) {}

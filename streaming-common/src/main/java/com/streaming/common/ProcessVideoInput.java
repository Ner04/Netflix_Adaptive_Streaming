package com.streaming.common;

/** Paths are relative to MEDIA_ROOT so the API and the worker can run on different machines. */
public record ProcessVideoInput(String videoId, String inputRelativePath, String outputRelativePath) {}

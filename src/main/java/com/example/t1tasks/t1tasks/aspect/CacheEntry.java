package com.example.t1tasks.t1tasks.aspect;

public class CacheEntry {
    private final Object value;
    private final long timestamp;
    private final long timeToLive;

    public CacheEntry(Object value, long timestamp, long timeToLive) {
        this.value = value;
        this.timestamp = timestamp;
        this.timeToLive = timeToLive;
    }

    public Object getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getTimeToLive() {
        return timeToLive;
    }
}
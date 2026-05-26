package com.example.myaudioplayer;

import java.util.Locale;
import java.util.Objects;

public class MusicFiles {

    private String path;
    private String title;
    private String artist;
    private String album;
    private long duration; // Duration in milliseconds
    private String id;

    // Full Constructor — FIXED: Added String id to parameters
    public MusicFiles(String path, String title, String artist,
                      String album, long duration, String id) {
        this.path = path;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.id = id;
    }

    // Empty Constructor
    public MusicFiles() {
    }

    // ---------------- GETTERS & SETTERS ----------------

    public String getPath() {
        return path != null ? path : "";
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title != null && !title.isEmpty()
                ? title
                : "Unknown Title";
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist != null && !artist.isEmpty()
                ? artist
                : "Unknown Artist";
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album != null && !album.isEmpty()
                ? album
                : "Unknown Album";
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    // FIXED: Changed return type to long to match the field type
    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // ---------------- FORMAT DURATION ----------------

    /**
     * Converts raw millisecond duration to an MM:SS formatted string
     * for UI display.
     */
    public String getFormattedDuration() {
        long totalSeconds = duration / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        return String.format(Locale.getDefault(),
                "%02d:%02d",
                minutes,
                seconds);
    }

    // ---------------- toString ----------------

    @Override
    public String toString() {
        return getTitle() + " - " + getArtist();
    }

    // ---------------- equals & hashCode ----------------

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        MusicFiles that = (MusicFiles) obj;
        return Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
}
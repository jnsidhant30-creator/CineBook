package com.movieticket.model;

/**
 * TmdbCrewMember.java — Represents a crew member from TMDB.
 */
public class TmdbCrewMember {
    private String name;
    private String job;
    private String profilePath;

    public TmdbCrewMember(String name, String job, String profilePath) {
        this.name = name;
        this.job = job;
        this.profilePath = profilePath;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getJob() { return job; }
    public void setJob(String job) { this.job = job; }

    public String getProfilePath() { return profilePath; }
    public void setProfilePath(String profilePath) { this.profilePath = profilePath; }
}

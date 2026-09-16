package com.movieticket.model;

/**
 * TmdbCastMember.java — Represents a cast member from TMDB.
 */
public class TmdbCastMember {
    private String name;
    private String character;
    private String profilePath;

    public TmdbCastMember(String name, String character, String profilePath) {
        this.name = name;
        this.character = character;
        this.profilePath = profilePath;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCharacter() { return character; }
    public void setCharacter(String character) { this.character = character; }

    public String getProfilePath() { return profilePath; }
    public void setProfilePath(String profilePath) { this.profilePath = profilePath; }
}

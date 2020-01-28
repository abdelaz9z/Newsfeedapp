package com.example.newsfeedapp;

import android.graphics.Bitmap;

/**
 * An {@link News} object contains information related to a single article.
 */
public class News {

    private Bitmap thumbnail;
    private String webTitle;
    private String trailText;
    private String byline;
    private String sectionName;
    private String webPublicationDate;
    private String webUrl;

    /**
     * Constructs a new {@link News} object
     *
     * @param thumbnail          Url to the thumbnail of the article
     * @param webTitle           Title of the article
     * @param trailText          TrailText of the article
     * @param byline             Author of the article
     * @param sectionName        Section for the article
     * @param webPublicationDate Publication date for the article
     * @param webUrl             Url of the article
     */
    public News(Bitmap thumbnail, String webTitle, String trailText, String byline, String sectionName, String webPublicationDate, String webUrl) {
        this.thumbnail = thumbnail;
        this.webTitle = webTitle;
        this.trailText = trailText;
        this.byline = byline;
        this.sectionName = sectionName;
        this.webPublicationDate = webPublicationDate;
        this.webUrl = webUrl;
    }

    public Bitmap getThumbnail() {
        return thumbnail;
    }

    public String getWebTitle() {
        return webTitle;
    }

    public String getTrailText() {
        return trailText;
    }

    public String getByline() {
        return byline;
    }

    public String getSectionName() {
        return sectionName;
    }

    public String getWebPublicationDate() {
        return webPublicationDate;
    }

    public String getWebUrl() {
        return webUrl;
    }
}

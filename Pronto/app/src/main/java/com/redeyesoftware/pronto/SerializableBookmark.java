package com.redeyesoftware.pronto;

import java.io.Serializable;

/**
 * Created by George on 24/11/2016.
 */

public class SerializableBookmark implements Serializable {
    private String messageID = "";
    private String message = "";
    private String author;
    private String date;
    private int likes = 0;
    private boolean iLiked = false;
    private int bookmarks = 0;
    private String attachment = "";

    public SerializableBookmark(String messageID, String message, String author, String date, int likes, boolean iLiked, int bookmarks, String attachment) {
        this.messageID = messageID;
        this.message = message;
        this.author = author;
        this.date = date;
        this.likes = likes;
        this.iLiked = iLiked;
        this.bookmarks = bookmarks;
        this.attachment = attachment;
    }

    public boolean isiLiked() {
        return iLiked;
    }

    public String getMessage() {
        return message;
    }

    public String getAuthor() {
        return author;
    }

    public String getDate() {
        return date;
    }

    public int getLikes() {
        return likes;
    }

    public int getBookmarks() {
        return bookmarks;
    }

    public String getMessageID() {
        return messageID;
    }

    public String getAttachment() {
        return attachment;
    }
}

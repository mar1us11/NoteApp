package com.noteapptoy.noteapptoy;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PostNoteRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    public PostNoteRequest() {
    }

    public PostNoteRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}

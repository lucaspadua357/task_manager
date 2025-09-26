package br.inatel.lucas.model;

import java.util.UUID;

public class Task {
    private final String id;
    private String title;
    private String description;
    private boolean done;

    public Task(String title, String description) {
        this.id = UUID.randomUUID().toString();
        setTitle(title);         // já usa o setter com validação
        this.description = description;
        this.done = false;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public boolean isDone() { return done; }

    public void setTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        this.title = title.trim();
    }

    public void setDescription(String description) {
        if (description == null) {
            throw new IllegalArgumentException("Description cannot be null");
        }
        this.description = description;
    }

    public void markDone() { this.done = true; }
}
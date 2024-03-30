package com.example.hackathonmacroeconomics;

public class Message {
    private String text;
    private boolean isUser; // true for user messages, false for ChatGPT

    public Message(String text, boolean isUser) {
        this.text = text;
        this.isUser = isUser;
    }

    public String getText() {
        return text;
    }

    public boolean isUser() {
        return isUser;
    }

    @Override
    public String toString() {
        return "Message{" +
                "text='" + text + '\'' +
                ", isUser=" + isUser +
                '}';
    }
}


package com.example.demo.appdemo;

import android.support.annotation.NonNull;

public class Request {

    private String sender;
    private String receiver;
    private String text;
    private String responsePlaylist;

    public Request()
    {}

    public Request(String sender, String receiver, String text)
    {
        this.sender = sender;
        this.receiver = receiver;
        this.text = text;
        this.responsePlaylist = "";
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @NonNull
    @Override
    public String toString() {
        return "from: " + sender + ", to: " + receiver + ", message: " + text + ", response: " + responsePlaylist;
    }
}

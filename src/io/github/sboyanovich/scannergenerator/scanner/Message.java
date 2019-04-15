package io.github.sboyanovich.scannergenerator.scanner;

public class Message {
    private boolean isError;
    private String text;

    public Message(boolean isError, String text) {
        this.isError = isError;
        this.text = text;
    }

    public boolean isError() {
        return isError;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return (this.isError ? "ERROR: " : "WARNING: ") + this.text;
    }
}

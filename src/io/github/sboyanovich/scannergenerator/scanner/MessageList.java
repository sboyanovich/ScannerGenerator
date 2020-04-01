package io.github.sboyanovich.scannergenerator.scanner;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

public class MessageList {
    private SortedMap<Position, Message> messages;
    private int warningCount;
    private int errorCount;

    public MessageList() {
        this.messages = new TreeMap<>();
    }

    public int getWarningCount() {
        return warningCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void addError(Position coord, String text) {
        this.messages.put(coord, new Message(true, text));
        errorCount++;
    }

    public void addWarning(Position coord, String text) {
        this.messages.put(coord, new Message(false, text));
        warningCount++;
    }

    public SortedMap<Position, Message> getSorted() {
        return Collections.unmodifiableSortedMap(this.messages);
    }
}

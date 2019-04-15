package io.github.sboyanovich.scannergenerator.scanner;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

public class MessageList {
    private SortedMap<Position, Message> messages;

    public MessageList() {
        this.messages = new TreeMap<>();
    }

    public void addError(Position coord, String text) {
        this.messages.put(coord, new Message(true, text));
    }

    public void addWarning(Position coord, String text) {
        this.messages.put(coord, new Message(false, text));
    }

    public SortedMap<Position, Message> getSorted() {
        return Collections.unmodifiableSortedMap(this.messages);
    }
}

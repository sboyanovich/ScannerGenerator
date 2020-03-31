package io.github.sboyanovich.scannergenerator.generated;

import io.github.sboyanovich.scannergenerator.scanner.Message;
import io.github.sboyanovich.scannergenerator.scanner.MessageList;
import io.github.sboyanovich.scannergenerator.scanner.NameDictionary;
import io.github.sboyanovich.scannergenerator.scanner.Position;

import java.util.SortedMap;

public class MockCompiler {
    private MessageList messages;
    private NameDictionary names;
    private int alphabetSize;

    public MockCompiler(int alphabetSize) {
        this.messages = new MessageList();
        this.names = new NameDictionary();
        this.alphabetSize = alphabetSize;
    }

    public int getWarningCount() {
        return this.messages.getWarningCount();
    }

    public int getErrorCount() {
        return this.messages.getErrorCount();
    }

    public int getAlphabetSize() {
        return this.alphabetSize;
    }

    public void addError(Position coord, String text) {
        this.messages.addError(coord, text);
    }

    public void addWarning(Position coord, String text) {
        this.messages.addWarning(coord, text);
    }

    public SortedMap<Position, Message> getSortedMessages() {
        return this.messages.getSorted();
    }

    public boolean contains(String s) {
        return this.names.contains(s);
    }

    public int addName(String name) {
        return this.names.addName(name);
    }

    public String getName(int code) {
        return this.names.getName(code);
    }
}

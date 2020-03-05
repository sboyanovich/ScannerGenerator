package io.github.sboyanovich.scannergenerator.tests;

import io.github.sboyanovich.scannergenerator.scanner.*;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;

public class ProCompiler {
    private MessageList messages;
    private NameDictionary names;
    private List<LexicalRecognizer> recognizers;

    public ProCompiler(List<LexicalRecognizer> recognizers) {
        this.messages = new MessageList();
        this.names = new NameDictionary();
        this.recognizers = recognizers;
    }

    public ProScanner getScanner(String program, Map<StateTag, Integer> modeSwitches) {
        return new ProScanner(program, this, this.recognizers, modeSwitches);
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

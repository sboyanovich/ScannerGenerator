package io.github.sboyanovich.scannergenerator.lex;

import io.github.sboyanovich.scannergenerator.Position;

import java.util.SortedMap;

public class Compiler {
    private MessageList messages;
    private NameDictionary names;
    private LexicalRecognizer dfa;

    public Compiler(LexicalRecognizer dfa) {
        this.messages = new MessageList();
        this.names = new NameDictionary();
        this.dfa = dfa;
    }

    public Scanner getScanner(String program) {
        return new Scanner(program, this, this.dfa);
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

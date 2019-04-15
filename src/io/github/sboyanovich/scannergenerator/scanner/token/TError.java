package io.github.sboyanovich.scannergenerator.scanner.token;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;

public class TError extends Token {
    private String contents;

    public TError(Fragment coords, String contents) {
        super(coords, DomainError.ERROR);
        this.contents = contents;
    }

    public String getContents() {
        return contents;
    }

    @Override
    public String toString() {
        return super.toString() + contents;
    }
}
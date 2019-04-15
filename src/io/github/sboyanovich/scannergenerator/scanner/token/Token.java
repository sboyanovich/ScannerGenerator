package io.github.sboyanovich.scannergenerator.scanner.token;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;

public abstract class Token {
    private Fragment coords;
    private Domain tag;

    public Token(Fragment coords, Domain tag) {
        this.coords = coords;
        this.tag = tag;
    }

    public Fragment getCoords() {
        return coords;
    }

    public Domain getTag() {
        return tag;
    }

    @Override
    public String toString() {
        return this.tag + " " + this.coords + ": ";
    }
}

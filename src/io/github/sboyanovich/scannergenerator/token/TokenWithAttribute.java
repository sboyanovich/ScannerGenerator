package io.github.sboyanovich.scannergenerator.token;

import io.github.sboyanovich.scannergenerator.Fragment;

/**
 * Standard base class for tokens with attribute.
 * */
public class TokenWithAttribute<T> extends Token {
    private T attribute;

    public TokenWithAttribute(Fragment coords, DomainWithAttribute<T> tag, T attribute) {
        super(coords, tag);
        this.attribute = attribute;
    }

    public T getAttribute() {
        return this.attribute;
    }

    @Override
    public String toString() {
        return super.toString() + this.attribute;
    }
}

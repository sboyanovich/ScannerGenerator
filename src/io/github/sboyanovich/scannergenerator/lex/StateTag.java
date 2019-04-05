package io.github.sboyanovich.scannergenerator.lex;

import io.github.sboyanovich.scannergenerator.token.Domain;

public interface StateTag {
    boolean isFinal();
    Domain getDomain();
}

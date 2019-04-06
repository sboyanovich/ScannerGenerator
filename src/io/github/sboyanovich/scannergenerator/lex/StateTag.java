package io.github.sboyanovich.scannergenerator.lex;

import io.github.sboyanovich.scannergenerator.token.Domain;

// TODO: Remove isFinal() -> standard implementation for non final state will be provided

public interface StateTag {
    boolean isFinal();
    Domain getDomain();
}

package io.github.sboyanovich.scannergenerator.token;

import io.github.sboyanovich.scannergenerator.Fragment;

/** Most basic, attributeless token. Useful for singleton lexical domain like keywords, op signs, parens etc.*/
public class BasicToken extends Token {
    public BasicToken(Fragment coords, Domain tag) {
        super(coords, tag);
    }
}

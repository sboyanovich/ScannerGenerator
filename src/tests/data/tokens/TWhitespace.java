package tests.data.tokens;

import lab.Fragment;
import lab.token.Token;
import tests.data.domains.SimpleDomains;

public class TWhitespace extends Token {
    public TWhitespace(Fragment coords) {
        super(coords, SimpleDomains.WHITESPACE);
    }
}

package lab.lex;

import lab.token.Domain;

public interface StateTag {
    boolean isFinal();
    Domain getDomain();
}

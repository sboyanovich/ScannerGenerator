package io.github.sboyanovich.scannergenerator.token;

import io.github.sboyanovich.scannergenerator.Fragment;
import io.github.sboyanovich.scannergenerator.lex.Text;

public interface DomainWithAttribute<T> extends Domain {
    T attribute(Text text, Fragment fragment);
}

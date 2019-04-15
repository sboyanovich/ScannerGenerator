package io.github.sboyanovich.scannergenerator.scanner.token;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.scanner.Text;

public interface DomainWithAttribute<T> extends Domain {
    T attribute(Text text, Fragment fragment);
}

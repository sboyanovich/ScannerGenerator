package io.github.sboyanovich.scannergenerator.scanner.token;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.scanner.Text;
import io.github.sboyanovich.scannergenerator.utility.Utility;

public enum DomainError implements DomainWithAttribute<String> {
    ERROR {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new TError(fragment, attribute(text, fragment));
        }

        // TODO: Factor the common part somewhere out
        @Override
        public String attribute(Text text, Fragment fragment) {
            return Utility.getTextFragmentAsString(text, fragment);
        }
    }
}

package io.github.sboyanovich.scannergenerator.scanner.token;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.scanner.Text;
import io.github.sboyanovich.scannergenerator.utility.Utility;

/**
 * Should be implemented with an enum!
 * <p>
 * There generally should be error domain.
 * <p>
 * There should be end of program domain.
 * <p>
 * Both of these should be unique and should not coincide.
 */
public interface Domain {
    Token createToken(Text text, Fragment fragment);

    DomainWithAttribute<String> ERROR = new DomainWithAttribute<>() {
        @Override
        public TokenWithAttribute<String> createToken(Text text, Fragment fragment) {
            return new TokenWithAttribute<>(fragment, ERROR, attribute(text, fragment));
        }

        @Override
        public String attribute(Text text, Fragment fragment) {
            return Utility.getTextFragmentAsString(text, fragment);
        }

        @Override
        public String toString() {
            return "ERROR";
        }
    };

    Domain END_OF_INPUT = new Domain() {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, END_OF_INPUT);
        }

        @Override
        public String toString() {
            return "END_OF_INPUT";
        }
    };
}

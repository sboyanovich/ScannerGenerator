package io.github.sboyanovich.scannergenerator.generated;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.scanner.Text;
import io.github.sboyanovich.scannergenerator.scanner.token.DomainWithAttribute;
import io.github.sboyanovich.scannergenerator.scanner.token.TokenWithAttribute;

import static io.github.sboyanovich.scannergenerator.utility.Utility.asCodePoint;
import static io.github.sboyanovich.scannergenerator.utility.Utility.getTextFragmentAsString;

public enum DomainsWithIntegerAttribute implements DomainWithAttribute<Integer> {
    CHAR {
        @Override
        public Integer attribute(Text text, Fragment fragment) {
            String s = getTextFragmentAsString(text, fragment);
            switch (s) {
                case "\\b":
                    return asCodePoint("\b");
                case "\\t":
                    return asCodePoint("\t");
                case "\\n":
                    return asCodePoint("\n");
                case "\\f":
                    return asCodePoint("\f");
                case "\\r":
                    return asCodePoint("\r");
                default:
                    if (s.startsWith("\\U+")) {
                        if (s.codePointAt(3) == asCodePoint("#")) {
                            return Integer.parseInt(s.substring(4));
                        } else {
                            return Integer.parseInt(s.substring(3), 16);
                        }
                    } else if (s.startsWith("\\")) {
                        return asCodePoint(s.substring(1));
                    } else {
                        return asCodePoint(s);
                    }
            }
        }

        @Override
        public TokenWithAttribute<Integer> createToken(Text text, Fragment fragment) {
            return new TokenWithAttribute<>(fragment, CHAR, attribute(text, fragment));
        }
    }
}
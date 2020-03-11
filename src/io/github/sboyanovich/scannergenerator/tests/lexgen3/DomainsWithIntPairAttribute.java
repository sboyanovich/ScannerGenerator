package io.github.sboyanovich.scannergenerator.tests.lexgen3;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.scanner.Text;
import io.github.sboyanovich.scannergenerator.scanner.token.DomainWithAttribute;
import io.github.sboyanovich.scannergenerator.scanner.token.TokenWithAttribute;

import static io.github.sboyanovich.scannergenerator.utility.Utility.getTextFragmentAsString;

public enum DomainsWithIntPairAttribute implements DomainWithAttribute<IntPair> {
    REPETITION_OP {
        @Override
        public IntPair attribute(Text text, Fragment fragment) {
            String s = getTextFragmentAsString(text, fragment);
            s = s.substring(1, s.length() - 1);

            int pos = s.indexOf(",");

            int a, b;

            if (pos == -1) {
                a = Integer.parseInt(s);
                b = a;
            } else if (pos + 1 < s.length()) {
                a = Integer.parseInt(s.substring(0, pos));
                b = Integer.parseInt(s.substring(pos + 1));
            } else {
                a = Integer.parseInt(s.substring(0, pos));
                b = -1;
            }
            return new IntPair(a, b);
        }

        @Override
        public TokenWithAttribute<IntPair> createToken(Text text, Fragment fragment) {
            return new TokenWithAttribute<>(fragment, REPETITION_OP, attribute(text, fragment));
        }
    }
}

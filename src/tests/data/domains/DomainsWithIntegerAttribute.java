package tests.data.domains;

import lab.Fragment;
import lab.lex.Text;
import lab.token.DomainWithAttribute;
import lab.token.Token;
import tests.data.tokens.TIntegerLiteral;
import utility.Utility;

public enum DomainsWithIntegerAttribute implements DomainWithAttribute<Integer> {
    INTEGER_LITERAL {
        @Override
        public Integer attribute(Text text, Fragment fragment) {
            return Integer.parseInt(
                    Utility.getTextFragmentAsString(text, fragment)
            );
        }

        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new TIntegerLiteral(fragment, attribute(text, fragment));
        }
    }
}
package tests.data.tokens;

import lab.Fragment;
import lab.token.Token;
import tests.data.domains.DomainsWithIntegerAttribute;

public class TIntegerLiteral extends Token {

    private int attribute;

    public TIntegerLiteral(Fragment coords, int attribute) {
        super(coords, DomainsWithIntegerAttribute.INTEGER_LITERAL);
        this.attribute = attribute;
    }

    public int getAttribute() {
        return attribute;
    }

    @Override
    public String toString() {
        return super.toString() + attribute;
    }
}

package tests.data.tokens;

import lab.Fragment;
import lab.token.Token;
import tests.data.domains.DomainsWithStringAttribute;

public class TKeyword extends Token {
    private String attribute;

    public TKeyword(Fragment coords, String attribute) {
        super(coords, DomainsWithStringAttribute.KEYWORD);
        this.attribute = attribute;
    }

    public String getAttribute() {
        return attribute;
    }

    @Override
    public String toString() {
        return super.toString() + attribute;
    }
}

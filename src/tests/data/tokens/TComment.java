package tests.data.tokens;

import lab.Fragment;
import lab.token.Token;
import tests.data.domains.DomainsWithStringAttribute;

public class TComment extends Token {
    private String attribute;

    public TComment(Fragment coords, String attribute) {
        super(coords, DomainsWithStringAttribute.COMMENT);
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

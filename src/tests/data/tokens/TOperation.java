package tests.data.tokens;

import lab.Fragment;
import lab.token.Token;
import tests.data.domains.DomainsWithStringAttribute;

public class TOperation extends Token {
    private String attribute;

    public TOperation(Fragment coords, String attribute) {
        super(coords, DomainsWithStringAttribute.OPERATION);
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

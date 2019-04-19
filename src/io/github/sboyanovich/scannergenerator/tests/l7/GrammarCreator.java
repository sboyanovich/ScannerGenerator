package io.github.sboyanovich.scannergenerator.tests.l7;

import io.github.sboyanovich.scannergenerator.scanner.token.TokenWithAttribute;
import io.github.sboyanovich.scannergenerator.tests.l7.aux.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static io.github.sboyanovich.scannergenerator.tests.data.domains.DomainsWithStringAttribute.AXM_DECL;
import static io.github.sboyanovich.scannergenerator.tests.data.domains.DomainsWithStringAttribute.TERMINAL;

public class GrammarCreator {
    private ParseTree tree;
    private Map<String, Integer> terminalNames;
    private Map<String, Integer> nonTerminalNames;
    private List<CFGProduction> productions;
    private int tCounter;
    private int ntCounter;
    private int axiom;

    public GrammarCreator(ParseTree tree) {
        this.tree = tree;
        this.terminalNames = new HashMap<>();
        this.nonTerminalNames = new HashMap<>();
        this.productions = new ArrayList<>();
        this.tCounter = 0;
        this.ntCounter = 0;
        this.axiom = -1;
    }

    // GRAMMAR
        /*
            0	<lang> 		    := <rule> <rule_list> .
            1	<rule_list>	    := <rule> <rule_list> | .
            2	<rule>		    := <lhs> EQUALS <rhs_list> DOT .
            3	<rhs_list>	    := <rhs> <rhs_list_c> .
            4	<rhs_list_c>	:= VERTICAL_BAR <rhs_list> | .
            5	<rhs>		    := <t> <rhs> | .
            6	<t>		        := TERMINAL | NON_TERMINAL .
            7	<lhs>		    := NON_TERMINAL | AXM_DECL .
        */
    // tree is known to concern this grammar
    public CFGrammar createGrammar() throws GrammarCreationException {
        ParseTree.NonTerminalNode root = this.tree.getRoot();
        ParseTree.NonTerminalNode ruleCNode = (ParseTree.NonTerminalNode) root.getChildren().get(0);
        ParseTree.NonTerminalNode rlCNode = (ParseTree.NonTerminalNode) root.getChildren().get(1);
        rule(ruleCNode);
        ruleList(rlCNode);

        if (this.axiom < 0) {
            throw new GrammarCreationException("No axiom defined!");
        }

        CFGrammarBuilder builder = new CFGrammarBuilder(this.ntCounter, this.tCounter, this.axiom);
        for (CFGProduction production : this.productions) {
            builder.addProduction(production);
        }

        Map<Integer, String> taiMap = new HashMap<>(Utility.inverseMap(this.terminalNames));
        Map<Integer, String> ntaiMap = new HashMap<>(Utility.inverseMap(this.nonTerminalNames));

        Function<Integer, String> nativeTai = taiMap::get;
        Function<Integer, String> nativeNtai = ntaiMap::get;

        return builder.build(nativeTai, nativeNtai);
    }

    private void ruleList(ParseTree.NonTerminalNode rlNode) throws GrammarCreationException {
        if (rlNode.getChildren().size() > 0) {
            ParseTree.NonTerminalNode ruleCNode =
                    (ParseTree.NonTerminalNode) rlNode.getChildren().get(0);
            ParseTree.NonTerminalNode rlCNode =
                    (ParseTree.NonTerminalNode) rlNode.getChildren().get(1);

            rule(ruleCNode);
            ruleList(rlCNode);
        }
    }

    private void rule(ParseTree.NonTerminalNode rNode) throws GrammarCreationException {
        ParseTree.NonTerminalNode lhsCNode = (ParseTree.NonTerminalNode) rNode.getChildren().get(0);
        ParseTree.NonTerminalNode rhslCNode = (ParseTree.NonTerminalNode) rNode.getChildren().get(2);
        int nt = lhs(lhsCNode);
        List<UAString> rhsStrings = rhsList(rhslCNode);
        for (UAString rhs : rhsStrings) {
            this.productions.add(new CFGProduction(nt, rhs));
        }
    }

    private int lhs(ParseTree.NonTerminalNode lhsNode) throws GrammarCreationException {
        ParseTree.TerminalNode lhs = (ParseTree.TerminalNode) lhsNode.getChildren().get(0);
        TokenWithAttribute token = (TokenWithAttribute) lhs.getSymbol();
        // this will work
        String name = (String) token.getAttribute();
        int index = addToNonTerminalNamesTable(name);
        if (token.getTag() == AXM_DECL) {
            if (this.axiom == -1) {
                this.axiom = index;
            } else {
                throw new GrammarCreationException("Axiom is being redefined!");
            }
        }
        return index;
    }

    private List<UAString> rhsList(ParseTree.NonTerminalNode rhslNode) {
        ParseTree.NonTerminalNode rhsCNode = (ParseTree.NonTerminalNode) rhslNode.getChildren().get(0);
        ParseTree.NonTerminalNode rhslcCNode = (ParseTree.NonTerminalNode) rhslNode.getChildren().get(1);
        List<UAString> result = new ArrayList<>();
        result.add(rhs(rhsCNode));
        result.addAll(rhsListC(rhslcCNode));
        return result;
    }

    private UAString rhs(ParseTree.NonTerminalNode rhsNode) {
        if (rhsNode.getChildren().isEmpty()) {
            return new UAString(new ArrayList<>());
        }
        ParseTree.NonTerminalNode tCNode = (ParseTree.NonTerminalNode) rhsNode.getChildren().get(0);
        ParseTree.NonTerminalNode rhsCNode = (ParseTree.NonTerminalNode) rhsNode.getChildren().get(1);

        UnifiedAlphabetSymbol ts = t(tCNode);
        UAString rs = rhs(rhsCNode);

        return rs.prepend(ts);
    }

    private UnifiedAlphabetSymbol t(ParseTree.NonTerminalNode tNode) {
        ParseTree.TerminalNode leaf = (ParseTree.TerminalNode) tNode.getChildren().get(0);
        TokenWithAttribute token = (TokenWithAttribute) leaf.getSymbol();
        String name = (String) token.getAttribute();
        if (leaf.getSymbol().getTag() == TERMINAL) {
            return new UnifiedAlphabetSymbol(addToTerminalNamesTable(name), true);
        } else {
            return new UnifiedAlphabetSymbol(addToNonTerminalNamesTable(name), false);
        }
    }

    private List<UAString> rhsListC(ParseTree.NonTerminalNode rhslcNode) {
        if (rhslcNode.getChildren().isEmpty()) {
            return new ArrayList<>();
        }
        ParseTree.NonTerminalNode rhslCNode = (ParseTree.NonTerminalNode) rhslcNode.getChildren().get(1);
        return rhsList(rhslCNode);
    }

    // adds name to table if absent and returns mapped index
    private int addToNonTerminalNamesTable(String name) {
        name = "(" + name + ")";
        if (!this.nonTerminalNames.containsKey(name)) {
            int index = this.ntCounter;
            this.nonTerminalNames.put(name, this.ntCounter++);
            return index;
        }
        return this.nonTerminalNames.get(name);
    }

    // adds name to table if absent and returns mapped index
    private int addToTerminalNamesTable(String name) {
        if (!this.terminalNames.containsKey(name)) {
            int index = this.tCounter;
            this.terminalNames.put(name, this.tCounter++);
            return index;
        }
        return this.terminalNames.get(name);
    }
}

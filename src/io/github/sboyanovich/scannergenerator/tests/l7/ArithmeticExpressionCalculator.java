package io.github.sboyanovich.scannergenerator.tests.l7;

import io.github.sboyanovich.scannergenerator.scanner.token.TokenWithAttribute;

public class ArithmeticExpressionCalculator {
    public static int evaluate(ParseTree expression) {
        return e(expression.getRoot());
    }

    private static int e(ParseTree.NonTerminalNode eNode) {
        ParseTree.NonTerminalNode tCNode = (ParseTree.NonTerminalNode) eNode.getChildren().get(0);
        ParseTree.NonTerminalNode e1CNode = (ParseTree.NonTerminalNode) eNode.getChildren().get(1);
        int t = t(tCNode);
        if (!e1CNode.getChildren().isEmpty()) {
            return e1(t, e1CNode);
        } else {
            return t;
        }
    }

    private static int t(ParseTree.NonTerminalNode tNode) {
        ParseTree.NonTerminalNode fCNode = (ParseTree.NonTerminalNode) tNode.getChildren().get(0);
        ParseTree.NonTerminalNode t1CNode = (ParseTree.NonTerminalNode) tNode.getChildren().get(1);

        int f = f(fCNode);
        if (!t1CNode.getChildren().isEmpty()) {
            return t1(f, t1CNode);
        } else {
            return f;
        }
    }

    private static int f(ParseTree.NonTerminalNode fNode) {
        if (fNode.getChildren().size() == 1) {
            ParseTree.TerminalNode nNode = (ParseTree.TerminalNode) fNode.getChildren().get(0);
            TokenWithAttribute n = (TokenWithAttribute) nNode.getSymbol();
            return (Integer) n.getAttribute();
        } else {
            return e((ParseTree.NonTerminalNode) fNode.getChildren().get(1));
        }
    }

    // called only for non-nulled t1
    private static int t1(int left, ParseTree.NonTerminalNode t1Node) {
        ParseTree.NonTerminalNode fCNode = (ParseTree.NonTerminalNode) t1Node.getChildren().get(1);
        ParseTree.NonTerminalNode t1CNode = (ParseTree.NonTerminalNode) t1Node.getChildren().get(2);
        int f = f(fCNode);
        if (t1CNode.getChildren().isEmpty()) {
            return left * f;
        } else {
            return t1(left * f, t1CNode);
        }
    }

    private static int e1(int left, ParseTree.NonTerminalNode e1Node) {
        ParseTree.NonTerminalNode tCNode = (ParseTree.NonTerminalNode) e1Node.getChildren().get(1);
        ParseTree.NonTerminalNode e1CNode = (ParseTree.NonTerminalNode) e1Node.getChildren().get(2);
        int t = t(tCNode);
        if (e1CNode.getChildren().isEmpty()) {
            return left + t;
        } else {
            return e1(left + t, e1CNode);
        }
    }

}

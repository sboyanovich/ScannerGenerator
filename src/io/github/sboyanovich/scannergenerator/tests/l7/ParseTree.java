package io.github.sboyanovich.scannergenerator.tests.l7;

import io.github.sboyanovich.scannergenerator.scanner.token.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static io.github.sboyanovich.scannergenerator.utility.Utility.*;

/* Mutable */
public class ParseTree {
    interface Node {
        int getNumber();
    }

    static class TerminalNode implements Node {
        private Token symbol;
        private int number;

        public TerminalNode(Token symbol, int number) {
            this.symbol = symbol;
            this.number = number;
        }

        public Token getSymbol() {
            return symbol;
        }

        public void setSymbol(Token symbol) {
            this.symbol = symbol;
        }

        @Override
        public int getNumber() {
            return this.number;
        }
    }

    static class NonTerminalNode implements Node {
        private int symbol;
        private int number;
        private List<Node> children;

        public NonTerminalNode(int symbol, int number) {
            this.symbol = symbol;
            this.number = number;
            this.children = new ArrayList<>();
        }

        public List<Node> getChildren() {
            return children;
        }

        public void setChildren(List<Node> children) {
            this.children = children;
        }

        public int getSymbol() {
            return this.symbol;
        }

        @Override
        public int getNumber() {
            return this.number;
        }
    }

    private NonTerminalNode root;

    public NonTerminalNode getRoot() {
        return this.root;
    }

    public ParseTree(int nonTerminal) {
        this.root = new NonTerminalNode(nonTerminal, 0);
    }

    // recursive for now
    private StringBuilder visit(
            Node node,
            StringBuilder accum,
            Function<Integer, String> nonTerminalAlphabetInterpretation
    ) {
        accum.append(TAB).append(node.getNumber());
        if (node instanceof TerminalNode) {
            accum.append(SPACE + "[label=\"");
            accum.append(((TerminalNode) node).getSymbol().getTag());
            accum.append("\"]").append(NEWLINE);
        } else {
            NonTerminalNode ntNode = (NonTerminalNode) node;

            accum.append(SPACE + "[label=\"");
            int nonTerminal = ntNode.getSymbol();
            accum.append(nonTerminalAlphabetInterpretation.apply(nonTerminal));
            accum.append("\"]").append(NEWLINE);

            for (Node child : ntNode.getChildren()) {
                accum.append(TAB).append(node.getNumber())
                        .append(SPACE + DOT_ARROW + SPACE)
                        .append(child.getNumber())
                        .append(NEWLINE);
            }

            for (Node child : ntNode.getChildren()) {
                visit(child, accum, nonTerminalAlphabetInterpretation);
            }
        }
        return accum;
    }

    public String toGraphvizDotString(Function<Integer, String> nonTerminalAlphabetInterpretation) {
        String result = "digraph parseTree {" +
                NEWLINE +
                TAB + "rankdir=TD;" + NEWLINE +
                visit(this.root, new StringBuilder(), nonTerminalAlphabetInterpretation) +
                "}" + NEWLINE;
        return result;
    }
}

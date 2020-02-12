package io.github.sboyanovich.scannergenerator.tests.gok;

import java.util.List;

import static io.github.sboyanovich.scannergenerator.utility.Utility.*;

public abstract class IRNode {
    private IRNode() {

    }

    int number;

    abstract StringBuilder dotVisit();

    final String toGraphVizDotString() {
        return ("digraph ast {" + NEWLINE +
                TAB + "rankdir=TD;" + NEWLINE) +
                dotVisit().toString() +
                "}" + NEWLINE;
    }

    static StringBuilder labelNode(int number, String label) {
        StringBuilder result = new StringBuilder();
        result.append(TAB).append(number).append(SPACE + "[label=\"").append(label).append("\"]" + NEWLINE);
        return result;
    }

    static StringBuilder edgeString(int from, int to) {
        StringBuilder result = new StringBuilder();
        result.append(TAB)
                .append(from)
                .append(SPACE + DOT_ARROW + SPACE)
                .append(to)
                .append(NEWLINE);
        return result;
    }

    public static class Func extends IRNode {
        String name;
        Args args;
        Body body;

        @Override
        StringBuilder dotVisit() {
            StringBuilder result = new StringBuilder();
            result.append(IRNode.labelNode(number, "func " + name));
            result.append(edgeString(number, args.number));
            result.append(edgeString(number, body.number));
            result.append(args.dotVisit());
            result.append(body.dotVisit());
            return result;
        }
    }

    public static class Args extends IRNode {
        List<String> argNames;

        @Override
        StringBuilder dotVisit() {
            StringBuilder result = new StringBuilder();
            result.append(labelNode(number, "Args: " + argNames.toString()));
            return result;
        }
    }

    public static class Body extends IRNode {
        List<Statement> statements;

        @Override
        StringBuilder dotVisit() {
            StringBuilder result = new StringBuilder();
            result.append(labelNode(number, "Body"));
            for (Statement s : statements) {
                result.append(edgeString(number, s.number));
            }
            for (Statement s : statements) {
                result.append(s.dotVisit());
            }
            return result;
        }
    }

    public static class BoolExpr extends IRNode {
        public enum BoolOp {
            CMP,
            EQUALS
        }

        Expression lhs;
        Expression rhs;
        BoolOp operator;

        @Override
        StringBuilder dotVisit() {
            StringBuilder result = new StringBuilder();
            String label = operator == BoolOp.CMP ? "LESS THAN" : "EQUALS";
            result.append(labelNode(number, label));
            result.append(edgeString(number, lhs.number));
            result.append(edgeString(number, rhs.number));
            result.append(lhs.dotVisit());
            result.append(rhs.dotVisit());
            return result;
        }
    }

    public static abstract class Statement extends IRNode {
        public static class IfStmt extends Statement {
            BoolExpr condition;
            Body body;
            Body elseBody; // can be null

            @Override
            StringBuilder dotVisit() {
                StringBuilder result = new StringBuilder();
                result.append(labelNode(number, "IF"));
                result.append(edgeString(number, condition.number));
                result.append(edgeString(number, body.number));
                if (elseBody != null) {
                    result.append(edgeString(number, elseBody.number));
                }
                result.append(condition.dotVisit());
                result.append(body.dotVisit());
                if (elseBody != null) {
                    result.append(elseBody.dotVisit());
                }
                return result;
            }
        }

        public static class ForStmt extends Statement {
            VarMut init; // can be null
            BoolExpr condition; // can be null (means forever)
            VarMut after; // can be null
            Body body;

            @Override
            StringBuilder dotVisit() {
                StringBuilder result = new StringBuilder();
                String label = condition != null ? "FOR" : "FOR(EVER)";
                result.append(labelNode(number, label));
                if (init != null) {
                    result.append(edgeString(number, init.number));
                }
                if (condition != null) {
                    result.append(edgeString(number, condition.number));
                }
                if (after != null) {
                    result.append(edgeString(number, after.number));
                }
                result.append(edgeString(number, body.number));

                if (init != null) {
                    result.append(init.dotVisit());
                }
                if (condition != null) {
                    result.append(condition.dotVisit());
                }
                if (after != null) {
                    result.append(after.dotVisit());
                }
                result.append(body.dotVisit());

                return result;
            }
        }

        public static class RetStmt extends Statement {
            Expression result;

            @Override
            StringBuilder dotVisit() {
                StringBuilder result = new StringBuilder();
                result.append(labelNode(number, "RETURN"));
                result.append(edgeString(number, this.result.number));
                result.append(this.result.dotVisit());
                return result;
            }
        }

        public static abstract class VarMut extends Statement {
            String varName;

            public static class Assignment extends VarMut {
                Expression assignedExpr;

                @Override
                StringBuilder dotVisit() {
                    StringBuilder result = new StringBuilder();
                    result.append(labelNode(number, "ASSIGN to variable " + varName));
                    result.append(edgeString(number, assignedExpr.number));
                    result.append(assignedExpr.dotVisit());
                    return result;
                }
            }

            public static class Inc extends VarMut {
                @Override
                StringBuilder dotVisit() {
                    StringBuilder result = new StringBuilder();
                    result.append(labelNode(number, "INCREMENT variable " + varName));
                    return result;
                }
            }

            public static class Dec extends VarMut {
                @Override
                StringBuilder dotVisit() {
                    StringBuilder result = new StringBuilder();
                    result.append(labelNode(number, "DECREMENT variable " + varName));
                    return result;
                }
            }
        }
    }

    public static abstract class Expression extends IRNode {
        public static class Var extends Expression {
            String name;

            @Override
            StringBuilder dotVisit() {
                StringBuilder result = new StringBuilder();
                result.append(labelNode(number, name));
                return result;
            }
        }

        public static class Num extends Expression {
            int value;

            @Override
            StringBuilder dotVisit() {
                StringBuilder result = new StringBuilder();
                result.append(labelNode(number, String.valueOf(value)));
                return result;
            }
        }

        public static class Sum extends Expression {
            List<Expression> operands;

            @Override
            StringBuilder dotVisit() {
                StringBuilder result = new StringBuilder();
                result.append(labelNode(number, "+"));
                for (Expression e : operands) {
                    result.append(edgeString(number, e.number));
                }
                for (Expression e : operands) {
                    result.append(e.dotVisit());
                }
                return result;
            }
        }

        public static class Neg extends Expression {
            Expression operand;

            @Override
            StringBuilder dotVisit() {
                StringBuilder result = new StringBuilder();
                result.append(labelNode(number, "-"));
                result.append(edgeString(number, operand.number));
                result.append(operand.dotVisit());
                return result;
            }
        }

        public static class Product extends Expression {
            List<Expression> operands;

            @Override
            StringBuilder dotVisit() {
                StringBuilder result = new StringBuilder();
                result.append(labelNode(number, "*"));
                for (Expression e : operands) {
                    result.append(edgeString(number, e.number));
                }
                for (Expression e : operands) {
                    result.append(e.dotVisit());
                }
                return result;
            }
        }

        public static class Difference extends Expression {
            Expression a;
            Expression b;
            // a - b

            @Override
            StringBuilder dotVisit() {
                StringBuilder result = new StringBuilder();
                result.append(labelNode(number, "-"));
                result.append(edgeString(number, a.number));
                result.append(edgeString(number, b.number));
                result.append(a.dotVisit());
                result.append(b.dotVisit());
                return result;
            }
        }
    }
}

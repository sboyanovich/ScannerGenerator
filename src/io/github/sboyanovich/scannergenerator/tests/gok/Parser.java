package io.github.sboyanovich.scannergenerator.tests.gok;

import io.github.sboyanovich.scannergenerator.scanner.token.Domain;
import io.github.sboyanovich.scannergenerator.scanner.token.Token;
import io.github.sboyanovich.scannergenerator.scanner.token.TokenWithAttribute;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static io.github.sboyanovich.scannergenerator.tests.gok.DomainsWithIntegerAttribute.NUM;
import static io.github.sboyanovich.scannergenerator.tests.gok.DomainsWithStringAttribute.ID;
import static io.github.sboyanovich.scannergenerator.tests.gok.SimpleDomains.*;

class Parser {
    // Needs additional checks for correctness

    private static final Set<Domain> STMTS_FIRST = Set.of(KW_RETURN, KW_IF, KW_FOR, ID);
    private static final Set<Domain> BOOL_FIRST = Set.of(NUM, LPAREN, ID, MINUS);
    private static final Set<Domain> VAR_MUT_OPERATORS = Set.of(ASSIGN, INC, DEC);

    private Token sym;
    private Iterator<Token> tokens;
    private int nodeNames;

    private Parser(Iterator<Token> tokens) {
        this.tokens = tokens;
        this.sym = tokens.next();
        this.nodeNames = 0;
    }

    static IRNode parse(Iterator<Token> tokens) {
        return new Parser(tokens).parse();
    }

    private IRNode parse() {
        IRNode p = p();
        expect(Domain.END_OF_PROGRAM);
        return p;
    }

    private IRNode p() {
        return func();
    }

    private IRNode.Func func() {
        Token funcNameToken = sym;
        expect(ID);
        String funcName = (String) ((TokenWithAttribute) funcNameToken).getAttribute();
        IRNode.Args args = args();
        IRNode.Body body = body();

        IRNode.Func result = new IRNode.Func();

        result.name = funcName;
        result.args = args;
        result.body = body;

        result.number = nodeNames++;

        return result;
    }

    private IRNode.Args args() {
        expect(LPAREN);
        List<String> names = new ArrayList<>();
        if (sym.getTag() == ID) {
            Token argNameToken = sym;
            nextToken();

            String argName = (String) ((TokenWithAttribute) argNameToken).getAttribute();
            names.add(argName);

            while (sym.getTag() == COMMA) {
                nextToken();
                argNameToken = sym;
                expect(ID);
                argName = (String) ((TokenWithAttribute) argNameToken).getAttribute();
                names.add(argName);
            }
        }
        expect(RPAREN);

        IRNode.Args result = new IRNode.Args();
        result.argNames = names;

        result.number = nodeNames++;

        return result;
    }

    private IRNode.Body body() {
        expect(LBRACKET);
        List<IRNode.Statement> statements = stmts();
        expect(RBRACKET);

        IRNode.Body result = new IRNode.Body();
        result.statements = statements;

        result.number = nodeNames++;

        return result;
    }

    private List<IRNode.Statement> stmts() {
        List<IRNode.Statement> statements = new ArrayList<>();

        while (STMTS_FIRST.contains(sym.getTag())) {
            Domain tag = sym.getTag();
            IRNode.Statement statement;
            if (tag == KW_IF) {
                statement = ifStmt();
                if (sym.getTag() == SEMICOLON) {
                    nextToken();
                }
            } else if (tag == KW_FOR) {
                statement = forStmt();
                if (sym.getTag() == SEMICOLON) {
                    nextToken();
                }
            } else if (tag == KW_RETURN) {
                statement = retStmt();
                expect(SEMICOLON);
            } else { // implicitly this is ID
                statement = varMut();
                expect(SEMICOLON);
            }
            statements.add(statement);
        }

        return statements;
    }

    private IRNode.Statement.IfStmt ifStmt() {
        expect(KW_IF);
        expect(LPAREN);
        IRNode.BoolExpr condition = bool();
        expect(RPAREN);
        IRNode.Body body = body();
        IRNode.Body elseBody = null;

        if (sym.getTag() == KW_ELSE) {
            nextToken();
            elseBody = body();
        }

        IRNode.Statement.IfStmt result = new IRNode.Statement.IfStmt();
        result.condition = condition;
        result.body = body;
        result.elseBody = elseBody;

        result.number = nodeNames++;

        return result;
    }

    private IRNode.Statement.ForStmt forStmt() {
        expect(KW_FOR);
        expect(LPAREN);

        IRNode.Statement.VarMut init = null;
        IRNode.BoolExpr condition = null;
        IRNode.Statement.VarMut after = null;

        if (sym.getTag() == ID) {
            init = varMut();
        }
        expect(SEMICOLON);
        if (BOOL_FIRST.contains(sym.getTag())) {
            condition = bool();
        }
        expect(SEMICOLON);
        if (sym.getTag() == ID) {
            after = varMut();
        }
        expect(RPAREN);

        IRNode.Body body = body();

        IRNode.Statement.ForStmt result = new IRNode.Statement.ForStmt();
        result.init = init;
        result.condition = condition;
        result.after = after;
        result.body = body;

        result.number = nodeNames++;

        return result;
    }

    private IRNode.Statement.VarMut varMut() {
        IRNode.Statement.VarMut result;

        String varName = var();

        anticipateEither(VAR_MUT_OPERATORS);
        if (sym.getTag() == ASSIGN) {
            nextToken();
            IRNode.Expression assignedExpr = expr();
            IRNode.Statement.VarMut.Assignment asgt = new IRNode.Statement.VarMut.Assignment();
            asgt.assignedExpr = assignedExpr;
            result = asgt;
        } else if (sym.getTag() == INC) {
            nextToken();
            result = new IRNode.Statement.VarMut.Inc();
        } else {    // implicitly DEC
            nextToken();
            result = new IRNode.Statement.VarMut.Dec();
        }

        result.varName = varName;

        result.number = nodeNames++;

        return result;
    }

    private IRNode.Statement.RetStmt retStmt() {
        expect(KW_RETURN);
        IRNode.Expression retExpr = expr();

        IRNode.Statement.RetStmt result = new IRNode.Statement.RetStmt();
        result.result = retExpr;

        result.number = nodeNames++;

        return result;
    }

    private IRNode.BoolExpr bool() {
        IRNode.BoolExpr result = new IRNode.BoolExpr();

        IRNode.Expression lhs = expr();

        if (sym.getTag() == CMP) {
            nextToken();
            result.operator = IRNode.BoolExpr.BoolOp.CMP;
        } else {
            expect(EQUALS);
            result.operator = IRNode.BoolExpr.BoolOp.EQUALS;
        }

        IRNode.Expression rhs = expr();

        result.lhs = lhs;
        result.rhs = rhs;

        result.number = nodeNames++;

        return result;
    }

    private String var() {
        Token varNameToken = sym;
        expect(ID);
        String result = (String) ((TokenWithAttribute) varNameToken).getAttribute();
        return result;
    }

    private IRNode.Expression expr() {
        IRNode.Expression result = t();

        IRNode.Expression t;
        while (sym.getTag() == PLUS || sym.getTag() == MINUS) {
            if (sym.getTag() == PLUS) {
                nextToken();

                IRNode.Expression.Sum sum = new IRNode.Expression.Sum();
                List<IRNode.Expression> operands = new ArrayList<>();

                t = t();

                operands.add(result);
                operands.add(t);

                sum.operands = operands;
                result = sum;

                result.number = nodeNames++;

            } else {
                nextToken();
                IRNode.Expression.Difference diff = new IRNode.Expression.Difference();

                t = t();

                diff.a = result;
                diff.b = t;

                result = diff;
                result.number = nodeNames++;
            }
        }

        return result;
    }

    private IRNode.Expression t() {
        IRNode.Expression result = f();

        IRNode.Expression f;
        while (sym.getTag() == MUL) {
            nextToken();

            IRNode.Expression.Product prod = new IRNode.Expression.Product();
            List<IRNode.Expression> operands = new ArrayList<>();

            f = f();

            operands.add(result);
            operands.add(f);

            prod.operands = operands;
            result = prod;

            result.number = nodeNames++;
        }

        return result;
    }

    private IRNode.Expression f() {
        IRNode.Expression result;

        if (sym.getTag() == ID) {
            IRNode.Expression.Var var = new IRNode.Expression.Var();
            var.name = var();
            //System.out.println("Read name: " + var.name);
            result = var;
        } else if (sym.getTag() == NUM) {
            IRNode.Expression.Num num = new IRNode.Expression.Num();
            num.value = (int) ((TokenWithAttribute) sym).getAttribute();
            result = num;
            nextToken();
        } else if (sym.getTag() == LPAREN) {
            nextToken();
            result = expr();
            expect(RPAREN);
        } else {
            expect(MINUS);
            IRNode.Expression.Neg neg = new IRNode.Expression.Neg();
            neg.operand = expr();
            result = neg;
        }

        result.number = nodeNames++;

        return result;
    }

    private void nextToken() {
        if (this.tokens.hasNext()) {
            this.sym = this.tokens.next();
        }
    }

    private void expect(Domain tag) {
        if (this.sym.getTag() == tag) {
            nextToken();
        } else {
            throw new IllegalStateException(
                    "Syntax error at token " + this.sym + ". Expected " + tag + ", got " + this.sym.getTag() + "!");
        }
    }

    private void anticipateEither(Set<Domain> tags) {
        if (!tags.contains(sym.getTag())) {
            throw new IllegalStateException(
                    "Syntax error at token " + this.sym + ". Expected one of" + tags + ", got " + this.sym.getTag() + "!");
        }
    }
}

package io.github.sboyanovich.scannergenerator.tests.biglexgen;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.scanner.Position;
import io.github.sboyanovich.scannergenerator.scanner.Text;
import io.github.sboyanovich.scannergenerator.scanner.token.Token;

import java.util.Optional;

import static io.github.sboyanovich.scannergenerator.tests.biglexgen.DomainsWithIntPairAttribute.REPETITION_OP;
import static io.github.sboyanovich.scannergenerator.tests.biglexgen.DomainsWithIntegerAttribute.CHAR;
import static io.github.sboyanovich.scannergenerator.tests.biglexgen.DomainsWithStringAttribute.*;
import static io.github.sboyanovich.scannergenerator.tests.biglexgen.GeneratedScanner.Mode.*;
import static io.github.sboyanovich.scannergenerator.tests.biglexgen.SimpleDomains.*;

public class MyScanner extends GeneratedScanner {
    private MockCompiler compiler;

    public MyScanner(String inputText) {
        super(inputText);
        this.compiler = new MockCompiler();
    }

    public MockCompiler getCompiler() {
        return compiler;
    }

    @Override
    protected void handleError(int codepoint, Mode mode, Position errorAt) {
        this.compiler.addError(errorAt, "Unexpected symbol encountered: " + codepoint);
    }

    @Override
    protected Optional<Token> handleLParen(Text text, Fragment fragment) {
        return Optional.of(LPAREN.createToken(text, fragment));
    }

    @Override
    protected Optional<Token> handleRParen(Text text, Fragment fragment) {
        return Optional.of(RPAREN.createToken(text, fragment));
    }

    @Override
    protected Optional<Token> handleOptionOp(Text text, Fragment fragment) {
        return Optional.of(OPTION_OP.createToken(text, fragment));
    }

    @Override
    protected Optional<Token> handleUnionOp(Text text, Fragment fragment) {
        return Optional.of(UNION_OP.createToken(text, fragment));
    }

    @Override
    protected Optional<Token> handlePosIterationOp(Text text, Fragment fragment) {
        return Optional.of(POS_ITERATION_OP.createToken(text, fragment));
    }

    @Override
    protected Optional<Token> handleIterationOp(Text text, Fragment fragment) {
        return Optional.of(ITERATION_OP.createToken(text, fragment));
    }

    @Override
    protected Optional<Token> handleDot(Text text, Fragment fragment) {
        return Optional.of(DOT.createToken(text, fragment));
    }

    @Override
    protected Optional<Token> handleCharClassRangeOp(Text text, Fragment fragment) {
        return Optional.of(CHAR_CLASS_RANGE_OP.createToken(text, fragment));
    }

    @Override
    protected Optional<Token> handleCharClassNeg(Text text, Fragment fragment) {
        return Optional.of(CHAR_CLASS_NEG.createToken(text, fragment));
    }

    @Override
    protected Optional<Token> handleCharClassOpen(Text text, Fragment fragment) {
        switchToMode(CHAR_CLASS);
        return Optional.of(CHAR_CLASS_OPEN.createToken(text, fragment));
    }

    @Override
    protected Optional<Token> handleCharClassClose(Text text, Fragment fragment) {
        switchToMode(REGEX);
        return Optional.of(CHAR_CLASS_CLOSE.createToken(text, fragment));
    }

    @Override
    protected Optional<Token> handleChar(Text text, Fragment fragment) {
        return Optional.of(CHAR.createToken(text, fragment));
    }

    @Override
    protected Optional<Token> handleClassChar(Text text, Fragment fragment) {
        return Optional.of(CHAR.createToken(text, fragment));
    }

    @Override
    protected Optional<Token> handleNamedExpr(Text text, Fragment fragment) {
        return Optional.of(NAMED_EXPR.createToken(text, fragment));
    }

    @Override
    protected Optional<Token> handleClassMinusOp(Text text, Fragment fragment) {
        return Optional.of(CLASS_MINUS_OP.createToken(text, fragment));
    }

    @Override
    protected Optional<Token> handleRepetitionOp(Text text, Fragment fragment) {
        return Optional.of(REPETITION_OP.createToken(text, fragment));
    }

    @Override
    protected Optional<Token> handleLAngleBracket(Text text, Fragment fragment) {
        return Optional.of(L_ANGLE_BRACKET.createToken(text, fragment));
    }

    @Override
    protected Optional<Token> handleRAngleBracket(Text text, Fragment fragment) {
        return Optional.of(R_ANGLE_BRACKET.createToken(text, fragment));
    }

    @Override
    protected Optional<Token> handleDefiner(Text text, Fragment fragment) {
        switchToMode(REGEX);
        return Optional.of(DEFINER.createToken(text, fragment));
    }

    @Override
    protected Optional<Token> handleRulesSectionMarker(Text text, Fragment fragment) {
        return Optional.of(RULES_SECTION_MARKER.createToken(text, fragment));
    }

    @Override
    protected Optional<Token> handleRuleEnd(Text text, Fragment fragment) {
        return Optional.of(RULE_END.createToken(text, fragment));
    }

    @Override
    protected Optional<Token> handleComma(Text text, Fragment fragment) {
        return Optional.of(COMMA.createToken(text, fragment));
    }

    @Override
    protected Optional<Token> handleModesSectionMarker(Text text, Fragment fragment) {
        return Optional.of(MODES_SECTION_MARKER.createToken(text, fragment));
    }

    @Override
    protected Optional<Token> handleIdentifier(Text text, Fragment fragment) {
        return Optional.of(IDENTIFIER.createToken(text, fragment));
    }

    @Override
    protected Optional<Token> handleDomainsGroupMarker(Text text, Fragment fragment) {
        return Optional.of(DOMAINS_GROUP_MARKER.createToken(text, fragment));
    }

    @Override
    protected Optional<Token> handleWhitespaceInRegex(Text text, Fragment fragment) {
        switchToMode(INITIAL);
        setStartToCurrentPosition();
        return Optional.empty();
    }

    @Override
    protected Optional<Token> handleCommentStart(Text text, Fragment fragment) {
        switchToMode(COMMENT);
        return Optional.empty();
    }

    @Override
    protected Optional<Token> handleNoAsteriskSeq(Text text, Fragment fragment) {
        return Optional.empty();
    }

    @Override
    protected Optional<Token> handleAsterisk(Text text, Fragment fragment) {
        return Optional.empty();
    }

    @Override
    protected Optional<Token> handleCommentClose(Text text, Fragment fragment) {
        switchToMode(INITIAL);
        setStartToCurrentPosition();
        return Optional.empty();
    }
}

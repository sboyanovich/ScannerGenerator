package io.github.sboyanovich.scannergenerator.generated;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.scanner.Position;
import io.github.sboyanovich.scannergenerator.scanner.Text;
import io.github.sboyanovich.scannergenerator.scanner.token.Token;

import java.util.Optional;

import static io.github.sboyanovich.scannergenerator.generated.GeneratedScanner.Mode.INITIAL;

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
    protected void handleError(int codePoint, Mode mode, Position errorAt) {
        this.compiler.addError(errorAt, "Unexpected symbol encountered: " + codePoint);
    }

    @Override
    protected Optional<Token> handleWhitespaceInRegex(Text text, Fragment fragment) {
        switchToMode(INITIAL);
        setStartToCurrentPosition();
        return Optional.empty();
    }

    @Override
    protected Optional<Token> handleNoAsteriskSeq(Text text, Fragment fragment) {
        return Optional.empty();
    }

    @Override
    protected Optional<Token> handleCommentAsterisk(Text text, Fragment fragment) {
        return Optional.empty();
    }

    @Override
    protected Optional<Token> handleCommentClose(Text text, Fragment fragment) {
        switchToMode(INITIAL);
        setStartToCurrentPosition();
        return Optional.empty();
    }

    @Override
    protected Optional<Token> handleSlcReg(Text text, Fragment fragment) {
        return Optional.empty();
    }

    @Override
    protected Optional<Token> handleSlcClose(Text text, Fragment fragment) {
        switchToMode(INITIAL);
        setStartToCurrentPosition();
        return Optional.empty();
    }
}

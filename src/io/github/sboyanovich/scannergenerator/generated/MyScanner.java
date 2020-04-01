package io.github.sboyanovich.scannergenerator.generated;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.scanner.Position;
import io.github.sboyanovich.scannergenerator.scanner.Text;
import io.github.sboyanovich.scannergenerator.scanner.token.Token;

import java.util.Optional;

import static io.github.sboyanovich.scannergenerator.generated.GeneratedScanner.Mode.INITIAL;

public class MyScanner extends GeneratedScanner {
    private MockCompiler compiler;

    public MyScanner(String inputText, int alphabetSize) {
        super(inputText);
        this.compiler = new MockCompiler(alphabetSize);
    }

    public MockCompiler getCompiler() {
        return compiler;
    }

    @Override
    protected Optional<Token> handleError(Text text, Mode mode, Position start, Position follow) {
        Optional<Token> result = super.handleError(text, mode, start, follow);
        int codePoint = text.codePointAt(follow.getIndex());
        this.compiler.addError(follow, "Unexpected symbol encountered: " + codePoint);
        setStartToCurrentPosition();
        return Optional.empty();
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

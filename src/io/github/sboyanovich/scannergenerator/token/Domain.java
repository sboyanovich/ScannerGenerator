package io.github.sboyanovich.scannergenerator.token;

import io.github.sboyanovich.scannergenerator.Fragment;
import io.github.sboyanovich.scannergenerator.lex.Text;

/**
 * Should be implemented with an enum!
 *
 * There generally should be error domain.
 *
 * There should be end of program domain.
 *
 * Both of these should be unique and should not coincide.
 *
 */
public interface Domain {
    Token createToken(Text text, Fragment fragment);
}

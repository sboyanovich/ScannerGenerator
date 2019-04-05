package lab.token;

import lab.Fragment;
import lab.lex.Text;

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

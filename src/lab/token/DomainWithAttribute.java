package lab.token;

import lab.Fragment;
import lab.lex.Text;

public interface DomainWithAttribute<T> extends Domain {
    T attribute(Text text, Fragment fragment);
}

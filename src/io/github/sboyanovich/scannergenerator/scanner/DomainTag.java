package io.github.sboyanovich.scannergenerator.scanner;

import io.github.sboyanovich.scannergenerator.automata.StateTag;
import io.github.sboyanovich.scannergenerator.scanner.token.Domain;

/**
 * This subtype of StateTag always produces some meaningful domain.
 * Useful for first-gen scanners (visitor pattern instead of switch case).
 * Those should exclusively use DomainTags (apart from NOT_FINAL and FINAL_DUMMY).
 * */
public interface DomainTag extends StateTag {
    Domain getDomain();
}

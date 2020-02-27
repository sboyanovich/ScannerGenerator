package io.github.sboyanovich.scannergenerator.scanner;

import io.github.sboyanovich.scannergenerator.scanner.token.Domain;

public interface StateTag {
    static boolean isFinal(StateTag tag) {
        return !tag.equals(StateTag.NOT_FINAL);
    }

    StateTag FINAL_DUMMY = new StateTag() {
        @Override
        public Domain getDomain() {
            throw new Error("Dummy tag doesn't correspond to any domain!");
        }

        @Override
        public String toString() {
            return "DUMMY";
        }
    };

    StateTag NOT_FINAL = new StateTag() {
        @Override
        public Domain getDomain() {
            throw new Error("This is never supposed to be called!");
        }

        @Override
        public String toString() {
            return "NOT_FINAL";
        }
    };

    Domain getDomain();

    default boolean hasDomain() {
        return true;
    }
}

package io.github.sboyanovich.scannergenerator.automata;

public interface StateTag {
    static boolean isFinal(StateTag tag) {
        return !tag.equals(StateTag.NOT_FINAL);
    }

    StateTag FINAL_DUMMY = new StateTag() {
        @Override
        public String toString() {
            return "DUMMY";
        }
    };

    StateTag NOT_FINAL = new StateTag() {
        @Override
        public String toString() {
            return "NOT_FINAL";
        }
    };
}

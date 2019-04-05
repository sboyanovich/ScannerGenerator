package io.github.sboyanovich.scannergenerator.utility;

/**
 *  Represents mapping of [0, domain - 1] to equivalence class [0, eqClassDomain - 1]
 *
  */
public class EquivalenceMap {
    private int domain;
    private int eqClassDomain;

    private int[] map;

    public EquivalenceMap(int domain, int eqClassDomain, int[] map) {
        this.domain = domain;
        this.eqClassDomain = eqClassDomain;
        this.map = map;
    }

    public int getEqClass(int elem) {
        return this.map[elem];
    }

    public int getDomain() {
        return domain;
    }

    public int getEqClassDomain() {
        return eqClassDomain;
    }
}

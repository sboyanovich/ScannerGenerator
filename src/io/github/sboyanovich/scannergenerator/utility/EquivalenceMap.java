package io.github.sboyanovich.scannergenerator.utility;

import java.util.Arrays;
import java.util.Objects;

import static io.github.sboyanovich.scannergenerator.utility.Utility.isInRange;

/**
 * Represents mapping of [0, domain - 1] to equivalence class [0, eqClassDomain - 1]
 */
public class EquivalenceMap {
    private int domain;
    private int eqClassDomain;

    private int[] map;

    public static EquivalenceMap identityMap(int domain) {
        int[] map = new int[domain];
        for (int i = 0; i < domain; i++) {
            map[i] = i;
        }
        return new EquivalenceMap(domain, domain, map);
    }

    public EquivalenceMap(int domain, int eqClassDomain, int[] map) {
        // validation
        Objects.requireNonNull(map);
        // domain > 0
        // 0 < eqClassDomain <= domain
        if (!(domain > 0)) {
            throw new IllegalArgumentException("Domain must be non-negative!");
        }
        if (!isInRange(eqClassDomain, 1, domain)) {
            throw new IllegalArgumentException("EqClassDomain must be in range [1, domain]!");
        }
        map = Arrays.copyOf(map, map.length);
        if (map.length != domain) {
            throw new IllegalArgumentException("Parameter map[] should have domain length!");
        }
        for (int i = 0; i < map.length; i++) {
            if (!isInRange(map[i], 0, eqClassDomain - 1)) {
                throw new IllegalArgumentException("Mappings should lie in range [0, eqClassDomain-1]!");
            }
        }

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

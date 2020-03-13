package io.github.sboyanovich.scannergenerator.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static io.github.sboyanovich.scannergenerator.utility.Utility.*;

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

    // useful for determinization
    public List<Integer> getRepresents() {
        List<Integer> result = new ArrayList<>();

        for (int i = 0; i < eqClassDomain; i++) {
            result.add(0);
        }

        for (int i = 0; i < domain; i++) {
            int c = map[i];
            result.set(c, i);
        }

        return result;
    }

    public List<List<Integer>> getClasses() {
        List<List<Integer>> result = new ArrayList<>();

        for (int i = 0; i < eqClassDomain; i++) {
            result.add(new ArrayList<>());
        }

        for (int i = 0; i < domain; i++) {
            int c = map[i];
            result.get(c).add(i);
        }

        return result;
    }

    public String displayClasses(Function<Integer, String> alphabetInterpretation) {
        List<List<Integer>> classes = getClasses();
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < eqClassDomain; i++) {
            result.append(i).append(SPACE).append(EQDEF).append(SPACE)
                    .append(displayAsSegments(classes.get(i), alphabetInterpretation));
            result.append(NEWLINE);
        }

        return result.toString();
    }

    public EquivalenceMap compose(EquivalenceMap map2) {
        // not checking parameters for validity for now
        int m = getDomain();
        int[] resultMap = new int[m];
        for (int i = 0; i < resultMap.length; i++) {
            resultMap[i] = map2.getEqClass(getEqClass(i));
        }
        return new EquivalenceMap(m, map2.getEqClassDomain(), resultMap);
    }
}

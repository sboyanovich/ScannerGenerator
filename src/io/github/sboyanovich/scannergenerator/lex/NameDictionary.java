package io.github.sboyanovich.scannergenerator.lex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NameDictionary {
    private Map<String, Integer> nameCodes;
    private List<String> names;

    public NameDictionary() {
        this.nameCodes = new HashMap<>();
        this.names = new ArrayList<>();
    }

    public boolean contains(String s) {
        return this.nameCodes.containsKey(s);
    }

    public int addName(String name) {
        int index = getIndex(name);
        if (index == -1) {
            int pos = this.names.size();
            this.names.add(name);
            this.nameCodes.put(name, pos);
            return pos;
        }
        return index;
    }

    public String getName(int code) {
        return this.names.get(code);
    }

    private int getIndex(String name) {
        Integer val = this.nameCodes.get(name);
        if (val != null) {
            return val;
        }
        return -1;
    }
}

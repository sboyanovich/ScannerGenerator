package io.github.sboyanovich.scannergenerator.automata;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

// TODO: UNDER CONSTRUCTION
public class SymbolSet implements Set<Integer> {
    private int alphabetSize;
    private boolean storesComplement;
    private HashSet<Integer> storage;

    public SymbolSet(int alphabetSize, boolean storesComplement) {
        this.alphabetSize = alphabetSize;
        this.storesComplement = storesComplement;
        this.storage = new HashSet<>();
    }

    public SymbolSet(int alphabetSize) {
        this(alphabetSize, false);
    }

    public int getAlphabetSize() {
        return alphabetSize;
    }

    public boolean isStoresComplement() {
        return storesComplement;
    }

    @Override
    public int size() {
        if (storesComplement) {
            return this.alphabetSize - this.storage.size();
        } else {
            return this.storage.size();
        }
    }

    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof Integer) {
            if (this.storesComplement) {
                return !this.storage.contains(o);
            } else {
                return this.storage.contains(o);
            }
        }
        return false;
    }

    @Override
    public Iterator<Integer> iterator() {
        if (!this.storesComplement) {
            return this.storage.iterator();
        } else {
            return new Iterator<>() {

                private int i = 0;
                private int n = alphabetSize;

                @Override
                public boolean hasNext() {
                    while (i < n && storage.contains(i)) {
                        i++;
                    }
                    return i < n;
                }

                @Override
                public Integer next() {
                    return i;
                }
            };
        }
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return null;
    }

    @Override
    public boolean add(Integer integer) {
        if (!this.contains(integer)) {
            int size = this.size();
            int half = this.alphabetSize / 2;

            if (!this.storesComplement) {
                if ((size + 1) < half) {
                    this.storage.add(integer);
                } else {
                    HashSet<Integer> newStorage = new HashSet<>();
                    for (int i = 0; i < alphabetSize; i++) {
                        if (!this.storage.contains(i) && i != integer) {
                            newStorage.add(i);
                        }
                    }
                    this.storesComplement = true;
                    this.storage = newStorage;
                }
            } else {
                if ((size + 1) < half) {
                    HashSet<Integer> newStorage = new HashSet<>();
                    for (int i = 0; i < alphabetSize; i++) {
                        if (!this.storage.contains(i)) {
                            newStorage.add(i);
                        }
                    }
                    this.storesComplement = false;
                    this.storage = newStorage;
                } else {
                    this.storage.remove(integer);
                }
            }
            return true;
        }
        return false;
    }

    //
    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends Integer> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {
        this.storesComplement = false;
        this.storage.clear();
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int hashCode() {
        return this.storage.hashCode();
    }
}

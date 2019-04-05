package io.github.sboyanovich.scannergenerator;

public class Fragment {
    private Position starting;
    private Position following;

    public Fragment(Position starting, Position following) {
        this.starting = starting;
        this.following = following;
    }

    public Position getStarting() {
        return starting;
    }

    public Position getFollowing() {
        return following;
    }

    @Override
    public String toString() {
        return this.starting + "-" + this.following;
    }
}

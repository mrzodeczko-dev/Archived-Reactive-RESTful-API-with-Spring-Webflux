package com.rzodeczko.domain.position_index;

import com.rzodeczko.domain.vo.Position;

import java.util.Objects;

public class PositionIndex {

    private Position position;
    private boolean isFree;

    public PositionIndex() {
    }

    public PositionIndex(Position position, boolean isFree) {
        this.position = position;
        this.isFree = isFree;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }
    public boolean isFree() { return isFree; }
    public void setFree(boolean free) { isFree = free; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PositionIndex)) return false;
        PositionIndex p = (PositionIndex) o;
        return isFree == p.isFree && Objects.equals(position, p.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, isFree);
    }

    public static class Builder {
        private Position position;
        private boolean isFree;

        public Builder position(Position position) { this.position = position; return this; }
        public Builder isFree(boolean isFree) { this.isFree = isFree; return this; }
        public PositionIndex build() { return new PositionIndex(position, isFree); }
    }
}

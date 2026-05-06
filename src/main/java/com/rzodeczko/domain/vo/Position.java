package com.rzodeczko.domain.vo;

import java.io.Serializable;
import java.util.Objects;

public class Position implements Serializable {

    private Integer rowNo;
    private Integer colNo;

    public Position() {
    }

    public Position(Integer rowNo, Integer colNo) {
        this.rowNo = rowNo;
        this.colNo = colNo;
    }

    public Position(String value) {
        String[] array = value.split(", ");
        if (array.length == 2) {
            rowNo = Integer.parseInt(array[0].split(": ")[1]);
            colNo = Integer.parseInt(array[1].split(": ")[1]);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public Integer getRowNo() { return rowNo; }
    public void setRowNo(Integer rowNo) { this.rowNo = rowNo; }
    public Integer getColNo() { return colNo; }
    public void setColNo(Integer colNo) { this.colNo = colNo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;
        Position p = (Position) o;
        return Objects.equals(rowNo, p.rowNo) && Objects.equals(colNo, p.colNo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rowNo, colNo);
    }

    @Override
    public String toString() {
        return "rowNo: " + rowNo + ", colNo: " + colNo;
    }

    public static class Builder {
        private Integer rowNo;
        private Integer colNo;

        public Builder rowNo(Integer rowNo) { this.rowNo = rowNo; return this; }
        public Builder colNo(Integer colNo) { this.colNo = colNo; return this; }
        public Position build() { return new Position(rowNo, colNo); }
    }
}

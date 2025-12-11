package com.school045.model;

public record Department(int id, String name, int headcount) {
    @Override
    public String toString() {
        return name + " (" + headcount + ")";
    }
}

package com.school045.model;

public record Position(int id, String name) {
    @Override
    public String toString() {
        return name;
    }
}

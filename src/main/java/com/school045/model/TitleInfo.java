package com.school045.model;

public record TitleInfo(int id, String name) {
    @Override
    public String toString() {
        return name;
    }
}

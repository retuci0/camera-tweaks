package me.retucio.sputnik.module;

public enum Category {

    PLAYER("jugador"),
    WORLD("mundo"),

    CAMERA("cámara"),
    RENDER("renderizado"),

    NETWORK("red"),

    CLIENT("cliente"),
    MISC("misc."),

    ALL("todos");

    private final String name;
    Category(String name) { this.name = name; }
    @Override public String toString() { return name; }
}

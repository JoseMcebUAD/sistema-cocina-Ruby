package com;

/**
 * Clase Launcher para iniciar la aplicación.
 * Esta clase es necesaria porque JavaFX requiere una clase de entrada
 * que NO extienda Application cuando se empaqueta como JAR ejecutable.
 */
public class Launcher {
    public static void main(String[] args) {
        Main.main(args);
    }
}

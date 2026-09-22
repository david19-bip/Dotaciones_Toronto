package com.dotacionestoronto.dotacionestoronto.util;

public class RolUtil {

    private RolUtil() {
    }

    public static String obtenerNombreRol(Integer idRol) {

        if (idRol == null) {
            return "";
        }

        return switch (idRol) {
            case 1 -> "ROLE_ADMIN";
            case 2 -> "ROLE_CLIENTE";
            case 3 -> "ROLE_VENDEDOR";
            case 4 -> "ROLE_BODEGUERO";
            default -> "";
        };
    }

    public static String obtenerNombreMostrar(Integer idRol) {

        if (idRol == null) {
            return "";
        }

        return switch (idRol) {
            case 1 -> "Administrador";
            case 2 -> "Cliente";
            case 3 -> "Vendedor";
            case 4 -> "Bodeguero";
            default -> "";
        };
    }
}
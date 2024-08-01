package com.example.alecontreras.mascotas;

public enum TipoMascota {
    PERRO, GATO, PAJARO, OTRO;

    public String toMasString() {
        switch (this) {
            case PERRO:
                return "Perro";
            case GATO:
                return "Gato";
            case PAJARO:
                return "Pájaro";
            case OTRO:
                return "Otro";
        }
        return "";
    }



    public static TipoMascota fromCode(int code) {
        switch (code) {
            case 0:
                return PERRO;
            case 1:
                return GATO;
            case 2:
                return PAJARO;
            default:
                return OTRO;
        }
    }

    public int toCode() {
        switch (this) {
            case PERRO:
                return 0;
            case  GATO:
                return 1;
            case PAJARO:
                return 2;
            default:
                return -1;
        }
    }

    public static TipoMascota fromString(String s) {
        switch (s) {
            case "PERRO":
                return PERRO;
            case "GATO":
                return GATO;
            case "PAJARO":
                return PAJARO;
            default:
                return OTRO;
        }
    }
}

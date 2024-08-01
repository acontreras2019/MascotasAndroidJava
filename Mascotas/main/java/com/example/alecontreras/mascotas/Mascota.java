package com.example.alecontreras.mascotas;


import java.text.SimpleDateFormat;
import java.util.Date;

public class Mascota {
    private int id;   // clave

    private int idAvellano;
    private String nMascota;
    private String nPropietario;

    private long fechaAdopcion;
    private TipoMascota tipo;

    private Boolean vacunado;
    private int estado;

    public Mascota(){
        this.id = 0;
        this.idAvellano = 0;
        this.nMascota="";
        this.nPropietario = "";
        this.fechaAdopcion = System.currentTimeMillis();
        this.tipo =TipoMascota.fromCode(-1);
        this.vacunado = false;
        this.estado = -1;
    }
    public Mascota(int id, int idAvellano, String nMascota, String nPropietario, long fechaAdopcion, TipoMascota tipo, Boolean vacunado, int estado){
        this.id = id;
        this.idAvellano = idAvellano;
        this.nMascota = nMascota;
        this.nPropietario = nPropietario;
        this.fechaAdopcion  = fechaAdopcion;
        this.tipo = tipo;
        this.vacunado = vacunado;
        this.estado = estado;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdAvellano() {
        return idAvellano;
    }

    public void setIdAvellano(int id) {
        this.idAvellano = id;
    }

    public String getnMascota() {
        return nMascota;
    }
    public void setnMascota(String nMascota) {
        this.nMascota = nMascota;
    }

    public String getnPropietario() {
        return nPropietario;
    }
    public void setnPropietario(String nPropietario) {
        this.nPropietario = nPropietario;
    }

    public String getfechaAdopcion() {
        Date f = tryParseDate(fechaAdopcion,new Date());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String strDate = sdf.format(f);

        return strDate;
    }
    public void setFechaAdopcion(long fechaAdopcion) {
        this.fechaAdopcion = fechaAdopcion;
    }

    public TipoMascota getTipo() {
        return tipo;
    }
    public void setTipo(TipoMascota tipo) {
        this.tipo = tipo;
    }
    public boolean getVacunado() {
        return vacunado;
    }
    public void setVacunado(boolean fecha) {
        this.vacunado = vacunado;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }


    private int tryParseInt(String value, int defaultVal) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private long tryParseLong(String value, long defaultVal) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private Date tryParseDate(long value, Date defaultVal) {
        try {

            Date date = new Date();
            date.setTime(value);

            return date;
        } catch (NullPointerException e) {
            return defaultVal;
        }
    }

    public String toTEXTformat() {
        StringBuilder sb = new StringBuilder();
        sb.append(id); sb.append("\n");
        sb.append(idAvellano); sb.append("\n");
        sb.append(nMascota);  sb.append("\n");
        sb.append(nPropietario); sb.append("\n");
        sb.append(fechaAdopcion); sb.append("\n");
        sb.append(tipo); sb.append("\n");
        sb.append(vacunado); sb.append("\n");
        sb.append(estado); sb.append("\n");
        return sb.toString();
    }

    public void setFromTEXTLineformat(String sdata) {
        String[] sUnaMascota;
        sUnaMascota = sdata.trim().split("\n");

        this.setId(tryParseInt(sUnaMascota[0].trim(),0));
        this.setIdAvellano(tryParseInt(sUnaMascota[1].trim(),0));
        this.setnMascota(sUnaMascota[2].trim());
        this.setnPropietario(sUnaMascota[3].trim());
        this.setFechaAdopcion(tryParseLong(sUnaMascota[4].trim(),0));
        TipoMascota t = TipoMascota.fromString(sUnaMascota[5].trim());
        this.setTipo(t);
        this.setVacunado(Boolean.parseBoolean( sUnaMascota[6].trim()));
        this.setEstado(tryParseInt(sUnaMascota[7].trim(), 0));

    }


}

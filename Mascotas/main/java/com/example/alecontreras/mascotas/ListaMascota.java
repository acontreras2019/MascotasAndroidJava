package com.example.alecontreras.mascotas;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
public class ListaMascota {
    ArrayList<Mascota> lista;
    Context context;
    public ListaMascota(Context ctx){
        this.lista = new ArrayList<Mascota>();
        this.context = ctx;
    }
    public ListaMascota(ArrayList<Mascota> l){
        this.lista = new ArrayList<Mascota>(l);
    }


    public String[] listToString() {
        int len = lista.size();
        String[] stringList = new String[len];
        for (int i = 0; i < len; i++) {
            stringList[i]  = lista.get(i).toTEXTformat();
        }

        return stringList;
    }

    public ArrayList<Mascota> setListFromTEXTLineformat(String[] sdata) {
        ArrayList<Mascota> l = new ArrayList<Mascota>();

        for (String s: sdata
        ) {
            Mascota p = new Mascota();
            p.setFromTEXTLineformat(s);
            l.add(p);
        }
        return l;


    }

    public String[] listToTEXTformat() {
        int len = lista.size();
        String[] stringList = new String[len];
        for (int i = 0; i < len; i++) {
            stringList[i]  = lista.get(i).toTEXTformat();
        }

        return stringList;
    }

    //Metodos TryParse
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

    private long tryParseDate(String d){
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date date = null;
        try {
            date = sdf.parse(d);

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        long millis = date.getTime();
        return millis;
    }

    public int tryBoolToInt(boolean b) {
        int n = 0;
        if (b) {
            n = 1;
        }
        return n;
    }

    public int getCount(){
        return lista.size();
    }
}


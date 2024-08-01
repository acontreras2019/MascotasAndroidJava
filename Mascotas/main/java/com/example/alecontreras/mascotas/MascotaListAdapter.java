package com.example.alecontreras.mascotas;

import android.widget.BaseAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class MascotaListAdapter extends BaseAdapter {
    private final Activity actividad;
    private ArrayList<Mascota>  lista;
    public MascotaListAdapter(Activity actividad, ArrayList<Mascota> lista) {
        super();
        this.actividad = actividad;
        this.lista = lista;

    }

    public int getCount() {
        return lista.size();

    }

    public Object getItem(int position) {
        return lista.get(position);

    }


    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = actividad.getLayoutInflater();
        @SuppressLint("ViewHolder") View view = inflater.inflate(R.layout.elem_mascota, null, true);

        String lbId = String.valueOf(lista.get(position).getId());
        String lbIdAvellano = String.valueOf(lista.get(position).getIdAvellano());
        String lbnMascota = lista.get(position).getnMascota() +" Avell:"+ lbIdAvellano + "IdInterno:" + lbId;
        String lbnPropietario = actividad.getResources().getString(R.string.lblpropietario) + lista.get(position).getnPropietario();
        String lbFechaAdopcion = actividad.getResources().getString(R.string.lblfechaAdopcion) + lista.get(position).getfechaAdopcion();
        String lbTipo = actividad.getResources().getString(R.string.lbltipo) + lista.get(position).getTipo();
        String lbVacunado = actividad.getResources().getString(R.string.lblvacunado) + lista.get(position).getVacunado();
        TipoMascota tMas = lista.get(position).getTipo();

        int tipoMascota = tMas.toCode();

        TextView tvN = (TextView) view.findViewById(R.id.txtnMascota);
        tvN.setText(lbnMascota);

        TextView tvPropietario = (TextView) view.findViewById(R.id.txtnPropietario);
        tvPropietario.setText(lbnPropietario);

        TextView tvFecha = (TextView) view.findViewById(R.id.txtFechaAdopcion);
        tvFecha.setText(lbFechaAdopcion);

        TextView tvTipo = (TextView) view.findViewById(R.id.txtTipo);
        tvTipo.setText(lbTipo);

        TextView tvVacunado = (TextView) view.findViewById(R.id.txtVacunado);
        tvVacunado.setText(lbVacunado);

        ImageView imageView = (ImageView) view.findViewById(R.id.foto_s);


       // switch (position){
        switch (tipoMascota){
            case 0:
                imageView.setImageResource(R.drawable.perro);
                break;
            case 1:
                imageView.setImageResource(R.drawable.gato);
                break;
            case 2:
                imageView.setImageResource(R.drawable.pajaro);
                break;
            default:
                imageView.setImageResource(R.drawable.otros);
                break;
        }

        return view;

    }

    }

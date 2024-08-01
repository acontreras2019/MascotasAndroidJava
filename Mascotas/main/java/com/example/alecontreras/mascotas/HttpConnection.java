package com.example.alecontreras.mascotas;



import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.JsonReader;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class HttpConnection extends Activity {

    public String urlString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
   ;
        SharedPreferences preferencias= this.getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        urlString = preferencias.getString("url","");

        new HttpPostTask().execute();

    }
    private class HttpPostTask extends AsyncTask<Void, Void, String> {
        ArrayList<Mascota> miList = new ArrayList<Mascota>();

        @Override
        protected String doInBackground(Void... params) {

            String data = "";
            URL url = null;

            try{
                url = new URL(urlString);
                HttpURLConnection conn = null;
                conn = (HttpURLConnection) url.openConnection();
                //Log.v("HTTPActivity", "En run(" + url + "): " );

                InputStream in = null;
                in = new BufferedInputStream(conn.getInputStream());

                data = readStreamJSON(in);

            }catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return data;

        }

        @Override
        protected void onPostExecute(String result) {

            String [] l = new ListaMascota(miList).listToString();

                Intent intent = new Intent(HttpConnection.this, MainActivity.class);
                intent.putExtra("list", l);
                setResult(RESULT_OK, intent);
              //  Log.v("HTTPActivityForCargainicial", "En run(" + l[0] + "): ");
                finish();
        }


        private String readStreamJSON(InputStream in) {
            JsonReader reader = new JsonReader(new InputStreamReader(in,  StandardCharsets.UTF_8));
            try {
                miList = readMascotasArray(reader);
                return miList.toString();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        }

        private ArrayList<Mascota> readMascotasArray(JsonReader reader) throws IOException {

            ArrayList<Mascota> mascotasJson = new ArrayList<Mascota>();
          //  Log.v("readMascotasArray", "Leyendo Json " );

            reader.beginArray();
            int id = -1;
            while (reader.hasNext()) {
                id++;
                mascotasJson.add(readMascota(reader, id));
            }

            reader.endArray();
            return mascotasJson;
        }
        public Mascota readMascota(JsonReader reader, int id) throws IOException {
            int idAvellano = 0;
            String nMascota = "";
            String nPropietario = "";
            long fechaAdopcion = 0;
            TipoMascota tipo = TipoMascota.fromCode(-1);;
            boolean vacunado = false;
            int estado = -1;


            reader.beginObject();
           // Log.v("ReadJson", "En la atributos de Mascota): " );
            while (reader.hasNext()) {
                String name = reader.nextName();
                switch (name) {
                    case "ID":
                        idAvellano = reader.nextInt();
                        break;
                    case "nMASCOTA":
                        nMascota = reader.nextString();
                        break;
                    case "nPROPIETARIO":
                        nPropietario = reader.nextString();
                        break;
                    case "ADOPCION":
                        fechaAdopcion = TryParseDate(reader.nextString());
                        break;
                    case "TIPO":
                        String xtipo = reader.nextString();
                        if (xtipo.equals("Perro")) tipo = TipoMascota.fromCode(0);
                        if (xtipo.equals("Gato")) tipo = TipoMascota.fromCode(1);
                        if (xtipo.equals("Pájaro")) tipo = TipoMascota.fromCode(2);
                        if (xtipo.equals("Otro")) tipo = TipoMascota.fromCode(-1);
                        break;
                    case "VACUNADO":
                        vacunado = Boolean.parseBoolean( reader.nextString());
                        break;

                    default:
                        reader.skipValue();
                        break;
                }
            }

            reader.endObject();
            Mascota m = new Mascota(0,idAvellano,nMascota, nPropietario, fechaAdopcion, tipo, vacunado, estado);
            return m;
        }

        private long TryParseDate(String d){
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
    }

}

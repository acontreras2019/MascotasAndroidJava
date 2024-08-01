package com.example.alecontreras.mascotas;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;

import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;

import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.JsonReader;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ServiceMascota extends Service {
    int num = 0;
    int delay = 0; // delay for 0 sec.
    int period = 15000; // repeat every 15 sec.
    Timer timer = new Timer();
    TimerTask tt;

    // Para las notificaciones
    private String mChannelID;

    private final CharSequence tickerText = "¡Mensaje de Notificación";
    private CharSequence contentText = "Has sido notificado";

    // Elementos de Accion para la Notificación
    private Intent mNotificationIntent, mDeleteIntent,acceptIntent  ;
    private NotificationManagerCompat mNotificationManager;
    private PendingIntent mContentIntent, mContentDeleteIntent, acceptPendingIntent;

    private static final int REQUEST_CODE = 123;
    String urlString = "";
    ArrayList<Mascota> miList = new ArrayList<Mascota>(); //Lista sin procesar
    private boolean isPaused = false;

    private String idsList = "";
    private int lastId, eventsCount;
    private NotificationCompat.Builder notificationBuilder;

    public static final String ACTION_SEND_NOTIFICATION = "com.example.alecontreras.mascotas.SEND_NOTIFICATION";
    public static final String ACTION_RECEIVE_RESULT = "com.example.alecontreras.mascotas.RECEIVE_RESULT";
    public static final String ACTION_RECEIVE_CANCELED = "com.example.alecontreras.mascotas.RECEIVE_CANCELED";
    public static final String ACTION_NOTIFICATION_CANCELLED = "com.example.alecontreras.mascotas.NOTIFICATION_CANCELLED";

    // Sónido y vibración al llegar la vibración
    RemoteViews mContentView = new RemoteViews(
            "com.example.alecontreras.mascotas",
            R.layout.notificacion_mascotas);

    public ServiceMascota() {


    }

    private BroadcastReceiver resultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent != null) {
                if (ACTION_RECEIVE_RESULT.equals(intent.getAction())) {
                    // Recibir los datos del BroadcastReceiver con Accion Procesada
                    Bundle extras = intent.getExtras();
                    if (extras != null) {
                        String[] listaPendientes = extras.getStringArray("limpiarLista");

                        if (listaPendientes != null) {

                            for (String m : listaPendientes
                            ) {
                                Mascota p = new Mascota();
                                p.setFromTEXTLineformat(m);
                                int idAvellanoBorrar = p.getIdAvellano();
                                miList.removeIf(mascota -> idAvellanoBorrar == mascota.getIdAvellano());
                                idsList = "";
                            }

                        }

                    }
                    else{
                        Log.d("ServiceMascota", "El Intent Send Noti no contiene extras");
                    }
                }else {
                    Log.d("ServiceMascota", "El Intent.Action es null en ACTION_RECEIVE_RESULT");
                }


                if (ACTION_RECEIVE_CANCELED.equals(intent.getAction())) {
                    // Recibir los datos del BroadcastReceiver con Accion Cancelada
                    Bundle extras = intent.getExtras();
                    if (extras != null) {
                        String[] listaPendientes = extras.getStringArray("limpiarLista");
                        if (listaPendientes != null) {
                            for (String m : listaPendientes
                            ) {
                                Mascota p = new Mascota();
                                p.setFromTEXTLineformat(m);
                                int idAvellanoBorrar = p.getIdAvellano();
                                miList.removeIf(mascota -> idAvellanoBorrar == mascota.getIdAvellano());
                                idsList = "";

                            }

                        }
                    }
                    else{
                        Log.d("ServiceMascota", "El Intent cancel Noti no contiene extras");
                    }
                } else {
                    Log.d("ServiceMascota", "El Intent.Action es null en ACTION_RECEIVE_CANCELED");
                }

            } else {
                Log.d("ServiceMascota", "El Intent es null  Noti");
            }
        }
    };

    public void onCreate() {
        createNotificationChannel();
        registrarFilterReceiver();

    }

    public void registrarFilterReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_RECEIVE_CANCELED);
        intentFilter.addAction(ACTION_RECEIVE_RESULT);
        LocalBroadcastManager.getInstance(this).registerReceiver(resultReceiver, intentFilter);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int idArranque) {

        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            urlString = bundle.getString("url");
            boolean pause = intent.getBooleanExtra("pause", false);

            if (pause) {
                pauseService();
            } else {
                resumeService();
            }

        }

        return START_STICKY;
    }

    private void pauseService() {
        isPaused = true;
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (tt != null) {
            tt.cancel();
            tt = null;
        }

        Log.d("Pausado", "Servicio pausado");
    }

    private void resumeService() {
        Log.d("Pausado", "Servicio start");
        isPaused = false;
        timer = new Timer();
        tt = new TimerTask() {
            @Override
            public void run() {
                ObtenerEventoFromURL();
                num++;
            }

            ;
        };
        timer.schedule(tt, delay, period);
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(resultReceiver);
        Toast.makeText(this, R.string.servicio_de_mascotas_detenido,
                Toast.LENGTH_SHORT).show();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (tt != null) {
            tt.cancel();
            tt = null;
        }

    }


    @Override
    public IBinder onBind(Intent intent) {
        // super.onBind(intent)
        // TODO: Return the communication channel to the service.

        return null;
        //throw new UnsupportedOperationException("Not yet implemented");
    }


    public void CrearIntents(int cant, String listId) {

        String[] listaPendiente = new ListaMascota(miList).listToTEXTformat();
        // Intent MainActivity
        mNotificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        mNotificationIntent.setAction(ACTION_SEND_NOTIFICATION);
        mNotificationIntent.putExtra("listaPendiente", listaPendiente);
        mNotificationIntent.putExtra("eventsCount", eventsCount);
        mNotificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (mNotificationIntent != null) {
            mNotificationIntent.putExtra("datos", "Eventos sin procesar: " + cant + "ids:" + listId);
        }
        mContentIntent = PendingIntent.getActivity(getApplicationContext(), 1111,
                mNotificationIntent, PendingIntent.FLAG_UPDATE_CURRENT |PendingIntent.FLAG_MUTABLE);

        // Intent para el BroadcastReceiver cuando el usuario cancela la notificación
        mDeleteIntent = new Intent(getApplicationContext(), Broadcast_Message.class);
        mDeleteIntent.setAction(ACTION_NOTIFICATION_CANCELLED);
        mDeleteIntent.putExtra("listaPendiente", listaPendiente);
        mDeleteIntent.putExtra("eventsCount", eventsCount);

        if (mDeleteIntent != null) {
            mDeleteIntent.putExtra("datos", "Eventos sin procesar: " + cant + " ids: " + listId);
        }
        mContentDeleteIntent = PendingIntent.getBroadcast(getApplicationContext(), 1113, mDeleteIntent, PendingIntent.FLAG_UPDATE_CURRENT |PendingIntent.FLAG_MUTABLE);

        Notificando(cant, listId);
    }

    private void createNotificationChannel() {
        mNotificationManager = NotificationManagerCompat.from(this);

        mChannelID = getPackageName() + ".channel_01";
        // The user-visible name of the channel.
        CharSequence name = getString(R.string.channel_name);

        // The user-visible description of the channel.
        String description = getString(R.string.channel_description);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(mChannelID, name, importance);

            // Configure the notification channel.
            mChannel.setDescription(description);
            mChannel.enableLights(true);

            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);

            mNotificationManager.createNotificationChannel(mChannel);
        }


    }

    public void Notificando(int cant, String listId) {
        int idNotificacion = 0;

        // Definimos el mensaje de  Notification  la expandir y el Intent:
        contentText = getString(R.string.eventos_pendientes);

        mContentView.setTextViewText(R.id.text, contentText + " ("
                + cant + ")");
        mContentView.setTextViewText(R.id.text2, " Ids: "
                + listId);

        // Construimos Notification

        notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), mChannelID)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(getString(R.string.eventos_pendientes) + cant + " ids: " + listId)
                .setContentIntent(mContentIntent)
                .setDeleteIntent(mContentDeleteIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH); // PRIORITY_HIGH para heads-up;

        Notification notification;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder
                    .setTicker(tickerText)
                    .setContentIntent(mContentIntent)
                    .setCustomContentView(mContentView);

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                Log.d("Notificando", "sin permisos");
                return;
            }
            mNotificationManager.notify(0, notificationBuilder.build());

        }else{
            notification = notificationBuilder.build();
            notification.flags = Notification.FLAG_AUTO_CANCEL;
            NotificationManager mNotificationManagerOld = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (mNotificationManagerOld != null) {
                mNotificationManagerOld.notify(idNotificacion, notification);
            }
        }
    }

    public void ObtenerEventoFromURL() {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    String data = "";
                    try {
                        // Creando la URL
                        URL url = new URL(urlString);
                        HttpURLConnection conn = null;

                        // Abriendo la conexión HTTP
                        conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");

                        InputStream in = null;
                        in = new BufferedInputStream(conn.getInputStream());

                        readStreamJSON(in);

                    }catch (MalformedURLException e) {
                        e.printStackTrace();
                        return;

                    }catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }).start();
        }

    private void readStreamJSON(InputStream in) {
        JsonReader reader = new JsonReader(new InputStreamReader(in,  StandardCharsets.UTF_8));
        try {
            ArrayList<Mascota> nuevosEventos =  readMascotasArray(reader);
            int nE = nuevosEventos.size();
            int nl = miList.size();

            for (Mascota n: nuevosEventos
                 ) {
                 int  idAvellanoNuevo =  n.getIdAvellano();
                 boolean existe = miList.stream().anyMatch(m -> m.getIdAvellano() == idAvellanoNuevo);

                if(!existe && (idAvellanoNuevo!= lastId)){
                    miList.add(n);
                    nl++;
                    idsList = idsList + "," + String.valueOf(n.getIdAvellano());
                    lastId = idAvellanoNuevo;
                    eventsCount++;
                    CrearIntents(nl, idsList);

                }

                }

            } catch (IOException ex) {
            throw new RuntimeException(ex);
        }//  return miList.toString();
        finally {
            try {
                reader.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private ArrayList<Mascota> readMascotasArray(JsonReader reader) throws IOException {

        ArrayList<Mascota> mascotasJson = new ArrayList<Mascota>();

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
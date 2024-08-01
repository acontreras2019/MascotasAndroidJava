package com.example.alecontreras.mascotas;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    int n =0;
    String urlInicial = "http://avellano.usal.es/~alsl/DAM/inicialJSON.txt";
    String urlCentinela = "http://avellano.usal.es/~alsl/DAM/centinelaJSON.txt";

    String urlHistorico = "http://avellano.usal.es/~alsl/DAM/historico.txt";
    private Switch serviceActivador;

    private ListView lv1;

    private TextView vDatos, tvNdatos;

    public static ArrayList<Mascota> misMascotas = new ArrayList<Mascota>();

    ListaMascota lista;

    private MascotaListAdapter adaptadorMascota;
    int totalEventos;
    long totalProcesados;
    public static final String ACTION_RECEIVE_RESULT = "com.example.alecontreras.mascotas.RECEIVE_RESULT";
    public static final String ACTION_RECEIVE_CANCELED = "com.example.alecontreras.mascotas.RECEIVE_CANCELED";
    public static final String ACTION_SEND_NOTIFICATION = "com.example.alecontreras.mascotas.SEND_NOTIFICATION";
    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 1;

    static int nElementos = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        overridePendingTransition(0, 0);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        n++;
        super.onStart();

        // Verificar y solicitar permisos si es necesario
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_POST_NOTIFICATIONS);
            }
        }
        IniciarVistas();
        CargarListening();
        CargarLista();

        registrarFilterReceiver();
        handleIntent(getIntent());

    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Toast.makeText(this, "onConfigurationChanged", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNewIntent(Intent intent) {
        // Recibir notificaciones desde el Boraccast
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }


    private void handleIntent(Intent intent) {
        if (intent != null && intent.getAction() != null) {
            if (ACTION_SEND_NOTIFICATION.equals(intent.getAction())) {
                // Manejar la acción de enviar notificación
                String[] listaPendientes = intent.getStringArrayExtra("listaPendiente");
                int eventsCount = intent.getIntExtra("eventsCount", 0);

                totalEventos = eventsCount;
                totalProcesados = totalProcesados + listaPendientes.length;

                if (listaPendientes != null) {

                    for (String m : listaPendientes
                    ) {
                        Mascota p = new Mascota();
                        p.setFromTEXTLineformat(m);
                        int idAvellanoBorrar = p.getIdAvellano();
                        misMascotas.removeIf(mascota -> idAvellanoBorrar == mascota.getIdAvellano());
                        p.setId(misMascotas.size());
                        misMascotas.add(p);
                    }
                    CargarLista();
                    notificarBroadcast(listaPendientes);
                }

            }
        }
    }


    private BroadcastReceiver resultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent != null && ACTION_RECEIVE_CANCELED.equals(intent.getAction())) {
                Bundle extras  = intent.getExtras();
                totalEventos = extras.getInt("eventsCount");
                vDatos.setText(getString(R.string.lbleventosCount)+totalEventos + getString(R.string.lblEventProcesados) +totalProcesados);

            }

        }
    };

    public void registrarFilterReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_RECEIVE_CANCELED);
        intentFilter.addAction(ACTION_RECEIVE_RESULT);
        LocalBroadcastManager.getInstance(this).registerReceiver(resultReceiver, intentFilter);

    }

    private void notificarBroadcast(String[] listaPendiente){
        Intent iToBroadcast = new Intent(getApplicationContext(), Broadcast_Message.class);
        iToBroadcast.setAction(ACTION_SEND_NOTIFICATION);
        iToBroadcast.putExtra("limpiarLista", listaPendiente);
        sendBroadcast(iToBroadcast);

    }
    private void IniciarVistas() {

        serviceActivador = (Switch) findViewById(R.id.swService);
        ListView listView = (ListView) findViewById(R.id.lv1);
        vDatos = (TextView) findViewById((R.id.tvData));
        tvNdatos = (TextView) findViewById(R.id.lblNmascotas);
        lv1 = (ListView) findViewById(R.id.lv1);

    }

    private void CargarListening() {

        serviceActivador.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // iniciar Servicio
                    Intent intent = new Intent(MainActivity.this, ServiceMascota.class);
                    intent.putExtra("url", urlCentinela);
                    intent.putExtra("pause", false);
                    startService(intent);
                    Toast.makeText(MainActivity.this, R.string.servicio_iniciado, Toast.LENGTH_SHORT).show();

                } else {
                    //alarmMgr.cancel(pendingIntent);
                    Intent intent = new Intent(MainActivity.this, ServiceMascota.class);
                    intent.putExtra("pause", true);
                    startService(intent);
                    Toast.makeText(MainActivity.this, R.string.servicio_detenido, Toast.LENGTH_SHORT).show();

                }
            }
        });

    }

    private void CargarLista() {
        SharedPreferences preferencias = getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencias.edit();
        if (!misMascotas.isEmpty()) {
            //Lista con items, preparar Url Centinela
            editor.putString("url", urlCentinela);
            MascotaListAdapter adaptadorMascota = new MascotaListAdapter(MainActivity.this, misMascotas);
            lv1.setAdapter(adaptadorMascota);
            adaptadorMascota.notifyDataSetChanged();

        } else {
            //Lista Vacía traer de Inicial
            editor.putString("url", urlInicial);

            Intent i = new Intent(this, HttpConnection.class);
            startActivityForResult(i, 1111);
        }
        editor.commit();

        vDatos.setText(getString(R.string.events)+ totalEventos + getString(R.string.procesados) +totalProcesados);
    }

    //Result de httpConection viene de Activity_third;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1111 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();

            if (extras != null) {
              //  Log.d("Main_OnActivity", "Recibiendo Http- OnActivity");

                String[] vList = extras.getStringArray("list");

                if (vList != null) {
                    lista = new ListaMascota(this);
                    misMascotas = lista.setListFromTEXTLineformat(vList);
                    lista.lista = misMascotas;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            CargarLista(); // Actualizar la interfaz de usuario en el hilo principal
                        }
                    });
                }

            }

        }
    }
    @Override
    protected void onDestroy() {
        // Desregistrar el receptor de broadcast
        LocalBroadcastManager.getInstance(this).unregisterReceiver(resultReceiver);
        super.onDestroy();
    }


}
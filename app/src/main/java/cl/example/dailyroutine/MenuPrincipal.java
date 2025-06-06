package cl.example.dailyroutine;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class MenuPrincipal extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    ListView listView;
    static ArrayList<Rutina> listaRutinas;
    private adaptadorRutinas adaptadorRutinasObj;
    private String nombreUsuarioActual;

    private FloatingActionButton fabAnadirRutina;
    private ImageButton botonOpcionesMenu;
    private ImageView imageViewAvatarMenu;
    private TextView textViewTituloMenu;
    private TextView textViewStreakCount;
    private ImageView imageViewStreakIcon;
    private TextView textViewStreakFreezerCount;
    private ImageView imageViewStreakFreezerIcon;
    private TextView textViewPointsCount;
    private ImageView imageViewPointsIcon;
    private SearchView searchViewRutinas;
    private static final int COSTO_CONGELADOR = 50;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    public static final String PREFS_NAME = "DailyRoutinePrefs";
    public static final String KEY_RUTINAS = "rutinasList";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_principal);

        mAuth = FirebaseAuth.getInstance();
        mGoogleSignInClient = GoogleSignIn.getClient(this,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build());

        nombreUsuarioActual = getIntent().getStringExtra("usuario");
        if (nombreUsuarioActual == null || nombreUsuarioActual.isEmpty()) {
            nombreUsuarioActual = "Usuario";
        }

        cargarRutinas(this);

        listView = findViewById(R.id.listaRutinas);
        adaptadorRutinasObj = new adaptadorRutinas(listaRutinas, MenuPrincipal.this);
        listView.setAdapter(adaptadorRutinasObj);

        fabAnadirRutina = findViewById(R.id.fabAnadirRutina);
        botonOpcionesMenu = findViewById(R.id.botonOpcionesMenu);
        imageViewAvatarMenu = findViewById(R.id.imageview_avatar_menu);
        textViewTituloMenu = findViewById(R.id.textViewTituloMenu);
        textViewStreakCount = findViewById(R.id.textViewStreakCount);
        imageViewStreakIcon = findViewById(R.id.imageViewStreakIcon);
        textViewStreakFreezerCount = findViewById(R.id.textViewStreakFreezerCount);
        imageViewStreakFreezerIcon = findViewById(R.id.imageViewStreakFreezerIcon);
        textViewPointsCount = findViewById(R.id.textViewPointsCount);
        imageViewPointsIcon = findViewById(R.id.imageViewPointsIcon);
        searchViewRutinas = findViewById(R.id.searchview_rutinas);


        searchViewRutinas.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adaptadorRutinasObj != null) {
                    adaptadorRutinasObj.getFilter().filter(newText);
                }
                return true;
            }
        });

        fabAnadirRutina.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MenuPrincipal.this, CrearRutina.class));
            }
        });

        botonOpcionesMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }
        });

        imageViewStreakFreezerIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MenuPrincipal.this)
                        .setTitle("Comprar Congelador de Racha")
                        .setMessage("¿Quieres comprar un congelador de racha por " + COSTO_CONGELADOR + " puntos?")
                        .setPositiveButton("Comprar", (dialog, which) -> {
                            if (GestorDeRachas.restarPuntos(MenuPrincipal.this, COSTO_CONGELADOR)) {
                                GestorDeRachas.añadirCongeladores(MenuPrincipal.this, 1);
                                actualizarVisualizacionRacha();
                                Toast.makeText(MenuPrincipal.this, "¡Congelador comprado! Te quedan " + GestorDeRachas.getCantidadPuntos(MenuPrincipal.this) + " puntos.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MenuPrincipal.this, "¡Puntos insuficientes! Necesitas " + COSTO_CONGELADOR + " puntos para comprar un congelador.", Toast.LENGTH_LONG).show();
                            }
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }
        });

        imageViewPointsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GestorDeRachas.sumarPuntos(MenuPrincipal.this, 10);
                actualizarVisualizacionRacha();
                Toast.makeText(MenuPrincipal.this, "¡Añadidos 10 puntos!", Toast.LENGTH_SHORT).show();
            }
        });

        actualizarVisualizacionRacha();
        cargarDatosPerfil();
    }

    private void cargarDatosPerfil() {
        SharedPreferences prefs = getSharedPreferences("PreferenciasPerfil", Context.MODE_PRIVATE);

        String nombrePantalla = prefs.getString("nombrePantalla", "");
        if (nombrePantalla.isEmpty()) {
            textViewTituloMenu.setText("Mis Rutinas");
        } else {
            textViewTituloMenu.setText("¡Hola, " + nombrePantalla + "!");
        }

        String avatarUriString = prefs.getString("avatarUri", null);
        if (avatarUriString != null) {
            try {
                imageViewAvatarMenu.setImageURI(Uri.parse(avatarUriString));
            } catch (Exception e) {
                imageViewAvatarMenu.setImageResource(R.drawable.perfil);
            }
        } else {
            imageViewAvatarMenu.setImageResource(R.drawable.perfil);
        }
    }


    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.menu_principal_opciones);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_opcion_perfil) {
            Intent intentPerfil = new Intent(MenuPrincipal.this, PerfilActivity.class);
            intentPerfil.putExtra(PerfilActivity.EXTRA_USERNAME, nombreUsuarioActual);
            startActivity(intentPerfil);
            return true;
        } else if (itemId == R.id.menu_opcion_historial) {
            startActivity(new Intent(MenuPrincipal.this, HistorialActivity.class));
            return true;
        } else if (itemId == R.id.menu_opcion_estadisticas) {
            startActivity(new Intent(MenuPrincipal.this, EstadisticasActivity.class));
            return true;
        } else if (itemId == R.id.menu_opcion_rutinas_famosas) {
            startActivity(new Intent(MenuPrincipal.this, RutinasFamosasActivity.class));
            return true;
        } else if (itemId == R.id.menu_opcion_cerrar_sesion) {
            cerrarSesion();
            return true;
        } else {
            return false;
        }
    }

    private void cerrarSesion() {
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Toast.makeText(MenuPrincipal.this, "Sesión cerrada.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MenuPrincipal.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    public static void cargarRutinas(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(KEY_RUTINAS, null);

        if (json != null) {
            Type type = new TypeToken<ArrayList<Rutina>>() {}.getType();
            listaRutinas = gson.fromJson(json, type);
            int maxId = 0;
            if (listaRutinas != null) {
                for (Rutina r : listaRutinas) {
                    if (r.getId() > maxId) {
                        maxId = r.getId();
                    }
                }
            }
            Rutina.setProximoId(maxId + 1);
        } else {
            listaRutinas = new ArrayList<>();
            cargarInformacionInicialSiEsNecesario(context);
        }
    }

    public static void guardarRutinas(Context context) {
        if (listaRutinas != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Gson gson = new Gson();
            String json = gson.toJson(listaRutinas);
            editor.putString(KEY_RUTINAS, json);
            editor.apply();
            Log.d("MenuPrincipal", "Rutinas guardadas localmente.");
        }
    }

    public static void cargarInformacionInicialSiEsNecesario(Context context) {
        if (listaRutinas != null && listaRutinas.isEmpty()) {
            Rutina.setProximoId(1);

            ArrayList<Actividad> acts1 = new ArrayList<>();
            acts1.add(new Actividad("Correr 30 min", false));
            acts1.add(new Actividad("Estiramientos", false));
            Rutina r1 = new Rutina("Entrenamiento Mañana", "07/06/2025", acts1, "Ejercicio");
            r1.setRecordatorioActivo(true);
            r1.setHoraRecordatorio("07:00");
            r1.setTextoRecordatorioPersonalizado("¡Hora de empezar el día con energía!");
            r1.setDiasSemana(Arrays.asList(Calendar.MONDAY, Calendar.WEDNESDAY, Calendar.FRIDAY));
            listaRutinas.add(r1);

            ArrayList<Actividad> acts2 = new ArrayList<>();
            acts2.add(new Actividad("Leer capítulo Android", false));
            Rutina r2 = new Rutina("Estudio Tarde", "07/06/2025", acts2, "Educación");
            r2.setRecordatorioActivo(false);
            listaRutinas.add(r2);

            ArrayList<Actividad> acts3 = new ArrayList<>();
            acts3.add(new Actividad("Lavar platos", false));
            acts3.add(new Actividad("Ordenar habitación", false));
            Rutina r3 = new Rutina("Tareas Hogar", "07/06/2025", acts3, "Hogar");
            r3.setRecordatorioActivo(true);
            r3.setHoraRecordatorio("18:30");
            r3.setTextoRecordatorioPersonalizado("");
            r3.setDiasSemana(Arrays.asList(Calendar.SATURDAY, Calendar.SUNDAY));
            listaRutinas.add(r3);

            guardarRutinas(context);
            Log.d("MenuPrincipal", "Información inicial cargada y guardada.");
        }
    }

    public static void marcarRutinaComoCompletadaYGuardar(Context context, int rutinaId) {
        if (listaRutinas == null) {
            cargarRutinas(context);
        }

        Rutina rutinaToUpdate = null;
        if (listaRutinas != null) {
            for (Rutina r : listaRutinas) {
                if (r.getId() == rutinaId) {
                    rutinaToUpdate = r;
                    break;
                }
            }
        }

        if (rutinaToUpdate != null) {
            boolean allActivitiesAlreadyCompleted = rutinaToUpdate.todasActividadesCompletadas();

            if (rutinaToUpdate.getActividades() != null && !rutinaToUpdate.getActividades().isEmpty()) {
                for (Actividad actividad : rutinaToUpdate.getActividades()) {
                    actividad.setCompletada(true);
                }
            } else {
                Log.d("MenuPrincipal", "Rutina " + rutinaToUpdate.getNombre() + " no tiene actividades, se considera completada.");
            }

            guardarRutinas(context);

            if (!allActivitiesAlreadyCompleted && rutinaToUpdate.todasActividadesCompletadas()) {
                GestorDeRachas.rutinaCompletadaHoy(context);
                GestorDeRachas.sumarPuntos(context, 10);
                Log.d("MenuPrincipal", "GestorDeRachas llamado desde acción de notificación para: " + rutinaToUpdate.getNombre());
            }

        } else {
            Log.e("MenuPrincipal", "Rutina con ID " + rutinaId + " no encontrada para marcar como completada.");
        }
    }

    private void actualizarVisualizacionRacha() {
        SharedPreferences prefs = getSharedPreferences(GestorDeRachas.PREFS_RACHAS_NAME, MODE_PRIVATE);

        int rachaActual = prefs.getInt(GestorDeRachas.KEY_CONTADOR_RACHA_ACTUAL, 0);
        textViewStreakCount.setText(String.valueOf(rachaActual));

        int congeladoresActuales = GestorDeRachas.getCantidadCongeladores(this);
        textViewStreakFreezerCount.setText(String.valueOf(congeladoresActuales));

        int puntosActuales = GestorDeRachas.getCantidadPuntos(this);
        textViewPointsCount.setText(String.valueOf(puntosActuales));

        if (rachaActual == 0) {
            imageViewStreakIcon.setImageResource(R.drawable.ic_notflame_pomodoro);
        } else {
            imageViewStreakIcon.setImageResource(R.drawable.ic_flame_pomodoro);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        guardarRutinas(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adaptadorRutinasObj != null) {
            adaptadorRutinasObj.getFilter().filter(searchViewRutinas.getQuery());
            adaptadorRutinasObj.notifyDataSetChanged();
        }
        actualizarVisualizacionRacha();
        cargarDatosPerfil();
    }
}
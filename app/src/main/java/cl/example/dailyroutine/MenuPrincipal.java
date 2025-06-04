// app/src/main/java/cl/example/dailyroutine/MenuPrincipal.java
package cl.example.dailyroutine;

import android.content.Intent;
import android.content.SharedPreferences; // Importar SharedPreferences
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.gson.Gson; // Importar Gson
import com.google.gson.reflect.TypeToken; // Importar TypeToken para Gson

import java.lang.reflect.Type; // Importar Type
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Calendar;

public class MenuPrincipal extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    ListView listView;
    static ArrayList<Rutina> listaRutinas;
    private adaptadorRutinas adaptadorRutinasObj;
    private String nombreUsuarioActual;

    private FloatingActionButton fabAnadirRutina;
    private ImageButton botonOpcionesMenu;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    public static final String PREFS_NAME = "DailyRoutinePrefs"; // Nombre del archivo de preferencias
    public static final String KEY_RUTINAS = "rutinasList"; // Clave para la lista de rutinas


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

        cargarRutinas(); // Cargar rutinas al iniciar la actividad

        listView = findViewById(R.id.listaRutinas);
        adaptadorRutinasObj = new adaptadorRutinas(listaRutinas, MenuPrincipal.this);
        listView.setAdapter(adaptadorRutinasObj);


        fabAnadirRutina = findViewById(R.id.fabAnadirRutina);
        botonOpcionesMenu = findViewById(R.id.botonOpcionesMenu);

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

    private void cargarRutinas() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(KEY_RUTINAS, null);

        if (json != null) {
            Type type = new TypeToken<ArrayList<Rutina>>() {}.getType();
            listaRutinas = gson.fromJson(json, type);
            // Asegurarse de que el proximoId se actualice correctamente después de cargar
            // para evitar IDs duplicados al crear nuevas rutinas.
            int maxId = 0;
            for (Rutina r : listaRutinas) {
                if (r.getId() > maxId) {
                    maxId = r.getId();
                }
            }
            Rutina.setProximoId(maxId + 1); // Establecer el próximo ID disponible
        } else {
            listaRutinas = new ArrayList<>();
            cargarInformacionInicialSiEsNecesario(); // Cargar datos iniciales solo si no hay datos guardados
        }
    }

    private void guardarRutinas() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(listaRutinas);
        editor.putString(KEY_RUTINAS, json);
        editor.apply();
    }

    public void cargarInformacionInicialSiEsNecesario() {
        // Esta función ahora solo se llamará si no hay datos guardados previamente.
        // Se mantiene la lógica de inicialización.
        ArrayList<Actividad> acts1 = new ArrayList<>();
        acts1.add(new Actividad("Correr 30 min", true));
        acts1.add(new Actividad("Estiramientos", false));
        Rutina r1 = new Rutina(0, "Entrenamiento Mañana", "03/06/2025", acts1, "Ejercicio", true, "07:00", "¡Hora de empezar el día con energía!", Arrays.asList(Calendar.MONDAY, Calendar.WEDNESDAY, Calendar.FRIDAY));
        listaRutinas.add(r1);

        ArrayList<Actividad> acts2 = new ArrayList<>();
        acts2.add(new Actividad("Leer capítulo Android", true));
        Rutina r2 = new Rutina(0, "Estudio Tarde", "04/06/2025", acts2, "Educación", false, "", "", new ArrayList<Integer>());
        listaRutinas.add(r2);

        ArrayList<Actividad> acts3 = new ArrayList<>();
        acts3.add(new Actividad("Lavar platos"));
        acts3.add(new Actividad("Ordenar habitación", true));
        Rutina r3 = new Rutina(0, "Tareas Hogar", "04/06/2025", acts3, "Hogar", true, "18:30", "", Arrays.asList(Calendar.SATURDAY, Calendar.SUNDAY));
        listaRutinas.add(r3);

        // Guardar las rutinas iniciales después de cargarlas por primera vez
        guardarRutinas();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Guardar rutinas cada vez que la actividad está a punto de dejar de ser visible
        guardarRutinas();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cuando se regresa a esta actividad, refrescar la lista.
        // Los datos ya deberían estar cargados por cargarRutinas() en onCreate
        // o por la actualización de CrearaRutina, DetalleRutina, etc.
        if (adaptadorRutinasObj != null) {
            adaptadorRutinasObj.notifyDataSetChanged();
        }
    }
}

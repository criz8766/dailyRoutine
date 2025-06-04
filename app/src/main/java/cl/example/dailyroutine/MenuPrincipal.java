package cl.example.dailyroutine;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem; // Importar MenuItem
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
// import android.widget.TextView; // No se usa directamente en este cambio
import android.widget.Toast;
import androidx.appcompat.widget.PopupMenu; // Importar PopupMenu

import androidx.appcompat.app.AppCompatActivity;
// import androidx.appcompat.widget.Toolbar; // No es estrictamente necesario para el PopupMenu

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List; // Importar List si aún no está
import java.util.Arrays; // Importar Arrays si se necesita para listas iniciales
import java.util.Calendar; // Importar Calendar para las constantes de días

public class MenuPrincipal extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener { // Implementar listener

    ListView listView;
    static ArrayList<Rutina> listaRutinas;
    private adaptadorRutinas adaptadorRutinasObj;
    private String nombreUsuarioActual;

    private FloatingActionButton fabAnadirRutina;
    private ImageButton botonOpcionesMenu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_principal);

        nombreUsuarioActual = getIntent().getStringExtra("usuario");
        if (nombreUsuarioActual == null || nombreUsuarioActual.isEmpty()) {
            nombreUsuarioActual = "Usuario";
        }

        // Aquí podríamos cargar las rutinas desde almacenamiento persistente en el futuro
        // Por ahora, si la lista es null o vacía, cargamos las iniciales.
        if (listaRutinas == null || listaRutinas.isEmpty()) { // Añadida verificación de isEmpty
            listaRutinas = new ArrayList<>();
            cargarInformacionInicialSiEsNecesario();
        }


        listView = findViewById(R.id.listaRutinas);
        // Podríamos necesitar filtrar las rutinas aquí para mostrar solo las del día actual
        // Esto dependerá de cómo queramos presentar las rutinas en la pantalla principal
        // Por ahora, se muestran todas como antes.
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
                showPopupMenu(v); // Llamar al método para mostrar el PopupMenu
            }
        });
    }

    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.setOnMenuItemClickListener(this); // 'this' implementa OnMenuItemClickListener
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
        } else {
            return false;
        }
    }

    public void cargarInformacionInicialSiEsNecesario() {
        // Solo cargar si la lista está realmente vacía
        if (listaRutinas == null || listaRutinas.isEmpty()) {
            listaRutinas = new ArrayList<>(); // Asegurarse de que esté inicializada

            ArrayList<Actividad> acts1 = new ArrayList<>();
            acts1.add(new Actividad("Correr 30 min", true));
            acts1.add(new Actividad("Estiramientos", false));
            // Añadido List<Integer> como noveno argumento
            Rutina r1 = new Rutina(0, "Entrenamiento Mañana", "03/06/2025", acts1, "Ejercicio", true, "07:00", "¡Hora de empezar el día con energía!", Arrays.asList(Calendar.MONDAY, Calendar.WEDNESDAY, Calendar.FRIDAY)); // Ejemplo: L, X, V
            listaRutinas.add(r1);

            ArrayList<Actividad> acts2 = new ArrayList<>();
            acts2.add(new Actividad("Leer capítulo Android", true));
            // Añadido List<Integer> como noveno argumento
            Rutina r2 = new Rutina(0, "Estudio Tarde", "04/06/2025", acts2, "Educación", false, "", "", new ArrayList<Integer>()); // Ejemplo: Sin días específicos
            listaRutinas.add(r2);

            ArrayList<Actividad> acts3 = new ArrayList<>();
            acts3.add(new Actividad("Lavar platos"));
            acts3.add(new Actividad("Ordenar habitación", true));
            // Añadido List<Integer> como noveno argumento
            Rutina r3 = new Rutina(0, "Tareas Hogar", "04/06/2025", acts3, "Hogar", true, "18:30", "", Arrays.asList(Calendar.SATURDAY, Calendar.SUNDAY)); // Ejemplo: S, D
            listaRutinas.add(r3);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cuando se regresa a esta actividad, refrescar la lista.
        // En el futuro, aquí también podrías filtrar las rutinas del día actual
        // antes de pasar la lista al adaptador.
        if (adaptadorRutinasObj != null) {
            adaptadorRutinasObj.notifyDataSetChanged();
        }
    }
}

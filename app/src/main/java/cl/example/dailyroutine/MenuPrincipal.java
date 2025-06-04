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

        if (listaRutinas == null) {
            listaRutinas = new ArrayList<>();
            cargarInformacionInicialSiEsNecesario();
        }

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
                showPopupMenu(v); // Llamar al método para mostrar el PopupMenu
            }
        });
    }

    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.setOnMenuItemClickListener(this); // 'this' implementa OnMenuItemClickListener
        popup.inflate(R.menu.menu_principal_opciones);
        // Intenta forzar la visualización de iconos si es necesario (para versiones más antiguas o temas específicos)
        // Esto a veces es necesario para que los iconos se muestren en PopupMenu.
        // No hay una forma estándar directa y garantizada en todos los casos vía XML para PopupMenu,
        // pero el inflador debería respetar los 'android:icon' del <item>.
        // Si los iconos no aparecen, podrías necesitar una solución más programática o un menú personalizado.
        // Por ahora, confiamos en que el inflador los mostrará.
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
        if (listaRutinas.isEmpty()) {
            ArrayList<Actividad> acts1 = new ArrayList<>();
            acts1.add(new Actividad("Correr 30 min", true));
            acts1.add(new Actividad("Estiramientos", false));
            Rutina r1 = new Rutina(0, "Entrenamiento Mañana", "03/06/2025", acts1, "Ejercicio", true, "07:00", "¡Hora de empezar el día con energía!");
            listaRutinas.add(r1);

            ArrayList<Actividad> acts2 = new ArrayList<>();
            acts2.add(new Actividad("Leer capítulo Android", true));
            Rutina r2 = new Rutina(0, "Estudio Tarde", "04/06/2025", acts2, "Educación", false, "", "");
            listaRutinas.add(r2);

            ArrayList<Actividad> acts3 = new ArrayList<>();
            acts3.add(new Actividad("Lavar platos"));
            acts3.add(new Actividad("Ordenar habitación", true));
            Rutina r3 = new Rutina(0, "Tareas Hogar", "04/06/2025", acts3, "Hogar", true, "18:30", "");
            listaRutinas.add(r3);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adaptadorRutinasObj != null) {
            adaptadorRutinasObj.notifyDataSetChanged();
        }
    }
}
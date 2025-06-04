package cl.example.dailyroutine;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MenuPrincipal extends AppCompatActivity {

    ListView listView;
    static ArrayList<Rutina> listaRutinas;
    private adaptadorRutinas adaptadorRutinasObj;
    private String nombreUsuarioActual;

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

        ImageButton botonAnadirRutina = findViewById(R.id.calendario);
        ImageButton botonHistorial = findViewById(R.id.botonHistorial);
        ImageButton botonPerfil = findViewById(R.id.botonPerfil);

        botonAnadirRutina.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MenuPrincipal.this, CrearRutina.class));
            }
        });

        botonHistorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuPrincipal.this, HistorialActivity.class));
            }
        });

        botonPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuPrincipal.this, PerfilActivity.class);
                intent.putExtra(PerfilActivity.EXTRA_USERNAME, nombreUsuarioActual);
                startActivity(intent);
            }
        });
    }

    public void cargarInformacionInicialSiEsNecesario() {
        if (listaRutinas.isEmpty()) {
            // Rutina 1 con recordatorio
            ArrayList<Actividad> acts1 = new ArrayList<>();
            acts1.add(new Actividad("Correr 30 min", true));
            acts1.add(new Actividad("Estiramientos", false));
            // Usamos el constructor que incluye todos los campos de recordatorio. El ID se genera dentro.
            Rutina r1 = new Rutina(0,"Entrenamiento Mañana", "29/05/2025", acts1, "Ejercicio", true, "07:00");
            listaRutinas.add(r1);

            // Rutina 2 sin recordatorio
            ArrayList<Actividad> acts2 = new ArrayList<>();
            acts2.add(new Actividad("Leer capítulo Android", true));
            Rutina r2 = new Rutina(0,"Estudio Tarde", "30/05/2025", acts2, "Educación", false, ""); // Hora vacía o nula si no hay recordatorio
            listaRutinas.add(r2);

            // Rutina 3 con recordatorio
            ArrayList<Actividad> acts3 = new ArrayList<>();
            acts3.add(new Actividad("Lavar platos"));
            acts3.add(new Actividad("Ordenar habitación", true));
            Rutina r3 = new Rutina(0,"Tareas Hogar", "30/05/2025", acts3, "Hogar", true, "18:30");
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
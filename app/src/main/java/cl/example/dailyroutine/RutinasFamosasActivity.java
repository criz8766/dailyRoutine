package cl.example.dailyroutine;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class RutinasFamosasActivity extends AppCompatActivity {

    private ListView lvRutinasFamosas;
    private AdaptadorRutinasFamosas adaptador;
    private ArrayList<Rutina> listaDeRutinasFamosas;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rutinas_famosas);

        toolbar = findViewById(R.id.toolbar_rutinas_famosas);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        lvRutinasFamosas = findViewById(R.id.listview_rutinas_famosas);
        listaDeRutinasFamosas = new ArrayList<>();
        cargarRutinasFamosasDeEjemplo();

        adaptador = new AdaptadorRutinasFamosas(this, listaDeRutinasFamosas);
        lvRutinasFamosas.setAdapter(adaptador);
    }

    private void cargarRutinasFamosasDeEjemplo() {
        // Ejemplo 1
        Rutina r1 = new Rutina(); // ID se autogenera
        r1.setNombre("Mañana Milagrosa (S.A.V.E.R.S.)");
        r1.setCategoria("Productividad");
        r1.setFecha("Rutina popular para empezar el día con energía y enfoque. Basada en 6 prácticas clave."); // Usando fecha como descripción
        ArrayList<Actividad> acts1 = new ArrayList<>();
        acts1.add(new Actividad("Silencio (Meditación/Oración)"));
        acts1.add(new Actividad("Afirmaciones"));
        acts1.add(new Actividad("Visualización"));
        acts1.add(new Actividad("Ejercicio (ligero)"));
        acts1.add(new Actividad("Lectura"));
        acts1.add(new Actividad("Escribir (Scribing/Journaling)"));
        r1.setActividades(acts1);
        listaDeRutinasFamosas.add(r1);

        // Ejemplo 2
        Rutina r2 = new Rutina();
        r2.setNombre("Entrenamiento 5x5 Básico");
        r2.setCategoria("Ejercicio de Fuerza");
        r2.setFecha("Programa simple y efectivo para ganar fuerza, alternando dos tipos de entrenamiento.");
        ArrayList<Actividad> acts2 = new ArrayList<>();
        acts2.add(new Actividad("Día A: Sentadillas 5x5, Press de Banca 5x5, Remo con Barra 5x5"));
        acts2.add(new Actividad("Día B: Sentadillas 5x5, Press Militar 5x5, Peso Muerto 1x5"));
        acts2.add(new Actividad("(Alternar Día A y Día B, con días de descanso)"));
        r2.setActividades(acts2);
        listaDeRutinasFamosas.add(r2);

        // Ejemplo 3
        Rutina r3 = new Rutina();
        r3.setNombre("Técnica Pomodoro (Estudio/Trabajo)");
        r3.setCategoria("Concentración");
        r3.setFecha("Maximiza la concentración con bloques de trabajo enfocado y descansos programados.");
        ArrayList<Actividad> acts3 = new ArrayList<>();
        acts3.add(new Actividad("Elegir una tarea"));
        acts3.add(new Actividad("Trabajar en la tarea - 25 min (1 Pomodoro)"));
        acts3.add(new Actividad("Descanso corto - 5 min"));
        acts3.add(new Actividad("Repetir 3-4 Pomodoros"));
        acts3.add(new Actividad("Descanso largo - 15-30 min"));
        r3.setActividades(acts3);
        listaDeRutinasFamosas.add(r3);

        // Ejemplo 4
        Rutina r4 = new Rutina();
        r4.setNombre("Rutina de Relajación Nocturna");
        r4.setCategoria("Bienestar");
        r4.setFecha("Prepara tu cuerpo y mente para un sueño reparador y de calidad.");
        ArrayList<Actividad> acts4 = new ArrayList<>();
        acts4.add(new Actividad("Evitar pantallas (móvil, TV) 1h antes de dormir"));
        acts4.add(new Actividad("Tomar una infusión relajante (manzanilla, tila)"));
        acts4.add(new Actividad("Leer un libro físico"));
        acts4.add(new Actividad("Ejercicios de respiración o meditación breve"));
        acts4.add(new Actividad("Asegurar oscuridad y silencio en la habitación"));
        r4.setActividades(acts4);
        listaDeRutinasFamosas.add(r4);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
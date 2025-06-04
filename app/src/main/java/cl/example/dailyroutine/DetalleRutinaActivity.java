package cl.example.dailyroutine;

import android.os.Bundle;
import android.util.Log; // Importar Log
import android.view.MenuItem;
import android.view.View; // Importar View
import android.widget.Button;
// import android.widget.CheckBox; // No se usa directamente aquí
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DetalleRutinaActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvNombreRutinaDetalle, tvFechaRutinaDetalle, tvCategoriaRutinaDetalle;
    private ListView lvActividadesDetalle;
    private Button btnMarcarTodasCompletadas, btnDesmarcarTodas; // Botones añadidos

    private Rutina rutinaActual;
    private int posicionRutinaActual; // No se usa directamente, pero bueno tenerla si es necesaria
    private AdaptadorActividades adaptadorActividades;

    public static final String EXTRA_POSICION_RUTINA_DETALLE = "cl.example.dailyroutine.POSICION_RUTINA_DETALLE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_rutina); // Asumiendo que este layout ya tiene los botones

        toolbar = findViewById(R.id.toolbar_detalle_rutina);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        tvNombreRutinaDetalle = findViewById(R.id.textview_nombre_rutina_detalle);
        tvFechaRutinaDetalle = findViewById(R.id.textview_fecha_rutina_detalle);
        tvCategoriaRutinaDetalle = findViewById(R.id.textview_categoria_rutina_detalle); // Asegúrate que este ID exista
        lvActividadesDetalle = findViewById(R.id.listview_actividades_detalle);
        btnMarcarTodasCompletadas = findViewById(R.id.boton_marcar_todas_completadas); // Asegúrate que este ID exista
        btnDesmarcarTodas = findViewById(R.id.boton_desmarcar_todas); // Asegúrate que este ID exista

        posicionRutinaActual = getIntent().getIntExtra(EXTRA_POSICION_RUTINA_DETALLE, -1);

        if (posicionRutinaActual != -1 && MenuPrincipal.listaRutinas != null && posicionRutinaActual < MenuPrincipal.listaRutinas.size()) { //
            rutinaActual = MenuPrincipal.listaRutinas.get(posicionRutinaActual); //
        } else {
            Toast.makeText(this, "Error al cargar la rutina.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        cargarDatosRutina();

        // Inicializar lista de actividades si es null para evitar NullPointerException
        if (rutinaActual.getActividades() == null) { //
            rutinaActual.setActividades(new ArrayList<>()); //
        }

        adaptadorActividades = new AdaptadorActividades(this, rutinaActual.getActividades(), new AdaptadorActividades.OnActividadCheckedChangeListener() {
            @Override
            public void onActividadCheckedChanged(int position, boolean isChecked) {
                // El estado de la actividad ya se actualiza dentro del AdaptadorActividades
                // Ahora, verificamos si TODAS las actividades de la rutina están completadas
                if (rutinaActual.todasActividadesCompletadas()) { //
                    verificarYActualizarRacha();
                }
            }
        });
        lvActividadesDetalle.setAdapter(adaptadorActividades);


        btnMarcarTodasCompletadas.setOnClickListener(v -> {
            marcarTodasLasActividades(true);
            // Después de marcar todas, verificamos si la rutina está completa y actualizamos racha
            if (rutinaActual.todasActividadesCompletadas()) { //
                verificarYActualizarRacha();
            }
        });

        btnDesmarcarTodas.setOnClickListener(v -> {
            marcarTodasLasActividades(false);
            // No se actualiza racha al desmarcar todas, ya que la rutina ya no estaría "completada hoy"
        });
    }

    private void cargarDatosRutina() {
        if (rutinaActual != null) {
            tvNombreRutinaDetalle.setText(rutinaActual.getNombre()); //
            tvFechaRutinaDetalle.setText("Fecha: " + rutinaActual.getFecha()); //

            if (tvCategoriaRutinaDetalle != null) { // Comprobar si el TextView existe
                if (rutinaActual.getCategoria() != null && !rutinaActual.getCategoria().isEmpty()) { //
                    tvCategoriaRutinaDetalle.setText("Categoría: " + rutinaActual.getCategoria()); //
                    tvCategoriaRutinaDetalle.setVisibility(View.VISIBLE);
                } else {
                    tvCategoriaRutinaDetalle.setVisibility(View.GONE);
                }
            }

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(rutinaActual.getNombre()); //
            }
        }
    }

    private void marcarTodasLasActividades(boolean completadas) {
        if (rutinaActual != null && rutinaActual.getActividades() != null) { //
            for (Actividad act : rutinaActual.getActividades()) { //
                act.setCompletada(completadas); //
            }
            if (adaptadorActividades != null) {
                adaptadorActividades.notifyDataSetChanged();
            }
        }
    }

    private void verificarYActualizarRacha() {
        if (rutinaActual == null) return;

        SimpleDateFormat sdfInput = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat sdfCompare = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String fechaHoyParaComparar = sdfCompare.format(new Date());
        String fechaRutinaParaComparar = "";

        try {
            Date fechaRutinaDate = sdfInput.parse(rutinaActual.getFecha()); //
            if (fechaRutinaDate != null) {
                fechaRutinaParaComparar = sdfCompare.format(fechaRutinaDate);
            }
        } catch (Exception e) {
            Log.e("DetalleRutinaActivity", "Error al parsear fecha de rutina para racha: " + rutinaActual.getFecha(), e); //
            return; // No continuar si la fecha de la rutina es inválida
        }

        if (fechaRutinaParaComparar.equals(fechaHoyParaComparar)) {
            Log.d("DetalleRutinaActivity", "Rutina (" + rutinaActual.getNombre() + ") completada hoy (" + fechaHoyParaComparar + "). Llamando a GestorDeRachas."); //
            GestorDeRachas.rutinaCompletadaHoy(this);
        } else {
            Log.d("DetalleRutinaActivity", "Rutina (" + rutinaActual.getNombre() + ") completada, pero no es de hoy (Rutina: " + rutinaActual.getFecha() + ", Hoy: " + sdfInput.format(new Date()) + "). No se actualiza racha."); //
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // No es necesario onPause para guardar, ya que los cambios en rutinaActual
    // (que es un objeto de MenuPrincipal.listaRutinas) son directos.
}
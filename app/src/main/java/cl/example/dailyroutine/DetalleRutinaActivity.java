package cl.example.dailyroutine;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;


import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class DetalleRutinaActivity extends AppCompatActivity {

    private TextView tvNombreRutina, tvFechaRutina;
    private ListView lvActividades;
    private Button botonVolver;
    private Toolbar toolbar;

    private Rutina rutinaSeleccionada;
    private AdaptadorActividades adaptadorActividadesObj;
    private int posicionRutina;

    public static final String EXTRA_POSICION_RUTINA_DETALLE = "cl.example.dailyroutine.POSICION_RUTINA_DETALLE";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_rutina);

        tvNombreRutina = findViewById(R.id.textview_nombre_rutina_detalle);
        tvFechaRutina = findViewById(R.id.textview_fecha_rutina_detalle);
        lvActividades = findViewById(R.id.listview_actividades_detalle);
        botonVolver = findViewById(R.id.boton_volver_menu);
        toolbar = findViewById(R.id.toolbar_detalle_rutina);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        posicionRutina = getIntent().getIntExtra(EXTRA_POSICION_RUTINA_DETALLE, -1);

        if (posicionRutina != -1 && MenuPrincipal.listaRutinas != null && posicionRutina < MenuPrincipal.listaRutinas.size()) {
            rutinaSeleccionada = MenuPrincipal.listaRutinas.get(posicionRutina);
            cargarDetallesRutina();
        } else {
            Toast.makeText(this, "Error al cargar la rutina.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        botonVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void cargarDetallesRutina() {
        if (rutinaSeleccionada != null) {
            tvNombreRutina.setText(rutinaSeleccionada.getNombre());
            tvFechaRutina.setText("Fecha: " + rutinaSeleccionada.getFecha());

            if (rutinaSeleccionada.getActividades() == null) {
                rutinaSeleccionada.setActividades(new ArrayList<>());
            }

            adaptadorActividadesObj = new AdaptadorActividades(this, rutinaSeleccionada.getActividades());
            lvActividades.setAdapter(adaptadorActividadesObj);
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

    @Override
    protected void onResume() {
        super.onResume();

    }
}
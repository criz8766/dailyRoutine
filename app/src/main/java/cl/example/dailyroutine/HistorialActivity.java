package cl.example.dailyroutine;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class HistorialActivity extends AppCompatActivity {

    private ListView lvHistorial;
    private AdaptadorHistorial adaptadorHistorialObj; // Renombrado para claridad
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        toolbar = findViewById(R.id.toolbar_historial);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        lvHistorial = findViewById(R.id.listview_historial_rutinas);

        ArrayList<Rutina> rutinasParaHistorial;
        if (MenuPrincipal.listaRutinas != null) {
            rutinasParaHistorial = MenuPrincipal.listaRutinas;
        } else {
            rutinasParaHistorial = new ArrayList<>();
        }

        adaptadorHistorialObj = new AdaptadorHistorial(this, rutinasParaHistorial);
        lvHistorial.setAdapter(adaptadorHistorialObj);
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
        if (MenuPrincipal.listaRutinas != null) {
            adaptadorHistorialObj = new AdaptadorHistorial(this, MenuPrincipal.listaRutinas);
            lvHistorial.setAdapter(adaptadorHistorialObj);
        } else if (adaptadorHistorialObj != null) {
            adaptadorHistorialObj = new AdaptadorHistorial(this, new ArrayList<>());
            lvHistorial.setAdapter(adaptadorHistorialObj);
        }
    }
}
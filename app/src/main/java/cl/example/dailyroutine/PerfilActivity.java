package cl.example.dailyroutine;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

public class PerfilActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView ivAvatar;
    private TextView tvUsername;
    private EditText etNombrePantalla;
    private Button btnGuardarPerfil;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "PreferenciasPerfil";
    private static final String KEY_NOMBRE_PANTALLA = "nombrePantalla";
    public static final String EXTRA_USERNAME = "USERNAME_ACTUAL";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        toolbar = findViewById(R.id.toolbar_perfil);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        ivAvatar = findViewById(R.id.imageview_avatar_perfil);
        tvUsername = findViewById(R.id.textview_username_perfil);
        etNombrePantalla = findViewById(R.id.edittext_nombre_pantalla_perfil);
        btnGuardarPerfil = findViewById(R.id.boton_guardar_perfil);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        String username = getIntent().getStringExtra(EXTRA_USERNAME);
        if (username != null && !username.isEmpty()) {
            tvUsername.setText(username);
        } else {
            tvUsername.setText("Usuario no disponible"); // Fallback
        }

        cargarNombrePantalla();

        btnGuardarPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarNombrePantalla();
            }
        });
    }

    private void cargarNombrePantalla() {
        String nombreGuardado = sharedPreferences.getString(KEY_NOMBRE_PANTALLA, "");
        etNombrePantalla.setText(nombreGuardado);
    }

    private void guardarNombrePantalla() {
        String nombreIngresado = etNombrePantalla.getText().toString().trim();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_NOMBRE_PANTALLA, nombreIngresado);
        editor.apply();
        Toast.makeText(this, "Nombre de pantalla guardado.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Vuelve a la actividad anterior
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
package cl.example.dailyroutine;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
    private static final String KEY_AVATAR_URI = "avatarUri"; // Nueva clave para la URI del avatar
    public static final String EXTRA_USERNAME = "USERNAME_ACTUAL";

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher;

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
        cargarAvatar(); // Cargar avatar guardado

        btnGuardarPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarNombrePantalla();
            }
        });

        // Inicializar ActivityResultLauncher
        galleryActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            ivAvatar.setImageURI(imageUri); // Establecer la imagen seleccionada
                            guardarAvatarUri(imageUri); // Guardar la URI con permiso persistente
                        }
                    }
                });

        // Configurar clic en el avatar para abrir la galería
        ivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirGaleria();
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

    // Método para cargar el avatar guardado
    private void cargarAvatar() {
        String avatarUriString = sharedPreferences.getString(KEY_AVATAR_URI, null);
        if (avatarUriString != null) {
            Uri avatarUri = Uri.parse(avatarUriString);
            try {
                // Intentar cargar la imagen
                ivAvatar.setImageURI(avatarUri);
            } catch (Exception e) {
                e.printStackTrace();
                // Si falla la carga (ej. permiso perdido o imagen borrada), usar el avatar por defecto
                ivAvatar.setImageResource(R.drawable.perfil); // O la imagen por defecto que uses
                // Opcional: remover la URI inválida para evitar futuros errores
                // sharedPreferences.edit().remove(KEY_AVATAR_URI).apply();
                Toast.makeText(this, "Error al cargar la foto de perfil. Usando imagen por defecto.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Método para guardar la URI del avatar con permiso persistente
    private void guardarAvatarUri(Uri uri) {
        try {
            // Solicitar permiso de lectura persistente para esta URI
            final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
            getContentResolver().takePersistableUriPermission(uri, takeFlags);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_AVATAR_URI, uri.toString());
            editor.apply();
            Toast.makeText(this, "Foto de perfil guardada.", Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error de permiso al guardar la foto.", Toast.LENGTH_SHORT).show();
            // Si falla por permiso, podrías querer limpiar la URI guardada
            sharedPreferences.edit().remove(KEY_AVATAR_URI).apply();
        }
    }

    // Método para abrir la galería
    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT); // Usar ACTION_OPEN_DOCUMENT
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Conceder permiso temporal
        galleryActivityResultLauncher.launch(intent);
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

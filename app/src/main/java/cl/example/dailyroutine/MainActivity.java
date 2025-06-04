package cl.example.dailyroutine;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull; // Asegúrate de importar NonNull
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivityGoogleAuth";
    private Context context;
    //private EditText usuarioCapturado;
    //private EditText claveCapturada;
    //private Button botonIniciarSesion;
    private SignInButton botonGoogleSignIn;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        //usuarioCapturado = findViewById(R.id.imputUsuario);
        //claveCapturada = findViewById(R.id.imputClave);
        //botonIniciarSesion = findViewById(R.id.botonIniciarSesion);
        botonGoogleSignIn = findViewById(R.id.botonGoogleSignIn);

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Configurar Google Sign-In
        // R.string.default_web_client_id es generado por el plugin google-services
        // si google-services.json está correctamente configurado y el SHA-1 está en Firebase.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            Log.d(TAG, "firebaseAuthWithGoogle (token ID): " + (account != null ? account.getIdToken() : "account es null"));
                            if (account != null && account.getIdToken() != null) {
                                firebaseAuthWithGoogle(account.getIdToken());
                            } else {
                                Log.e(TAG, "Cuenta de Google o ID Token es null.");
                                Toast.makeText(context, "No se pudo obtener la información de la cuenta de Google.", Toast.LENGTH_LONG).show();
                            }
                        } catch (ApiException e) {
                            Log.w(TAG, "Google sign in failed", e);
                            Toast.makeText(context, "Falló el inicio de sesión con Google. Código: " + e.getStatusCode(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.w(TAG, "Google Sign-In Activity cancelada o con error. Código de resultado: " + result.getResultCode());
                        // Opcional: Mostrar un Toast solo si no fue una cancelación del usuario
                        // if (result.getResultCode() != RESULT_CANCELED) {
                        //    Toast.makeText(context, "Inicio de sesión con Google no completado.", Toast.LENGTH_SHORT).show();
                        // }
                    }
                }
        );

        //botonIniciarSesion.setOnClickListener(new View.OnClickListener() {
            //@Override
            //public void onClick(View v) {
                //String usuarioIn = usuarioCapturado.getText().toString();
                //String claveIn = claveCapturada.getText().toString();
                //if (iniciarSesionSimple(usuarioIn, claveIn)) {
                    //navegarAMenuPrincipal(usuarioIn);
                //} else {
                    //Toast.makeText(context, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                //}
                //usuarioCapturado.setText("");
                //claveCapturada.setText("");
            //}
        //});

        botonGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInConGoogle();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Comprobar si el usuario ya inició sesión con Firebase
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "Usuario ya logueado con Firebase: " + (currentUser.getEmail() != null ? currentUser.getEmail() : currentUser.getUid()));
            String nombreUsuario = currentUser.getDisplayName();
            if (nombreUsuario == null || nombreUsuario.isEmpty()) {
                nombreUsuario = currentUser.getEmail() != null ? currentUser.getEmail() : "Usuario";
            }
            navegarAMenuPrincipal(nombreUsuario);
        }
    }

    private void signInConGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential exitoso");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String nombreUsuario = user.getDisplayName();
                                if (nombreUsuario == null || nombreUsuario.isEmpty()) {
                                    nombreUsuario = user.getEmail() != null ? user.getEmail() : "Usuario";
                                }
                                navegarAMenuPrincipal(nombreUsuario);
                            }
                        } else {
                            Log.w(TAG, "signInWithCredential fallido", task.getException());
                            Toast.makeText(context, "Autenticación con Firebase fallida. " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public boolean iniciarSesionSimple(String usuarioIngresado, String claveIngresada) {
        String adminUser = "admin";
        String adminPassword = "1234";
        return usuarioIngresado.equalsIgnoreCase(adminUser) && claveIngresada.equalsIgnoreCase(adminPassword);
    }

    private void navegarAMenuPrincipal(String nombreUsuario) {
        Intent intent = new Intent(MainActivity.this, MenuPrincipal.class);
        intent.putExtra("usuario", nombreUsuario);
        startActivity(intent);
        finish(); // Finaliza MainActivity para que el usuario no pueda volver con el botón "atrás"
        Toast.makeText(MainActivity.this, "Bienvenido " + nombreUsuario, Toast.LENGTH_SHORT).show();
    }
}
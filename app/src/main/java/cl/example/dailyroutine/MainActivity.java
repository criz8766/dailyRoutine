// app/src/main/java/cl/example/dailyroutine/MainActivity.java
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
import androidx.annotation.NonNull;
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
    private SignInButton botonGoogleSignIn;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        botonGoogleSignIn = findViewById(R.id.botonGoogleSignIn);

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Configurar Google Sign-In
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
                    }
                }
        );

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
            // Verificar si la actividad se lanzó después de un cierre de sesión
            // Esto es crucial para evitar que el usuario "logueado" vuelva automáticamente
            // justo después de cerrar sesión desde MenuPrincipal.
            // Para el cierre de sesión, MenuPrincipal ya usa FLAG_ACTIVITY_CLEAR_TOP,
            // pero si la app se "mató" y se reinició con el usuario logueado, esto lo detectará.
            Log.d(TAG, "Usuario ya logueado con Firebase: " + (currentUser.getEmail() != null ? currentUser.getEmail() : currentUser.getUid()));
            String nombreUsuario = currentUser.getDisplayName();
            if (nombreUsuario == null || nombreUsuario.isEmpty()) {
                nombreUsuario = currentUser.getEmail() != null ? currentUser.getEmail() : "Usuario";
            }
            navegarAMenuPrincipal(nombreUsuario);
        } else {
            Log.d(TAG, "No hay usuario logueado en Firebase.");
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

    private void navegarAMenuPrincipal(String nombreUsuario) {
        Intent intent = new Intent(MainActivity.this, MenuPrincipal.class);
        intent.putExtra("usuario", nombreUsuario);
        startActivity(intent);
        finish();
        Toast.makeText(MainActivity.this, "Bienvenido " + nombreUsuario, Toast.LENGTH_SHORT).show();
    }
}
package cl.example.dailyroutine;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context= this;

        Button botonIniciarSesion = (Button) findViewById(R.id.botonIniciarSesion);
        EditText usuarioCapturado = (EditText) findViewById(R.id.imputUsuario);
        EditText cableCapturada = (EditText) findViewById(R.id.imputClave);

        botonIniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(MainActivity.this, MenuPrincipal.class));
                String usuaioIn = usuarioCapturado.getText().toString();
                String claveIn = cableCapturada.getText().toString();
                //Toast.makeText(MainActivity.this, "Usuario: " +prueba, Toast.LENGTH_SHORT).show();

                if (iniciarSesion(usuaioIn, claveIn)){
                    //startActivity(new Intent(MainActivity.this, MenuPrincipal.class));

                    Intent intent = new Intent(MainActivity.this, MenuPrincipal.class);
                    intent.putExtra("usuario", usuaioIn);
                    startActivity(intent);

                    finish();

                    Toast.makeText(MainActivity.this, "Bienvenido", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(context, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                }
                usuarioCapturado.setText("");
                cableCapturada.setText("");
            }
        });

    }

    public boolean iniciarSesion(String usuarioIngresado, String claveIngresada){
        String adminUser = "admin";
        String adminPassword = "1234";
        if (usuarioIngresado.equalsIgnoreCase(adminUser)){
            if (claveIngresada.equalsIgnoreCase(adminPassword)){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }
}
package cl.example.dailyroutine;

import android.app.Application;
import com.jakewharton.threetenabp.AndroidThreeTen;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Inicializar la librería de fechas y horas
        AndroidThreeTen.init(this);
    }
}
package esime.proyecto.proyectcgm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void usuario(View view){
        startActivity(new Intent(getApplicationContext(), LoginUser.class));
    }
    public void driver(View view){
        startActivity(new Intent(getApplicationContext(), LoginDriver.class));
    }
}
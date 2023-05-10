package esime.proyecto.proyectcgm;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

public class SignUpDriver extends AppCompatActivity {
    TextInputEditText t_placa,t_email,t_password;
    Button b_insertar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_driver);

        t_placa=findViewById(R.id.txtPlaca);
        t_email=findViewById(R.id.txtemail);
        t_password=findViewById(R.id.txtpassword);
        b_insertar=findViewById(R.id.buttonSignUp);

        b_insertar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertarDatos();
            }
        });
    }

    private void insertarDatos() {
        final String placa=t_placa.getText().toString().trim();
        final String email=t_email.getText().toString().trim();
        final String password=t_password.getText().toString().trim();

        final ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("cargando");

        if(placa.isEmpty()){
            t_placa.setError("Campo faltante");
            return;
        } else if (email.isEmpty()) {
            t_email.setError("Campo faltante");
            return;
        }
        else {
            progressDialog.show();
            StringRequest request =new StringRequest(Request.Method.POST, "http://10.0.2.2:80/LoginRegister/insertard.php", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (response.equalsIgnoreCase("Registro correcto"))
                    {
                        Toast.makeText(SignUpDriver.this, "datos insertados", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();

                        Intent intent = new Intent(SignUpDriver.this, LoginUser.class);
                        startActivity(intent);
                    }
                    else{
                        Toast.makeText(SignUpDriver.this, response, Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        Toast.makeText(SignUpDriver.this, "No se pudo insertar", Toast.LENGTH_SHORT).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(SignUpDriver.this,error.getMessage(),Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }){
                @Nullable
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {

                    Map<String,String>params = new HashMap<>();
                    params.put("placa",placa);
                    params.put("email",email);
                    params.put("password",password);

                    return params;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(SignUpDriver.this);
            requestQueue.add(request);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void login(View view){
        startActivity(new Intent(getApplicationContext(),LoginUser.class));
        finish();
    }
}
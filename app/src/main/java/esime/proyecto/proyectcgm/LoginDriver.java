package esime.proyecto.proyectcgm;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

public class LoginDriver extends AppCompatActivity {

    TextInputEditText placa, password;

    String str_placa,str_password;
    String url = "http://10.0.2.2:80/LoginRegister/logind.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_driver);

        placa = findViewById(R.id.lplaca);
        password = findViewById(R.id.lpassword);


    }

    public void Login(View view) {

        if(placa.getText().toString().equals("")){
            Toast.makeText(this, "Enter placa", Toast.LENGTH_SHORT).show();
        }
        else if(password.getText().toString().equals("")){
            Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show();
        }
        else{


            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Por favor espera...");

            progressDialog.show();

            str_placa = placa.getText().toString().trim();
            str_password = password.getText().toString().trim();


            StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    progressDialog.dismiss();

                    if(response.equalsIgnoreCase("Ingreso correcto")){

                        placa.setText("");
                        password.setText("");
                        startActivity(new Intent(getApplicationContext(),MapaDriver.class));
                        Toast.makeText(LoginDriver.this, response, Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(LoginDriver.this, response, Toast.LENGTH_SHORT).show();
                    }

                }
            },new Response.ErrorListener(){

                @Override
                public void onErrorResponse(VolleyError error) {
                    progressDialog.dismiss();
                    Toast.makeText(LoginDriver.this, error.getMessage().toString(), Toast.LENGTH_SHORT).show();
                }
            }

            ){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> params = new HashMap<String, String>();
                    params.put("placa",str_placa);
                    params.put("password",str_password);
                    return params;

                }
            };

            RequestQueue requestQueue = Volley.newRequestQueue(LoginDriver.this);
            requestQueue.add(request);


        }
    }
    public void registro(View view){
        startActivity(new Intent(getApplicationContext(), SignUpDriver.class));
    }
}
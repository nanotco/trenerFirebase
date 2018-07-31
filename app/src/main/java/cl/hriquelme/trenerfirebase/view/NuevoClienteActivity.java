package cl.hriquelme.trenerfirebase.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import cl.hriquelme.trenerfirebase.BaseActivity;
import cl.hriquelme.trenerfirebase.R;


public class NuevoClienteActivity extends BaseActivity {

    private static final String TAG = "NuevoClienteActivity";
    private static final String EMAIL_KEY = "email";
    private static final String RUT_KEY = "rut";
    private static final String NOMBRE_KEY = "nombre";
    EditText edt_email;
    EditText edt_nombre;
    EditText edt_rut;
    Button btn_guardar;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Verifica si usuario está logueado
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser usuarioActual = firebaseAuth.getCurrentUser();
        loginExitoso(usuarioActual);
    }

    private void loginExitoso(FirebaseUser usuarioActual) {
        if (usuarioActual != null) {
            Log.d(TAG, "Loguin exitoso");
        } else {
            Log.w(TAG, "Loguin no exitoso");
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_cliente);
        edt_email = findViewById(R.id.edt_correo);
        edt_nombre = findViewById(R.id.edt_nombre);
        edt_rut = findViewById(R.id.edt_rut);
        btn_guardar = findViewById(R.id.btn_guardar);
        btn_guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarCliente();
            }
        });

    }

    private void guardarCliente(){
        String textEmail = edt_email.getText().toString().trim();
        String textRut = edt_rut.getText().toString().trim();
        String textNombre = edt_nombre.getText().toString().trim();

        if (textEmail.isEmpty() || textRut.isEmpty() || textNombre.isEmpty()) {
            Snackbar.make(edt_nombre, "Complete todos los datos", Snackbar.LENGTH_LONG).show();
        } else {
            muestraProgressDialog();
            Map<String, Object> dataToSave = new HashMap<>();
            dataToSave.put(EMAIL_KEY, textEmail);
            dataToSave.put(RUT_KEY, textRut);
            dataToSave.put(NOMBRE_KEY, textNombre);
            db.collection("clientes").document().set(dataToSave).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        ocultarProgressDialog();
                        Snackbar.make(edt_nombre, "Se guardaron los datos", Snackbar.LENGTH_LONG).show();
                        Intent intent = new Intent(NuevoClienteActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }else{
                        Log.w(TAG, "Documento No Exitoso: ");
                        ocultarProgressDialog();
                        Snackbar.make(edt_nombre, "No se guardaron los datos", Snackbar.LENGTH_LONG).show();
                    }
                }
            });

        }
    }
}

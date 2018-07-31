package cl.hriquelme.trenerfirebase.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import cl.hriquelme.trenerfirebase.R;

public class DetalleCliente extends AppCompatActivity {

    private static final String TAG = "DetalleCliente";
    TextView txtNombre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_cliente);
        txtNombre = findViewById(R.id.tvombreDetalle);
        Bundle bundle = getIntent().getExtras();
        if (bundle !=null){
            txtNombre.setText(bundle.getString("idCliente"));
        } else {
            Log.w(TAG, "Error");
        }
    }
}

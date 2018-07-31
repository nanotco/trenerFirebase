package cl.hriquelme.trenerfirebase.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import cl.hriquelme.trenerfirebase.BaseActivity;
import cl.hriquelme.trenerfirebase.LoginActivity;
import cl.hriquelme.trenerfirebase.R;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private FirebaseAuth firebaseAuth;
    ListView listView;
    private List<String> listaNombres = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.listview);
        muestraProgressDialog();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Capturar instancia (datos de login, sesión) de Firebase
        firebaseAuth = FirebaseAuth.getInstance();

        // Acción al cliquear Botón flotante
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NuevoClienteActivity.class);
                startActivity(intent);
            }
        });

        // Intent al hacer click sobre Listview
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                Intent intent = new Intent(view.getContext(), DetalleCliente.class);
                String idCliente = listView.getItemAtPosition(i).toString();
                intent.putExtra("idCliente", idCliente);
                Log.d(TAG, "idCliente: " +idCliente);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Carga Listview desde Firebase
        db.collection("clientes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
           listaNombres.clear();

           for (DocumentSnapshot snapshot : documentSnapshots){
               listaNombres.add(snapshot.getString("nombre"));
           }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                        android.R.layout.simple_selectable_list_item, listaNombres);
                adapter.notifyDataSetChanged();
                listView.setAdapter(adapter);
                ocultarProgressDialog();
                Log.d(TAG, "listaNombres: "+ listaNombres);
            }
        });
    }


    // Verifica si usuario está logueado
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser usuarioActual = firebaseAuth.getCurrentUser();
        loginExitoso(usuarioActual);
    }

    // Mostrar datos de sesión en menu nav_header_main
    private void loginExitoso(FirebaseUser usuarioActual) {
        if (usuarioActual != null) {
            NavigationView navigationView = findViewById(R.id.nav_view);
            View view = navigationView.getHeaderView(0);
            TextView getNombre = view.findViewById(R.id.tvNombre);
            TextView getEmail = view.findViewById(R.id.tvEmail);
            ImageView getFoto = view.findViewById(R.id.ivPerfil);
            Picasso.with(MainActivity.this).load(usuarioActual.getPhotoUrl()).into(getFoto);
            getNombre.setText(usuarioActual.getDisplayName());
            getEmail.setText(usuarioActual.getEmail());
            Log.d(TAG, "Login exitoso: " + usuarioActual.getDisplayName());
        }else{
            Log.w(TAG, "Error al iniciar sesión" );
        }
    }

    // Cierra actividad anterior
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // Menú de Toolbar que se inflará
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    //Acciones desde menú de toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //Menú para Cerrar Cesión y rediregir a login activity
        if (id == R.id.action_settings) {
            cerrarSesion();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();        }
        return super.onOptionsItemSelected(item);
    }

    // Firebase cerrar sesión
    private void cerrarSesion() {
        firebaseAuth.signOut();
    }

    //Acciones de activity_maindrawer según menú presionado
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_camera) {

        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_1) {

        } else if (id == R.id.nav_2) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

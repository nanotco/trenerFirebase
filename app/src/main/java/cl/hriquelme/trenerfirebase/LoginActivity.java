package cl.hriquelme.trenerfirebase;

import android.content.Intent;

import android.support.annotation.NonNull;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
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
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import cl.hriquelme.trenerfirebase.view.MainActivity;

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "LoginGoogleActivity";
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private EditText etEmail;
    private EditText etClave;
    private Button btnLogin;
    private SignInButton btnGoogle;
    private LoginButton btnFacebook;
    private FirebaseAuth firebaseAuth;
    private CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //Código hash en consola para registrar app en developers.facebook.com
        /*try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "cl.hriquelme.trenerfirebase",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }*/

        // Capturar instancia (datos de login, sesión) de Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        mCallbackManager = CallbackManager.Factory.create();
        etEmail = findViewById(R.id.et_email);
        etClave = findViewById(R.id.et_clave);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnLogin = findViewById(R.id.btnLogin);
        btnFacebook = findViewById(R.id.btnFacebook);

        // "Escucha" las acciones de los botones
        btnGoogle.setOnClickListener(this);
        btnFacebook.setReadPermissions("email", "public_profile");
        btnLogin.setOnClickListener(this);

        //Configuracion login Facebook
        btnFacebook.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                FacebokToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                loginExitoso(null);
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                loginExitoso(null);
            }
        });

        // Configuración inicio sesión Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    // Verifica si usuario está logueado
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser usuarioActual = firebaseAuth.getCurrentUser();
        loginExitoso(usuarioActual);
    }

    // Acciones al presionar los botones
    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btnGoogle) {
            loginGoogle();
        } else if (i == R.id.btnLogin) {
            cerrarSesion();
        }
    }

    // Inicio con facebook
    private void FacebokToken(AccessToken token) {
        Log.d(TAG, "FacebokToken:" + token);
        muestraProgressDialog();

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success");
                    FirebaseUser usuario = firebaseAuth.getCurrentUser();
                    loginExitoso(usuario);
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.getException());
                    Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    loginExitoso(null);
                }
                ocultarProgressDialog();
            }
        });
    }

    //Resultado de intent de Google o Facebook. Datos de sesión de Firebase
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Resultado del Intent desde GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google login exitoso, autenticación con Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google login error
                Log.w(TAG, "Error de login con Google", e);
                loginExitoso(null);
            }
        } else {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    //Login de Firebase con Google
    private void firebaseAuthWithGoogle(GoogleSignInAccount cuentaGoogle) {
        Log.d(TAG, "firebaseAuthConGoogle:" + cuentaGoogle.getId());
        muestraProgressDialog();

        AuthCredential credential = GoogleAuthProvider.getCredential(cuentaGoogle.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Login exitoso, se muestran datos extraídos desde Google
                    Log.d(TAG, "loginConCredencial:exitoso");
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    loginExitoso(user);
                } else {
                    // Error, se muestra mensaje
                    Log.w(TAG, "loginConCredencial:error", task.getException());
                    loginExitoso(null);
                }
                ocultarProgressDialog();
            }
        });
    }

    // Intent que incia login con Google
    private void loginGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void cerrarSesion() {
        // Firebase cerrar sesión
        firebaseAuth.signOut();

        // Google cerrar sessión
        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                loginExitoso(null);
            }
        });
    }

    // Redireccionamiento a Main Activity en caso de Login exitoso
    public void loginExitoso(FirebaseUser usuario) {
        ocultarProgressDialog();
        if (usuario != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Log.w(TAG, "Error al iniciar sesión");
        }
    }
}


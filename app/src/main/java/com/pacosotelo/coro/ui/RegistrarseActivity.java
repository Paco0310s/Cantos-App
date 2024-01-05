package com.pacosotelo.coro.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pacosotelo.coro.R;
import com.pacosotelo.coro.modelos.Usuario;

import java.io.ByteArrayOutputStream;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegistrarseActivity extends AppCompatActivity {
    private TextInputEditText etNombre, etUsuario, etContra, etTelefono;
    private FirebaseAuth auth;
    private CircleImageView civFoto;
    private Usuario usuario;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrarse);

        auth = FirebaseAuth.getInstance();

        civFoto = findViewById(R.id.civPhotoUser);
        Button bRegistrarse = findViewById(R.id.bRegistrarse);
        etNombre = findViewById(R.id.etNombre);
        etUsuario = findViewById(R.id.etCorreo);
        etContra = findViewById(R.id.etPass);
        etTelefono = findViewById(R.id.etTelefono);
        usuario = new Usuario();

        bRegistrarse.setOnClickListener(v -> registrarse());
        findViewById(R.id.ibAtras).setOnClickListener(v -> onBackPressed());
        civFoto.setOnClickListener(v -> {
            final PopupMenu popupMenu = new PopupMenu(RegistrarseActivity.this, civFoto);
            popupMenu.getMenuInflater().inflate(R.menu.menu_foto, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.id.agregar_foto:
                        if(ActivityCompat.checkSelfPermission(RegistrarseActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                                == PackageManager.PERMISSION_GRANTED){
                            abrirGaleria();
                        }else{
                            ActivityCompat.requestPermissions(RegistrarseActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                        }
                        return true;
                    case R.id.tomar_foto:
                        if(ActivityCompat.checkSelfPermission(RegistrarseActivity.this, Manifest.permission.CAMERA)
                                == PackageManager.PERMISSION_GRANTED){
                            abrirCamara();
                        }else{
                            ActivityCompat.requestPermissions(RegistrarseActivity.this, new String[]{Manifest.permission.CAMERA}, 2);
                        }
                        return true;
                    default:
                        return false;
                }
            });
            popupMenu.show();
        });
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(RegistrarseActivity.this, LoginActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.right_in,R.anim.right_out);
        finish();
    }

    public void registrarse() {
        String nombre = Objects.requireNonNull(etNombre.getText()).toString();
        String email = Objects.requireNonNull(etUsuario.getText()).toString();
        String pass = Objects.requireNonNull(etContra.getText()).toString();
        String numero = Objects.requireNonNull(etTelefono.getText()).toString();

        if(TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Introduce tu correo electrónico",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Introduce tu contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        if(pass.length() < 8) {
            Toast.makeText(this, "Minimo 8 carácteres", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
            if(!task.isSuccessful()) {
                Toast.makeText(RegistrarseActivity.this, "Error al registrarse: " +
                        task.getException(), Toast.LENGTH_SHORT).show();
            } else {
                DatabaseReference dr = FirebaseDatabase.getInstance().getReference("usuarios");
                FirebaseUser currentUser = auth.getCurrentUser();

                if (currentUser != null) {
                    usuario.setUid(currentUser.getUid());
                    usuario.setNombre(nombre);
                    usuario.setEmail(email);
                    usuario.setNumber_phone(numero);

                    dr.child(currentUser.getUid()).setValue(usuario);

                    Toast.makeText(RegistrarseActivity.this, "Se ha creado el " +
                            "usuario, bienvenido", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(RegistrarseActivity.this,
                            MenuLateralActivity.class);
                    i.putExtra("usuario", usuario);
                    startActivity(i);
                    overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                    finish();
                } else {
                    Toast.makeText(RegistrarseActivity.this, "Ocurrió un error, " +
                                    "Intentelo nuevamente",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 2){
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                abrirCamara();
            }else{
                Toast.makeText(this, "No se pudo acceder a la camara: permiso denegado", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == 1){
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                abrirGaleria();
            }else{
                Toast.makeText(this, "No se pudo acceder a la galeria: permiso denegado", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void subirImagen(Uri FileUri){
        String id = UUID.randomUUID().toString();

        StorageReference Folder = FirebaseStorage.getInstance().getReference().child("Fotos");

        final StorageReference file_name = Folder.child("IMG-USER-" + id);

        file_name.putFile(FileUri).addOnSuccessListener(taskSnapshot -> file_name.getDownloadUrl().addOnSuccessListener(uri -> {
            usuario.setFoto(String.valueOf(uri));
            Toast.makeText(this, "Se ha subido la imagen", Toast.LENGTH_SHORT).show();
        }));
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 2){
            if (resultCode == Activity.RESULT_OK){
                assert data != null;
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                civFoto.setImageBitmap(bitmap);
                subirImagen(getImageUri(this,bitmap));
            }
        }
        if (requestCode == 1){
            if (resultCode == Activity.RESULT_OK){
                assert data != null;
                Uri path = data.getData();
                civFoto.setImageURI(path);
                subirImagen(path);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void abrirCamara(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 2);
        }
    }

    @SuppressLint("IntentReset")
    private void abrirGaleria(){
        @SuppressLint("IntentReset") Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/");
        startActivityForResult(Intent.createChooser(intent,"Seleccione la Aplicación"), 1);
    }
}
package com.pacosotelo.coro.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.android.material.navigation.NavigationView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pacosotelo.coro.R;
import com.pacosotelo.coro.databinding.ActivityMenuLateralBinding;
import com.pacosotelo.coro.modelos.Usuario;
import com.pacosotelo.coro.tools.Constantes;
import com.pacosotelo.coro.tools.MenuHelper;
import com.squareup.picasso.Picasso;
import java.io.ByteArrayOutputStream;
import de.hdodenhof.circleimageview.CircleImageView;
import androidx.lifecycle.ViewModelProvider;
import com.pacosotelo.coro.viewmodel.GruposViewModel;

public class MenuLateralActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private CircleImageView civFoto;
    private Usuario usuario;
    private GruposViewModel gruposViewModel;
    private NavController navController;
    private NavigationView navigationView;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this); // Uncomment when androidx.edge:edge is available

        ActivityMenuLateralBinding binding = ActivityMenuLateralBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMenuLateral.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_listaCantos, R.id.nav_listaTodosCantos, R.id.nav_listaGrupos, R.id.nav_listaEsquemas, R.id.nav_afinador)
                .setOpenableLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_menu_lateral);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Listener manual para forzar la selección del menu lateral al presionar
        navigationView.setNavigationItemSelectedListener(item -> {
            boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
            if (handled) {
                drawer.closeDrawers();
            }
            return handled;
        });

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int currentId = destination.getId();
            navigationView.setCheckedItem(currentId);
        });


        View header = navigationView.getHeaderView(0);
        header.findViewById(R.id.header_title);
        TextView tvNameUser = header.findViewById(R.id.tvUser);
        TextView tvEmailUser = header.findViewById(R.id.tvEmailUser);
        civFoto = header.findViewById(R.id.civPhotoUser);

        gruposViewModel = new ViewModelProvider(this).get(GruposViewModel.class);

        gruposViewModel.getPhotoUploadSuccess().observe(this, photoUrl -> {
            if (photoUrl != null && !photoUrl.isEmpty()) {
                Picasso.get().load(photoUrl).into(civFoto);
                Toast.makeText(this, "Se ha subido la imagen", Toast.LENGTH_SHORT).show();
            }
        });

        gruposViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            usuario = this.getIntent().getSerializableExtra("usuario", Usuario.class);
        } else {
            usuario = (Usuario) this.getIntent().getSerializableExtra("usuario");
        }

        if(usuario != null) {
            Constantes.usuario = usuario;

            tvNameUser.setText(usuario.getNombre());
            tvEmailUser.setText(usuario.getEmail());

            String foto = usuario.getFoto();
            if(!foto.isEmpty()) Picasso.get().load(foto).into(civFoto);
        } else {
            usuario = new Usuario();
        }

    }

    private void toggleTheme() {
        MenuHelper.toggleTheme(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_toolbar, menu);
        MenuItem themeItem = menu.findItem(R.id.action_toggle_theme);
        SharedPreferences pref = getSharedPreferences("theme_pref", MODE_PRIVATE);
        boolean isDark = pref.getBoolean("is_dark_mode", false);
        if (themeItem != null) {
            themeItem.setIcon(isDark ? R.drawable.ic_light_mode : R.drawable.ic_night_mode);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_toggle_theme) {
            toggleTheme();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_menu_lateral);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
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

    private void foto() {
        final PopupMenu popupMenu = new PopupMenu(new ContextThemeWrapper(MenuLateralActivity.this, R.style.CustomPopupMenuStyle), civFoto);
        popupMenu.getMenuInflater().inflate(R.menu.menu_foto, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.agregar_foto:
                    if(ActivityCompat.checkSelfPermission(MenuLateralActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED){
                        abrirGaleria();
                    }else{
                        ActivityCompat.requestPermissions(MenuLateralActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    }
                    return true;
                case R.id.tomar_foto:
                    if(ActivityCompat.checkSelfPermission(MenuLateralActivity.this, Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED){
                        abrirCamara();
                    }else{
                        ActivityCompat.requestPermissions(MenuLateralActivity.this, new String[]{Manifest.permission.CAMERA}, 2);
                    }
                    return true;
                default:
                    return false;
            }
        });
        popupMenu.show();
    }

    private void subirImagen(Uri FileUri){
        if (usuario != null && usuario.getUid() != null) {
            gruposViewModel.uploadUserPhoto(usuario, FileUri);
        }
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
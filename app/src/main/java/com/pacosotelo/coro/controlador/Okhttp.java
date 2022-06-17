package com.pacosotelo.coro.controlador;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.pacosotelo.coro.vista.Lista;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class Okhttp {

    public Okhttp() {

    }

    public static void get(Context c, String ruta) {
        OkHttpClient cliente = new OkHttpClient();

        Request request = new Request.Builder().url(ruta).build();

        cliente.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                if(response.isSuccessful()) {
                    ResponseBody responseBody = response.body();

                    if (responseBody != null) {

                        try {
                            JSONArray json = new JSONArray(responseBody.string());

                            for (int i = 0; i < json.length(); i++) {
                                JSONObject jsonObj = json.getJSONObject(i);

                                Integer id = jsonObj.getInt("ID");
                                String nombre = jsonObj.getString("NOMBRE");
                                String letra = jsonObj.getString("LETRA");
                                String categoria = jsonObj.getString("CATEGORIAS");
                                SQLite.insertar(id, nombre, letra, categoria, c);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        //this.runOnUiThread(() -> {});

                    }
                }
            }
        });

        //return json;
    }

    public void postCanto(String nombre, String letra, String categorias) {
        String URL = "https://pacosd.000webhostapp.com/bd_cantos/guardar.php";
        OkHttpClient cliente = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json;charset=utf-8");
        JSONObject actualData = new JSONObject();
        try {
            actualData.put("nombre", nombre);
            actualData.put("letra", letra);
            actualData.put("categorias", categorias);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(JSON, actualData.toString());
        Log.d("BODY", body.toString());

        Request newReq = new Request.Builder().url(URL).post(body).build();
        Log.d("REQ", newReq.toString());

        try {
            Response response = cliente.newCall(newReq).execute();
            Log.d("RESP", String.valueOf(response));
            Log.d("RESP_BODY", response.body().string());
            //Nuevo.this.runOnUiThread(() -> {
            //    Toast.makeText(Nuevo.this, respuesta, Toast.LENGTH_SHORT).show();
            //});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

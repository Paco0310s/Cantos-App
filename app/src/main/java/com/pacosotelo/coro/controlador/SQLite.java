package com.pacosotelo.coro.controlador;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLite extends SQLiteOpenHelper {

    public static final String OBTENER_TODO =
            "SELECT * FROM CANTOS ORDER BY NOMBRE ASC;";

    public SQLite(Context contexto) {
        super(contexto, "CORO",null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase bd) {
        bd.execSQL(
            "CREATE TABLE CANTOS (" +
            "ID INTEGER PRIMARY KEY, " +
            "NOMBRE VARCHAR(50), " +
            "LETRA TEXT, " +
            "CATEGORIAS VARCHAR(100)" +
            ");"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase bd, int versionActual, int versionNueva) {
        bd.execSQL("DROP TABLE IF EXISTS CANTOS;");
    }

    public static boolean insertar(Integer id, String nombre, String letra, String categorias, Context c) {
        SQLite admin = new SQLite(c);
        SQLiteDatabase base = admin.getWritableDatabase();

        long retult = -1;

        if(!existe(c,id)) {
            ContentValues registro = new ContentValues();
            registro.put("ID", id);
            registro.put("NOMBRE", nombre);
            registro.put("LETRA", letra);
            registro.put("CATEGORIAS", categorias);
            retult = base.insert("CANTOS", null, registro);
        }

        base.close();

        return retult > 0;
    }

    public static boolean existe(Context c, Integer id) {
        SQLite admin = new SQLite(c);
        SQLiteDatabase bs = admin.getWritableDatabase();
        @SuppressLint("Recycle")
        Cursor fila = bs.rawQuery("SELECT ID FROM CANTOS WHERE ID = " + id, null);

        return fila.moveToFirst();
    }

    public static boolean modificar(String id, String nombre, String letra, String categorias, Context c) {
        SQLite admin = new SQLite(c);
        SQLiteDatabase base = admin.getWritableDatabase();

        ContentValues registro = new ContentValues();
        registro.put("NOMBRE",nombre);
        registro.put("LETRA",letra);
        registro.put("CATEGORIAS",categorias);

        int retult = base.update("CANTOS", registro, "ID=?", new String[]{id});

        base.close();

        return retult > 0;
    }

    public static boolean eliminar(String id, Context c) {
        SQLite admin = new SQLite(c);
        SQLiteDatabase base = admin.getWritableDatabase();

        int result = base.delete("CANTOS", "ID=?", new String[]{id});

        base.close();

        return result > 0;
    }
}

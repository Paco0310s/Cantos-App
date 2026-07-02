package com.pacosotelo.coro.modelos;

import java.io.Serializable;
import java.util.ArrayList;

public class Esquema implements Serializable {
    private String id;
    private String nombre;
    private ArrayList<Canto> cantos;
    private String fecha_creacion;
    private String fecha_modificacion;
    private String app;
    private String grupo_id;

    public Esquema() {
        cantos = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public ArrayList<Canto> getCantos() {
        return cantos;
    }

    public void setCantos(ArrayList<Canto> cantos) {
        this.cantos = cantos;
    }

    public String getFecha_creacion() {
        return fecha_creacion;
    }

    public void setFecha_creacion(String fecha_creacion) {
        this.fecha_creacion = fecha_creacion;
    }

    public String getFecha_modificacion() {
        return fecha_modificacion;
    }

    public void setFecha_modificacion(String fecha_modificacion) {
        this.fecha_modificacion = fecha_modificacion;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getGrupo_id() {
        return grupo_id;
    }

    public void setGrupo_id(String grupo_id) {
        this.grupo_id = grupo_id;
    }
}

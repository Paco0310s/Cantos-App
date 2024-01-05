package com.pacosotelo.coro.modelos;

import java.io.Serializable;
import java.util.ArrayList;

public class Esquema implements Serializable {
    private String id;
    private String nombre;
    private ArrayList<Canto> cantos;
    private String fecha;

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

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
}

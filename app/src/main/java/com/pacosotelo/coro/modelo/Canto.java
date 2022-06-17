package com.pacosotelo.coro.modelo;

import java.util.ArrayList;

public class Canto {
    private String id;
    private String nombre;
    private String letra;
    private ArrayList<String> momentos, tiempos;

    public Canto() {
        momentos = new ArrayList<>();
        tiempos = new ArrayList<>();
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

    public String getLetra() {
        return letra;
    }

    public void setLetra(String letra) {
        this.letra = letra;
    }

    public ArrayList<String> getMomentos() {
        return momentos;
    }

    public void setMomentos(ArrayList<String> momentos) {
        this.momentos = momentos;
    }

    public ArrayList<String> getTiempos() {
        return tiempos;
    }

    public void setTiempos(ArrayList<String> tiempos) {
        this.tiempos = tiempos;
    }

    @Override
    public String toString() {
        return nombre;
    }
}

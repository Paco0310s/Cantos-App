package com.pacosotelo.coro.modelos;

import java.util.ArrayList;

public class Canto {
    private String id;
    private String nombre;
    private String letra;
    private ArrayList<String> momentos, tiempos;
    private String id_usuario;
    private String id_grupo;
    private String visibilidad;

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

    public String getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(String id_usuario) {
        this.id_usuario = id_usuario;
    }

    public String getId_grupo() {
        return id_grupo;
    }

    public void setId_grupo(String id_grupo) {
        this.id_grupo = id_grupo;
    }

    public String getVisibilidad() {
        return visibilidad;
    }

    public void setVisibilidad(String visibilidad) {
        this.visibilidad = visibilidad;
    }

    @Override
    public String toString() {
        return nombre;
    }
}

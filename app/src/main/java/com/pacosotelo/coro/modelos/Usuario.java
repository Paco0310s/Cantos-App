package com.pacosotelo.coro.modelos;

import java.io.Serializable;
import java.util.ArrayList;

public class Usuario implements Serializable {
    String nombre;
    String email;
    String foto;
    String uid;
    String number_phone;
    String fecha_creacion;
    ArrayList<String> grupos;
    String grupoActual;

    public Usuario() {
        this.nombre = "";
        this.email = "";
        this.foto = "";
        this.uid = "";
        this.number_phone = "";
        this.fecha_creacion = "";
        this.grupos = new ArrayList<>();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNumber_phone() {
        return number_phone;
    }

    public void setNumber_phone(String number_phone) {
        this.number_phone = number_phone;
    }

    public String getFecha_creacion() {
        return fecha_creacion;
    }

    public void setFecha_creacion(String fecha_creacion) {
        this.fecha_creacion = fecha_creacion;
    }

    public ArrayList<String> getGrupos() {
        return grupos;
    }

    public void setGrupos(ArrayList<String> grupos) {
        this.grupos = grupos;
    }

    public String getGrupoActual() {
        return grupoActual;
    }

    public void setGrupoActual(String grupoActual) {
        this.grupoActual = grupoActual;
    }
}

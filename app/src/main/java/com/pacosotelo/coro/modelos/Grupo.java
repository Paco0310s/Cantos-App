package com.pacosotelo.coro.modelos;

import java.io.Serializable;
import java.util.ArrayList;

public class Grupo implements Serializable {
    private String uuid;
    private String nombre;
    private String codigo;

    public Grupo() {}

    public Grupo(String uuid, String nombre, String codigo) {
        this.uuid = uuid;
        this.nombre = nombre;
        this.codigo = codigo;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }


}

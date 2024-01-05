package com.pacosotelo.coro.modelos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Canto implements Serializable {
    private String id;
    private String nombre;
    private String letra;
    private String autor;
    private String tono;
    private ArrayList<String> momentos, tiempos;
    private String id_grupo;
    private String fecha_creacion;
    private String fecha_modificacion;
    private String fecha_eliminacion;
    private String creado_por;
    private String modificado_por;
    private String eliminado_por;
    private boolean publico;
    private boolean visible;

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

    public String getId_grupo() {
        return id_grupo;
    }

    public void setId_grupo(String id_grupo) {
        this.id_grupo = id_grupo;
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

    public String getCreado_por() {
        return creado_por;
    }

    public void setCreado_por(String creado_por) {
        this.creado_por = creado_por;
    }

    public String getModificado_por() {
        return modificado_por;
    }

    public void setModificado_por(String modificado_por) {
        this.modificado_por = modificado_por;
    }

    public String getEliminado_por() {
        return eliminado_por;
    }

    public void setEliminado_por(String eliminado_por) {
        this.eliminado_por = eliminado_por;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getFecha_eliminacion() {
        return fecha_eliminacion;
    }

    public void setFecha_eliminacion(String fecha_eliminacion) {
        this.fecha_eliminacion = fecha_eliminacion;
    }

    public boolean isPublico() {
        return publico;
    }

    public void setPublico(boolean publico) {
        this.publico = publico;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getTono() {
        return tono;
    }

    public void setTono(String tono) {
        this.tono = tono;
    }

    @Override
    public String toString() {
        return nombre;
    }
}

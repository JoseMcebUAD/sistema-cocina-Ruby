package com.Model;
/**
 * Representa la entidad 'cliente' de la base de datos.
 * Esta clase se utiliza para almacenar y transportar los datos
 * de un  cliente (generico[sin informacion]...) a traves de todas las capas del sistema
 */
public class ModeloCliente {
    private int idCliente;
    private String nombreCliente;
    private String direcciones;
    private String telefono;
    private double tarifaDomicilio;

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getDirecciones() {
        return direcciones;
    }

    public void setDirecciones(String direcciones) {
        this.direcciones = direcciones;
    }
    
    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public double getTarifaDomicilio() {
        return tarifaDomicilio;
    }

    public void setTarifaDomicilio(double tarifaDomicilio) {
        this.tarifaDomicilio = tarifaDomicilio;
    }
}

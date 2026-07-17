package com.cocinarubi.presentation.dto.response;

public class RegistroClienteResponseDTO {

    private int idRegistroCliente;
    private String nombre;
    private String telefono;
    private Integer idRuta;
    private String nombreRuta;
    private String direccion;

    public RegistroClienteResponseDTO() {}

    public RegistroClienteResponseDTO(int idRegistroCliente, String nombre, String telefono,
                                      Integer idRuta, String nombreRuta, String direccion) {
        this.idRegistroCliente = idRegistroCliente;
        this.nombre = nombre;
        this.telefono = telefono;
        this.idRuta = idRuta;
        this.nombreRuta = nombreRuta;
        this.direccion = direccion;
    }

    public int getIdRegistroCliente() { return idRegistroCliente; }
    public void setIdRegistroCliente(int idRegistroCliente) { this.idRegistroCliente = idRegistroCliente; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public Integer getIdRuta() { return idRuta; }
    public void setIdRuta(Integer idRuta) { this.idRuta = idRuta; }

    public String getNombreRuta() { return nombreRuta; }
    public void setNombreRuta(String nombreRuta) { this.nombreRuta = nombreRuta; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
}

package com.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.DAO.Daos.ClienteDAO;
import com.Model.ModeloCliente;

public class ClientService {
    private ClienteDAO clienteDAO = new ClienteDAO();
    
    public ModeloCliente addClient(ModeloCliente cliente){
        try {
        List<ModeloCliente> existingClients = clienteDAO.all();
        boolean isClientDuplicate = existingClients.stream().anyMatch(c -> 
            c.getNombreCliente().equalsIgnoreCase(cliente.getNombreCliente()) || 
            c.getTelefono().equals(cliente.getTelefono())
        );
        if (isClientDuplicate) {
            System.out.println("Validación: El cliente ya existe (Nombre o Teléfono duplicado).");
            return null;
        }
        return clienteDAO.create(cliente);
    } catch (SQLException e) {
        System.err.println("Error al guardar cliente: " + e.getMessage());
        return null;
    }
    }

    public boolean updateClient(ModeloCliente cliente) {
        try {
            clienteDAO.update(cliente.getIdCliente(), cliente);
            return true;
        } catch (SQLException e) {
            System.err.println("Error al editar: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteClient(ModeloCliente cliente) {
        try {
        int id = cliente.getIdCliente();
        if (id == 0) {
            List<ModeloCliente> todos = clienteDAO.all();
            for (ModeloCliente c : todos) {
                if (c.getTelefono().equals(cliente.getTelefono())) {
                    id = c.getIdCliente();
                    break;
                }
            }
        }
            if (id != 0) {
                clienteDAO.delete(id);
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error al eliminar: " + e.getMessage());
            return false;
        }
    }

    public List<ModeloCliente> getAllClients() {
        try {
            return clienteDAO.all();
        } catch (SQLException e) {
            System.err.println("Error at getAllClients: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public ModeloCliente findOrRegister(ModeloCliente posibleCliente) {
        try {
            List<ModeloCliente> clientes = clienteDAO.all();
            
            // 1. Buscar si ya existe (Prioridad al Teléfono)
            for (ModeloCliente c : clientes) {
                // Verificamos si coincide el teléfono (ignorando vacíos)
                if (!c.getTelefono().equals("0") && c.getTelefono().equals(posibleCliente.getTelefono())) {
                    return c;
                }
                // Opcional: Verificamos por nombre si el teléfono no coincidió
                if (c.getNombreCliente().equalsIgnoreCase(posibleCliente.getNombreCliente())) {
                    return c;
                }
            }
            System.out.println("Cliente nuevo detectado en orden. Registrando...");
            return clienteDAO.create(posibleCliente);

        } catch (SQLException e) {
            System.err.println("Error en findOrRegister: " + e.getMessage());
            return null;
        }
    }

}

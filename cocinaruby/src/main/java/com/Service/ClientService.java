package com.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.DAO.Daos.ClienteDAO;
import com.Model.ModeloCliente;

public class ClientService {
    private ClienteDAO clienteDAO = new ClienteDAO();
    
    /**
     * Agrega un nuevo cliente a la base de datos.
     * Valida que el cliente no sea duplicado por nombre o teléfono.
     */
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
            System.err.println("Error al actualizar cliente: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteClient(ModeloCliente cliente) {
        try {
        int id = cliente.getIdCliente();
        if (id == 0) {
            List<ModeloCliente> allClients = clienteDAO.all();
            for (ModeloCliente c : allClients) {
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
            System.err.println("Error al eliminar cliente: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene todos los clientes de la base de datos.
     * @return Lista de clientes, o lista vacía si hay error
     */
    public List<ModeloCliente> getAllClients() {
        try {
            return clienteDAO.all();
        } catch (SQLException e) {
            System.err.println("Error obteniendo todos los clientes: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Busca un cliente existente o lo registra si es nuevo.
     * Prioriza búsqueda por teléfono.
     * @param possibleClient Cliente a buscar o registrar
     * @return Cliente encontrado o nuevo cliente registrado
     */
    public ModeloCliente findOrRegister(ModeloCliente possibleClient) {
        try {
            List<ModeloCliente> allClients = clienteDAO.all();
            
            // 1. Buscar si ya existe (Prioridad al Teléfono)
            for (ModeloCliente c : allClients) {
                // Verificar si coincide el teléfono (ignorando vacíos)
                if (!c.getTelefono().equals("0") && c.getTelefono().equals(possibleClient.getTelefono())) {
                    return c;
                }
                // Opcional: Verificar por nombre si el teléfono no coincidió
                if (c.getNombreCliente().equalsIgnoreCase(possibleClient.getNombreCliente())) {
                    return c;
                }
            }
            System.out.println("Cliente nuevo detectado en orden. Registrando...");
            return clienteDAO.create(possibleClient);

        } catch (SQLException e) {
            System.err.println("Error en findOrRegister: " + e.getMessage());
            return null;
        }
    }

}

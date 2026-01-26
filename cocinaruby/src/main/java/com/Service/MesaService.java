package com.Service;

import com.Config.CConexion;
import com.DAO.Daos.MesaDAO;
import com.Model.ModeloMesa;
import com.Model.Enum.EstadoMesaEnum;

import java.sql.SQLException;
import java.util.List;

/**
 * Servicio para manejar la lógica de negocio de mesas.
 * Gestiona CRUD de mesas y transiciones de estado.
 */
public class MesaService {

    private final MesaDAO mesaDAO;

    public MesaService() {
        this.mesaDAO = new MesaDAO();
    }

    public MesaService(CConexion conector) {
        this.mesaDAO = new MesaDAO(conector);
    }

    /**
     * Crea una nueva mesa con estado DISPONIBLE por defecto.
     *
     * @return ModeloMesa creado con ID asignado
     * @throws SQLException si hay error en la operación
     */
    public ModeloMesa addMesa() throws SQLException {
        ModeloMesa mesa = new ModeloMesa();
        mesa.setEstadoMesa(EstadoMesaEnum.DISPONIBLE);
        return mesaDAO.create(mesa);
    }

    /**
     * Actualiza el estado de una mesa.
     *
     * @param mesa ModeloMesa con los datos actualizados
     * @return true si se actualizó correctamente
     * @throws SQLException si hay error en la operación
     */
    public boolean updateMesa(ModeloMesa mesa) throws SQLException {
        if (!EstadoMesaEnum.esValido(mesa.getEstadoMesa())) {
            throw new SQLException("Estado de mesa no válido: " + mesa.getEstadoMesa());
        }
        return mesaDAO.update(mesa.getIdMesa(), mesa);
    }

    /**
     * Elimina una mesa. Valida que no tenga órdenes asociadas.
     *
     * @param idMesa ID de la mesa a eliminar
     * @return true si se eliminó correctamente
     * @throws SQLException si hay error o tiene órdenes asociadas
     */
    public boolean deleteMesa(int idMesa) throws SQLException {
        if (mesaDAO.tieneOrdenesAsociadas(idMesa)) {
            throw new SQLException("No se puede eliminar la mesa porque tiene órdenes asociadas (históricas o activas)");
        }
        return mesaDAO.delete(idMesa);
    }

    /**
     * Obtiene todas las mesas ordenadas por ID.
     *
     * @return Lista de todas las mesas
     * @throws SQLException si hay error en la consulta
     */
    public List<ModeloMesa> getAllMesas() throws SQLException {
        return mesaDAO.all();
    }

    /**
     * Obtiene solo las mesas disponibles.
     *
     * @return Lista de mesas con estado DISPONIBLE
     * @throws SQLException si hay error en la consulta
     */
    public List<ModeloMesa> getMesasDisponibles() throws SQLException {
        return mesaDAO.findDisponibles();
    }

    /**
     * Obtiene mesas filtradas por estado.
     *
     * @param estado Estado a filtrar (DISPONIBLE, OCUPADO, SUSPENDIDO)
     * @return Lista de mesas con el estado especificado
     * @throws SQLException si hay error en la consulta o estado inválido
     */
    public List<ModeloMesa> getMesasPorEstado(String estado) throws SQLException {
        if (!EstadoMesaEnum.esValido(estado)) {
            throw new SQLException("Estado de mesa no válido: " + estado);
        }
        return mesaDAO.findByEstado(estado);
    }

    /**
     * Cambia el estado de una mesa.
     *
     * @param idMesa ID de la mesa
     * @param nuevoEstado Nuevo estado (DISPONIBLE, OCUPADO, SUSPENDIDO)
     * @return true si se cambió correctamente
     * @throws SQLException si hay error o estado inválido
     */
    public boolean cambiarEstado(int idMesa, String nuevoEstado) throws SQLException {
        if (!EstadoMesaEnum.esValido(nuevoEstado)) {
            throw new SQLException("Estado de mesa no válido: " + nuevoEstado);
        }
        return mesaDAO.cambiarEstado(idMesa, nuevoEstado);
    }

    /**
     * Marca una mesa como OCUPADO cuando se crea una orden.
     *
     * @param idMesa ID de la mesa
     * @return true si se marcó correctamente
     * @throws SQLException si hay error en la operación
     */
    public boolean marcarComoOcupada(int idMesa) throws SQLException {
        return mesaDAO.cambiarEstado(idMesa, EstadoMesaEnum.OCUPADO);
    }

    /**
     * Marca una mesa como DISPONIBLE cuando se factura una orden.
     *
     * @param idMesa ID de la mesa
     * @return true si se marcó correctamente
     * @throws SQLException si hay error en la operación
     */
    public boolean marcarComoDisponible(int idMesa) throws SQLException {
        return mesaDAO.cambiarEstado(idMesa, EstadoMesaEnum.DISPONIBLE);
    }

    /**
     * Valida que una mesa esté disponible antes de crear una orden.
     *
     * @param idMesa ID de la mesa a validar
     * @return true si la mesa está DISPONIBLE
     * @throws SQLException si hay error en la consulta
     */
    public boolean validarMesaDisponible(int idMesa) throws SQLException {
        ModeloMesa mesa = mesaDAO.read(idMesa);
        if (mesa == null) {
            return false;
        }
        return mesa.getEstadoMesa().equals(EstadoMesaEnum.DISPONIBLE);
    }

    /**
     * Obtiene el siguiente número de mesa para mostrar en UI.
     * Formato: "Mesa X" donde X es (count + 1)
     *
     * @return String con formato "Mesa X"
     * @throws SQLException si hay error en la consulta
     */
    public String getSiguienteNumeroMesa() throws SQLException {
        int count = mesaDAO.contarMesas();
        return "Mesa " + (count + 1);
    }

    /**
     * Obtiene una mesa por ID.
     *
     * @param idMesa ID de la mesa
     * @return ModeloMesa o null si no existe
     * @throws SQLException si hay error en la consulta
     */
    public ModeloMesa getMesa(int idMesa) throws SQLException {
        return mesaDAO.read(idMesa);
    }
}

package com.DAO.Factory;

import com.DAO.Daos.Orden.OrdenDomicilioDAO;
import com.DAO.Daos.Orden.OrdenMesaDAO;
import com.DAO.Daos.Orden.OrdenMostradorDAO;
import com.DAO.Interfaces.ICrud;
import com.Model.ModeloOrden;
import com.Model.Enum.TiposClienteEnum;
import com.Model.Orden.ModeloOrdenDomicilio;
import com.Model.Orden.ModeloOrdenMesa;
import com.Model.Orden.ModeloOrdenMostrador;

import net.bytebuddy.matcher.StringMatcher.Mode;

/**
 * Factory para crear instancias de DAOs especializados de órdenes.
 */
public class OrdenDAOFactory {

    /**
     * Crea un DAO especializado según el tipo de cliente.
     *
     * @param tipo Tipo de cliente: "Domicilio", "Mesa", "Mostrador"
     */
    public static <T extends ModeloOrden> ICrud<T> crearDAO(Class<T> tipo) {

        if (tipo == ModeloOrdenDomicilio.class)
            return (ICrud<T>) new OrdenDomicilioDAO();

        if (tipo == ModeloOrdenMesa.class)
            return (ICrud<T>) new OrdenMesaDAO();

        if (tipo == ModeloOrdenMostrador.class)
            return (ICrud<T>) new OrdenMostradorDAO();

        throw new IllegalArgumentException("Tipo no soportado");
    }
}

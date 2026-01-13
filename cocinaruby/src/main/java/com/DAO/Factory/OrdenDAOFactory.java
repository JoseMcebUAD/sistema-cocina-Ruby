package com.DAO.Factory;

import com.DAO.Daos.Orden.OrdenDomicilioDAO;
import com.DAO.Daos.Orden.OrdenMesaDAO;
import com.DAO.Daos.Orden.OrdenMostradorDAO;
import com.DAO.Interfaces.ICrud;
import com.Model.Enum.TiposClienteEnum;

/**
 * Factory para crear instancias de DAOs especializados de órdenes.
 */
public class OrdenDAOFactory {

    /**
     * Crea un DAO especializado según el tipo de cliente.
     *
     * @param tipoCliente Tipo de cliente: "Domicilio", "Mesa", "Mostrador"
     */
    public static ICrud<?> crearDAO(String tipoCliente) {
        if (tipoCliente == null || tipoCliente.isEmpty()) {
            throw new IllegalArgumentException("El tipo de cliente no puede ser null o vacío");
        }

        switch (tipoCliente) {
            case TiposClienteEnum.PAGO_DOMICILIO:
                return new OrdenDomicilioDAO();

            case TiposClienteEnum.PAGO_MESA:
                return new OrdenMesaDAO();

            case TiposClienteEnum.PAGO_MOSTRADOR:
                return new OrdenMostradorDAO();

            default:
                throw new IllegalArgumentException("Tipo de cliente no válido: " + tipoCliente +
                        ". Usa: " + TiposClienteEnum.PAGO_DOMICILIO + ", " +
                        TiposClienteEnum.PAGO_MESA + ", o " +
                        TiposClienteEnum.PAGO_MOSTRADOR);
        }
    }

    /**
     * Crea un DAO de domicilio.
     *
     * @return OrdenDomicilioDAO
     */
    public static OrdenDomicilioDAO crearDomicilioDAO() {
        return new OrdenDomicilioDAO();
    }

    /**
     * Crea un DAO de mesa.
     *
     * @return OrdenMesaDAO
     */
    public static OrdenMesaDAO crearMesaDAO() {
        return new OrdenMesaDAO();
    }

    /**
     * Crea un DAO de mostrador.
     *
     * @return OrdenMostradorDAO
     */
    public static OrdenMostradorDAO crearMostradorDAO() {
        return new OrdenMostradorDAO();
    }
}

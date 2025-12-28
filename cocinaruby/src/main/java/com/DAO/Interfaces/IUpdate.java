package com.DAO.Interfaces;

import java.sql.SQLException;
/**
 * Cuarto principio de SOLID para implementar las interfazes del CRUD
 * interfaz para el update de una tupla
 */
public interface IUpdate<T> {

    /**
     * Actualiza una tupla de la bd
     * @param id llave primaria de la tupla
     * @param model modelo a actualizar
     * @return
     * @throws SQLException
     */
    boolean update(int id, T model) throws SQLException;

}

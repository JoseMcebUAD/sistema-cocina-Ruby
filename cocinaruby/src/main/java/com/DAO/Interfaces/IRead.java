package com.DAO.Interfaces;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
/**
 * Cuarto principio de SOLID para implementar las interfazes del CRUD
 * interfaz leer tuplas de la base de datos
 */
public interface IRead<T> {
    /**
     * lee una tupla de la base de datos
     * @param id llave primaria de la tupla
     * @throws SQLException
     */
    T find(int id) throws SQLException;
    /**
     * regresa todos los elementos de una base de datos
     * @return
     * @throws SQLException
     */
    List<T> all() throws SQLException;

}

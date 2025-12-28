package com.DAO.Interfaces;

import java.sql.SQLException;
/**
 * Cuarto principio de SOLID para implementar las interfazes del CRUD
 * interfaz para el Create de una tupla
 */
public interface ICreate<T> {
    /**
     * crea una tupla a la base de datos
     * @param model modelo de la entidad de la base
     * @return El modelo con el ID insertado
     * @throws SQLException
     */
    T create(T model) throws SQLException;


}

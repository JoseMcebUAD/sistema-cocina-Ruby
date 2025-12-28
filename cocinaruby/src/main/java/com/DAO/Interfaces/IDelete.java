package com.DAO.Interfaces;

import java.sql.SQLException;
/**
 * Cuarto principio de SOLID para implementar las interfazes del CRUD
 * interfaz para el DELETE de una tupla
 */
public interface IDelete {
    /**
     * elimina una tupla de la base de datos
     * @param id llave primaria de la tupla
     * @return si el cliente se eliminó o no
     * @throws SQLException
     */
    boolean delete(int id) throws SQLException;
    // /**
    //  * elimina varias tuplas de la base de datos
    //  * @param ids ids de las tuplas
    //  * @return cantidad de elementos eliminados 
    //  * @throws SQLException
    //  */
    // default int deleteMany(List<Integer> ids) throws SQLException {
    //     int count = 0;
    //     for (int id : ids) {
    //         if (delete(id)) {
    //             count++;
    //         }
    //     }
    //     return count;
    // }

    
}

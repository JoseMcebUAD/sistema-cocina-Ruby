package com.DAO.Interfaces;
/**
 * Cuarto principio de SOLID para implementar las interfazes del CRUD
 * interfaz para un DAO con CRUD
 */
public interface ICrud<T> extends ICreate<T>, IRead<T>, IUpdate<T>, IDelete {
}

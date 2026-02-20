package com.recruitx.hrone.services;

import java.util.List;

public interface InterfaceCRUD<T> {

    // Create
    void create(T t);

    // Read all
    List<T> getAll();

    // Read by ID
    T getById(int id);

    // Update
    void update(T t);

    // Delete by ID
    void delete(int id);
}

package com.example.totpsender.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface Repository<T, ID> {

    T save(T entity);

    Optional<T> findById(ID id);

    List<T> findAll();

    void deleteById(ID id);

    boolean existsById(ID id);
}

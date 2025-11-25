package pe.edu.upeu.farmafx.service;

import java.util.List;
import java.util.Optional;

public interface ICrudGenericService<T, ID> {

    T save(T entity);

    T update(ID id, T entity);

    List<T> findAll();

    Optional<T> findById(ID id);

    void delete(T entity);

    void deleteById(ID id);
}

package pe.edu.upeu.farmafx.service.impl;

import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.farmafx.exception.ModelNotFoundException;
import pe.edu.upeu.farmafx.repository.ICrudGenericRepository;
import pe.edu.upeu.farmafx.service.ICrudGenericService;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public abstract class CrudGenericServiceImpl<T, ID> implements ICrudGenericService<T, ID> {

    protected abstract ICrudGenericRepository<T, ID> getRepository();

    @Override
    @Transactional
    public T save(T entity) {
        return getRepository().save(entity);
    }

    @Override
    @Transactional
    public T update(ID id, T entity) {
        getRepository().findById(id)
                .orElseThrow(() -> new ModelNotFoundException("ID NOT FOUND: " + id));
        return getRepository().save(entity);
    }

    @Override
    public List<T> findAll() {
        return getRepository().findAll();
    }

    @Override
    public Optional<T> findById(ID id) {
        return getRepository().findById(id);
    }

    @Override
    @Transactional
    public void delete(T entity) {
        getRepository().delete(entity);
    }

    @Override
    @Transactional
    public void deleteById(ID id) {
        if (!getRepository().existsById(id)) {
            throw new ModelNotFoundException("ID NOT FOUND: " + id);
        }
        getRepository().deleteById(id);
    }
}

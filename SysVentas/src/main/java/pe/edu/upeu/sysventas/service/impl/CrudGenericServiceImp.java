package pe.edu.upeu.sysventas.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.upeu.sysventas.exception.ModelNotFoundException;
import pe.edu.upeu.sysventas.repository.ICrudGenericRepository;
import pe.edu.upeu.sysventas.service.ICrudGenericService;

import java.util.List;

@RequiredArgsConstructor
@Service
public abstract class CrudGenericServiceImp<T,ID> implements ICrudGenericService<T,ID> {
    protected abstract ICrudGenericRepository<T,ID> getRepo();

    @Override
    public T save(T t) {
        return getRepo().save(t);
    }

    @Override
    public T update(ID id, T t) {
        getRepo().findById(id).orElseThrow(()->new ModelNotFoundException("ID NOT FOUND"+id));
        return getRepo().save(t);
    }

    @Override
    public List<T> findAll() {
        return getRepo().findAll();
    }

    @Override
    public T findById(ID id) {
        return getRepo().findById(id).orElseThrow(()->new ModelNotFoundException("ID NOT FOUND"+id));
    }

    @Override
    public void delete(T t) {
        getRepo().delete(t);
    }

    @Override
    public void deleteById(ID id) {
        if(!getRepo().existsById(id)){
            throw new ModelNotFoundException("ID NOT FOUND"+id);
        }
        getRepo().deleteById(id);
    }
}

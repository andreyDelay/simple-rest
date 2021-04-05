package repository.hibernate;

import model.File;

import java.util.List;

public interface Repository <T, ID>{
    T save(T object);

    T update(T object);

    void delete(ID id);

    T find(ID id);

    List<T> findAll();

}

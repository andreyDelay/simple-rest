package service;

import java.util.List;

public interface Service<T> {
    T post(T entity);

    T put(T entity);

    void delete(Long id);

    T get(Long id);

    List<T> getAll();

    String getJson(List<T> list);
}

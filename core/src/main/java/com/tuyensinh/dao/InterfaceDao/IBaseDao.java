package com.tuyensinh.dao.InterfaceDao;

import java.util.List;

public interface IBaseDao<T> {

    T findById(Integer id);

    T save(T entity);

    void update(T entity);

    void delete(T entity);

    void deleteById(Integer id);

    List<T> findAll();

    List<T> findByPage(int page, int pageSize);

    long count();
}

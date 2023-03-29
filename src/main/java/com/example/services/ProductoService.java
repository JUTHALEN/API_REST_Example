package com.example.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.example.entities.Producto;

public interface ProductoService{
    public List<Producto> findAll(Sort sort);
    public Page<Producto> findAll(Pageable pageable);
    public Producto findById(long Id);
    public Producto save(Producto producto);
    public void delete(Producto producto);
}

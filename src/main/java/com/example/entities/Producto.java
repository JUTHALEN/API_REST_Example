package com.example.entities;

import java.io.Serializable;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Producto implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotEmpty(message = "El nombre no puede estar vacío")
    @Size(min = 4, max = 25, message="El nombre tiene que tener entre 4 y 25 caracteres.")
    private String nombre;

    @Size(max=255, message = "La descripción no puede superar los 255 carácteres.")
    private String descripcion;

    @Min(value = 0, message = "El precio no puede ser negativo")
    private double precio;

    @Min(value = 0, message = "El stock no puede ser negativo")
    private long stock;
    
    //Relacionamos las tablas:
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private Presentacion presentacion;

}

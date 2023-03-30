package com.example.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.entities.Producto;
import com.example.services.ProductoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/productos")
public class ProductoController {
    
    //Método para que nos devuelva el listado de productos con paginacion o no:
    @Autowired
    private ProductoService productoService;

    /**
     * El método siguiente va a responder a una petición (request) del tipo:
     * http://localhost:8080/productos?page=1&size=4, 
     * es decir tiene que ser capaz de de devolver un listado de productos de paginados, o no, 
     * pero en cualquier caso ordenados por un criterio (nombre, descripción, etc.)
     * 
     * La petición anterior implica @Requestparam (required = false) porq no esta obligado a recibirlo
     * 
     * /productos/3 -> PAthVariable
     */
    @GetMapping
    public ResponseEntity<List<Producto>> findAll(@RequestParam(name = "page", required = false) Integer page,
                                                  @RequestParam(name = "size", required = false) Integer size ){
        
        ResponseEntity<List<Producto>> responseEntity = null;
        List<Producto> productos = new ArrayList<>();
        Sort sortByNombre = Sort.by("nombre");
        
        if (page != null && size != null) {
            //Con paginación y ordenamineto:
            try {
                Pageable pageable = PageRequest.of(page, size, sortByNombre);
                Page<Producto> productosPaginados = productoService.findAll(pageable);
                productos = productosPaginados.getContent();
                responseEntity = new ResponseEntity<List<Producto>>(productos, HttpStatus.OK);

            } catch (Exception e) {
                responseEntity = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                e.getMessage();
            }
        } else {
            //Sin paginación, pero con ordenamiento
            try {
                productos = productoService.findAll(sortByNombre);
                responseEntity = new ResponseEntity<List<Producto>>(productos, HttpStatus.OK);

            } catch (Exception e) {
                responseEntity = new ResponseEntity<>(HttpStatus.NO_CONTENT);
                e.getMessage();
            }
        }

        return responseEntity;        
    }; 

    //Recupera el producto por el id. Va a responder a una peticion del tipo:
    //http://localhost:8080/productos/2

    @GetMapping(value="/{id}")
    public ResponseEntity<Map<String, Object>> findById(@PathVariable(name = "id") int id) {
        
        ResponseEntity<Map<String, Object>> responseEntity = null;
        //Creamos un mapa para personalizar el mensaje en un mapa:
        Map<String, Object> responseAsMap = new HashMap<>();

        try {
            Producto producto = productoService.findById(id);

            if (producto != null) {
                String successMessage = "Se ha encontrado el producto con id " +id;
                //Metemos el mensaje en el mapa:
                responseAsMap.put("mensaje", successMessage);
                responseAsMap.put("producto", producto);
                responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap, HttpStatus.OK);
            } else {
                String errorMessage = "No se ha encontrado el producto con id " +id;
                responseAsMap.put("mensaje", errorMessage);
                responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap, HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
           String errorGrave = "Error en el servidor";
           responseAsMap.put("mensaje", errorGrave);
           responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        return responseEntity;
    }

    /**
    * Persiste un producto en la base de datos:
    */
    @PostMapping
    @Transactional
    public ResponseEntity<Map<String, Object>> insert(@Valid @RequestBody Producto producto, BindingResult result){
        
        Map<String, Object> responseAsMap = new HashMap<>();
        ResponseEntity<Map<String, Object>> responseEntity = null;

        //Primero comprobamos si hay errores en el producto recibido:
        if(result.hasErrors()){ //Con hasError si da verdadero es que hay errores
            List<String> errorMessages = new ArrayList<>();

            for (ObjectError error : result.getAllErrors()) {
                errorMessages.add(error.getDefaultMessage()); //Muestra los mensajes añadidos en la creacion de las tablas
            }

            responseAsMap.put("errores", errorMessages);
            
            responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap, HttpStatus.BAD_REQUEST);
            return responseEntity;
        } 

        Producto productoDB = productoService.save(producto);

        try{
            if (productoDB != null) {
                String mensaje = "El producto se ha guardado correctamente: ";
                responseAsMap.put("mensaje", mensaje);
                responseAsMap.put("producto", productoDB);

                responseEntity = new ResponseEntity<Map<String,Object>>(responseAsMap, HttpStatus.CREATED);
                
            } else {
                //No se ha creado el producto
                
            }
        } catch (DataAccessException e){
            String errorGrave = "Se ha producido un fatal error" +
                 " la causa más probable es: " + e.getMostSpecificCause();
            responseAsMap.put("errorGrave", errorGrave);
            responseEntity = new ResponseEntity<Map<String,Object>>(responseAsMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        //Si no hay errores guardo el producto
        return responseEntity;
    }

    /**
     * Actualiza un producto en la BBDD
     */
    @PutMapping("/{id}") //Modificar
    @Transactional
    public ResponseEntity<Map<String,Object>> update (@Valid 
                @RequestBody Producto producto,
                BindingResult result,
                @PathVariable(name = "id") Integer id) { //En el cuerpo de la peticion va un objeto

        Map<String, Object> responseAsMap = new HashMap<>();
        ResponseEntity<Map<String, Object>> responseEntity = null;

    /**
    * Primero: Comprobar si hay errores en el producto recibido - VALIDACION
    */
    if (result.hasErrors()) {

        List<String> errorMessage = new ArrayList<>();

    for(ObjectError error : result.getAllErrors()) {

        errorMessage.add(error.getDefaultMessage()); //Muestras los mensajes de la Entity Producto
    }

    responseAsMap.put("errores", errorMessage);
    responseEntity = new ResponseEntity<Map<String,Object>>(responseAsMap, HttpStatus.BAD_REQUEST);
    return responseEntity;

    }

    // Si no hay errores, persistimos el producto
    //Vinculando previamente el id que se recibe con el producto

    producto.setId(id); //En el JSON, con el save se modifica ese elemento
    Producto productoDataBase = productoService.save(producto);

    try {
    if(productoDataBase != null) {

        String mensaje = "El producto se ha actualizado correctamente";
        responseAsMap.put("mensaje", mensaje);

        responseAsMap.put("producto", productoDataBase);

        responseEntity = new ResponseEntity<Map<String,Object>>(responseAsMap,HttpStatus.OK);

    } else {

        //No se ha actualizado el producto

        String mensaje2 = "El producto no se ha actualizado";
        responseAsMap.put("mensaje", mensaje2);
        responseEntity = new ResponseEntity<Map<String,Object>>(responseAsMap,HttpStatus.INTERNAL_SERVER_ERROR);

    } 
    
    } catch (DataAccessException e) {
        String errorGrave = "Ha tenido lugar un error grave" + "la causa puede ser "
                + e.getMostSpecificCause(); // especifica la causa especifica del error.

        responseAsMap.put("errorGrave", errorGrave);
        responseEntity = new ResponseEntity<Map<String,Object>>(responseAsMap, HttpStatus.INTERNAL_SERVER_ERROR);

    }
    //Si no hay errores se ejecuta este return, se persiste el producto
        return responseEntity;
    }

    //Creamos el método para borrar un producto
//     @DeleteMapping("/{id}")
//     @Transactional
//     public ResponseEntity<Map<String,Object>> delete(@Valid 
//                         @RequestBody Producto producto,
//                         BindingResult result) {

//     Map<String, Object> responseAsMap = new HashMap<>();
//     ResponseEntity<Map<String, Object>> responseEntity = null;

//     /**
//      * Primero: Comprobar si hay errores en el producto recibido - VALIDACION
//      */
//     if (result.hasErrors()) {

//         List<String> errorMessage = new ArrayList<>();

//         for(ObjectError error : result.getAllErrors()) {
//             errorMessage.add(error.getDefaultMessage()); 
//         }

//         responseAsMap.put("errores", errorMessage);
//         responseEntity = new ResponseEntity<Map<String,Object>>(responseAsMap, HttpStatus.BAD_REQUEST);
//         return responseEntity;
//     }

//     try {
//         productoService.delete(producto); 
//         String mensaje = "El producto se ha eliminado correctamente";
//         responseAsMap.put("mensaje", mensaje);
//         responseAsMap.put("producto", producto);
//         responseEntity = new ResponseEntity<Map<String,Object>>(responseAsMap, HttpStatus.OK);

//     } catch (DataAccessException e) {
//         String errorGrave = "Ha tenido lugar un error grave" + "la causa puede ser "
//                 + e.getMostSpecificCause(); 
//         responseAsMap.put("errorGrave", errorGrave);
//         responseEntity = new ResponseEntity<Map<String,Object>>(responseAsMap, HttpStatus.INTERNAL_SERVER_ERROR);
//     }
//     return responseEntity;
// }

//Método hecho por el profesor:
@DeleteMapping
@Transactional
public ResponseEntity<String> delete(@PathVariable(name = "id") Integer id){

    //Para borrar primero tenemos que recuperar el producto:
    ResponseEntity<String> responseEntity = null;

    try {
        Producto producto = productoService.findById(id);
        if (producto != null) {
            productoService.delete(producto);
            responseEntity = new ResponseEntity<String>("Ha sido borrado correctamente", HttpStatus.OK);

        } else {
            responseEntity = new ResponseEntity<String>("No existe el producto buscado", HttpStatus.NOT_FOUND);
        }
        
    } catch (DataAccessException e) {
        e.getLocalizedMessage();
        responseEntity = new ResponseEntity<String>("Error fatal",HttpStatus.INTERNAL_SERVER_ERROR);
    }


    return responseEntity;
}
    




    
    /**
     * Método de ejemplo para entender el formato JSON, no tiene que ver con el proyecto
     */
    // @GetMapping
    // public List<String> nombres() {
    //     List<String> nombres = Arrays.asList(
    //         "Salma", "Judith", "Elisabet"
    //     );
    //     return nombres;
    // }

}

package pe.edu.upeu.PharmaBackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.PharmaBackend.entity.Producto;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    /** La condición y el descuento se ejecutan juntos bajo el bloqueo de la base de datos. */
    @Modifying(flushAutomatically = true)
    @Query("update Producto p set p.stock = p.stock - :cantidad where p.idProducto = :id and p.estado = true and p.stock >= :cantidad")
    int descontarStock(@Param("id") Long id, @Param("cantidad") Integer cantidad);
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCaseAndIdProductoNot(String nombre, long id);

    List<Producto> findByCategoriaIdCategoria(Long categoriaId);
    boolean existsByCategoriaIdCategoria(Long categoriaId);


}

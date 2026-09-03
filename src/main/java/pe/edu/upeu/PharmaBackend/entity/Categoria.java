package pe.edu.upeu.PharmaBackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categorias")
@Getter
@Setter
public class Categoria {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long idCategoria;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(length = 200)
    private String  descripcion;

    @Column(nullable = false)
    private Boolean estado;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @PrePersist
    public void prePersist(){
        this.fechaCreacion = LocalDateTime.now();
        if (estado == null) {
            estado = true;
        }
    }
    @PreUpdate
    public void preUpdate(){
        this.fechaModificacion = LocalDateTime.now();
    }
}

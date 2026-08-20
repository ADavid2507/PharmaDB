package pe.edu.upeu.PharmaBackend.dto;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CategoriaResponseDTO {
    private Long id;
    private String nombre;
    private String  descripcion;
    private Boolean estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
}

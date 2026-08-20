package pe.edu.upeu.PharmaBackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CategoriaRequestDTO {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(
            min = 3,
            max = 30,
            message = "El nombre debe tener entre 3 y 30 caracteres"
    )
    private String nombre;
    @Size(
            max = 200,
            message = "La descripción no debe pasar los 200 caracteres"
    )
    private String descripcion;

    @NotNull(message = "El estado es obligatorio")
    private Boolean estado;
}

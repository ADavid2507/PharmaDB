package pe.edu.upeu.PharmaBackend.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProductoRequestDTO {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(
            min = 3,
            max = 200,
            message = "El nombre debe tener entre 3 y 200 caracteres"
    )
    private String nombre;

    @NotNull(message = "El precio es obligatorio")
    @PositiveOrZero(message = "El precio debe ser mayor o igual a 0")
    private BigDecimal precio;

    @NotNull(message = "El stock es obligatorio")
    @PositiveOrZero(message = "El stock debe ser mayor o igual a 0")
    private Integer stock;

    @NotNull(message = "La categoria es obligatoria")
    @Positive(message = "El id de la categoria debe ser mayor a 0")
    private Long categoriaId;
}

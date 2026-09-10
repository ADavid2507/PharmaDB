package pe.edu.upeu.PharmaBackend.dto.reporte;

import lombok.*;

import java.math.BigDecimal;

public record VentaPorCategoriaDTO(
        Long idCategoria,
        String categoriaNombre,
        Long cantidadVentas,
        BigDecimal subtotalVentas
){

}

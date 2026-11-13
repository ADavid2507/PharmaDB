package pe.edu.upeu.PharmaBackend.util;
import java.util.Set;
import org.springframework.data.domain.*;
import pe.edu.upeu.PharmaBackend.exception.ReglaNegocioException;
public final class PaginacionUtil {
 private PaginacionUtil() {}
 public static Pageable construir(int pagina, int tamanio, String ordenarPor, String direccion, Set<String> permitidos, String defecto) {
  if (pagina < 0 || tamanio < 1 || tamanio > 100) throw new ReglaNegocioException("La pagina debe ser >= 0 y el tamaño entre 1 y 100");
  String campo = ordenarPor == null || ordenarPor.isBlank() ? defecto : ordenarPor;
  if (!permitidos.contains(campo)) throw new ReglaNegocioException("Campo de orden no permitido: " + campo);
  if (!"asc".equalsIgnoreCase(direccion) && !"desc".equalsIgnoreCase(direccion)) throw new ReglaNegocioException("La direccion debe ser asc o desc");
  return PageRequest.of(pagina,tamanio,Sort.by(Sort.Direction.fromString(direccion),campo).and(Sort.by("id")));
 }
}

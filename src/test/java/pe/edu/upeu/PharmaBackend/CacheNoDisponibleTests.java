package pe.edu.upeu.PharmaBackend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pe.edu.upeu.PharmaBackend.service.service.CategoriaService;
import pe.edu.upeu.PharmaBackend.dto.CategoriaRequestDTO;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Un puerto local cerrado verifica la recuperación real, sin detener el Redis de desarrollo. */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:s13_sin_redis;MODE=Oracle;DB_CLOSE_DELAY=-1",
        "spring.cache.type=redis",
        "spring.data.redis.port=16379",
        "spring.data.redis.connect-timeout=500ms"
})
@ActiveProfiles("test")
class CacheNoDisponibleTests {
    @Autowired CategoriaService categorias;
    @Autowired WebApplicationContext context;

    @Test void lecturaYEscrituraContinuanConLaBaseSiRedisNoResponde() {
        var categoria = categorias.create(new CategoriaRequestDTO("Sin Redis", null, true));
        assertThat(categorias.readAll()).extracting("nombre").contains("Sin Redis");
        categorias.update(categoria.getId_categoria(), new CategoriaRequestDTO("Actualizada sin Redis", null, true));
        assertThat(categorias.read(categoria.getId_categoria()).getNombre()).isEqualTo("Actualizada sin Redis");
        categorias.delete(categoria.getId_categoria());
        assertThat(categorias.readAll()).isEmpty();
    }

    @Test void healthInformaDownSinExponerDetallesAlPublico() throws Exception {
        MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build()
                .perform(get("/actuator/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.components").doesNotExist());
    }
}

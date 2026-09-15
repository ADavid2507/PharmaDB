package pe.edu.upeu.PharmaBackend;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import pe.edu.upeu.PharmaBackend.config.CorsConfig;
import pe.edu.upeu.PharmaBackend.controller.ReporteController;
import pe.edu.upeu.PharmaBackend.controller.VentaController;
import pe.edu.upeu.PharmaBackend.exception.GlobalExceptionHandler;
import pe.edu.upeu.PharmaBackend.service.service.ReporteService;
import pe.edu.upeu.PharmaBackend.service.service.VentaService;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ConsultaWebTests {
    private AnnotationConfigWebApplicationContext context;
    private MockMvc mvc;

    @Configuration
    @EnableWebMvc
    static class WebConfig {
        @Bean VentaService ventas() { return mock(VentaService.class); }
        @Bean ReporteService reportes() { return mock(ReporteService.class); }
    }

    @BeforeEach
    void setup() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(WebConfig.class, CorsConfig.class, VentaController.class,
                ReporteController.class, GlobalExceptionHandler.class);
        context.refresh();
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @AfterEach void close() { context.close(); }

    @Test void allowedOriginCanReadReport() throws Exception {
        mvc.perform(get("/api/v1/reportes/ventas-por-categoria")
                .header("Origin", "http://localhost:4200"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
    }

    @Test void otherOriginIsRejected() throws Exception {
        mvc.perform(get("/api/v1/reportes/ventas-por-categoria")
                .header("Origin", "http://localhost:5500"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test void patchPreflightIsAllowed() throws Exception {
        mvc.perform(options("/api/v1/ventas/4/anular")
                .header("Origin", "http://localhost:4200")
                .header("Access-Control-Request-Method", "PATCH")
                .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Methods", "GET,POST,PUT,PATCH,DELETE,OPTIONS"));
    }

    @Test void malformedDateHasApplicationErrorContract() throws Exception {
        mvc.perform(get("/api/v1/ventas/buscar").param("desde", "01-09-2026"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/v1/ventas/buscar"))
                .andExpect(jsonPath("$.validationErrors.desde").exists())
                .andExpect(jsonPath("$.message").value("El parámetro 'desde' tiene un formato inválido. Use una fecha ISO yyyy-MM-dd"));
    }
}

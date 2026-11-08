package pe.edu.upeu.PharmaBackend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import pe.edu.upeu.PharmaBackend.config.RequestIdFilter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class RequestIdErrorTests {
    @Autowired WebApplicationContext context;
    @Autowired RequestIdFilter requestId;

    @Test void elErrorDeNegocioIncluyeElMismoCodigoDeLaCabecera() throws Exception {
        var mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(requestId).apply(springSecurity()).build();
        var result = mvc.perform(get("/api/v1/productos/999999999").with(user("admin")
                        .authorities(new SimpleGrantedAuthority("CATALOGO_LEER"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.requestId").isNotEmpty()).andReturn();
        String id = result.getResponse().getHeader("X-Request-Id");
        assertThat(id).matches("[0-9a-f]{8}");
        assertThat(result.getResponse().getContentAsString()).contains("\"requestId\":\"" + id + "\"");
        assertThat(org.slf4j.MDC.get("requestId")).isNull();
    }

    @Test void elErrorDelFiltroDeSeguridadTambienIncluyeElCodigo() throws Exception {
        var mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(requestId).apply(springSecurity()).build();
        var result = mvc.perform(get("/api/v1/productos/999999999"))
                .andExpect(status().isUnauthorized()).andReturn();
        String id = result.getResponse().getHeader("X-Request-Id");
        assertThat(result.getResponse().getContentAsString()).contains("\"requestId\":\"" + id + "\"");
    }
}

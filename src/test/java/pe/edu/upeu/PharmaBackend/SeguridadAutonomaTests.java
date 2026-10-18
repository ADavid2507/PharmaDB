package pe.edu.upeu.PharmaBackend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import pe.edu.upeu.PharmaBackend.entity.Empleado;
import pe.edu.upeu.PharmaBackend.entity.Rol;
import pe.edu.upeu.PharmaBackend.entity.Usuario;
import pe.edu.upeu.PharmaBackend.repository.EmpleadoRepository;
import pe.edu.upeu.PharmaBackend.repository.RolRepository;
import pe.edu.upeu.PharmaBackend.repository.UsuarioRepository;
import pe.edu.upeu.PharmaBackend.security.JwtService;
import pe.edu.upeu.PharmaBackend.security.UsuarioDetailsService;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Integra filtros JWT, controladores, validación, BCrypt, JPA y H2; no usa Oracle. */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:seguridad_autonoma;MODE=Oracle;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "spring.flyway.enabled=false",
        "app.jwt.secret=w9Z6dP/dzykNH/uiI3Z/KGjBlP0H4Ji+Tuq4fm2cTNuLD4jWafu1gBL74V2SJEvy"
})
@ActiveProfiles("test")
@Transactional
class SeguridadAutonomaTests {
    @Autowired WebApplicationContext context;
    @Autowired UsuarioRepository usuarios;
    @Autowired EmpleadoRepository empleados;
    @Autowired RolRepository roles;
    @Autowired PasswordEncoder encoder;
    @Autowired JwtService jwt;
    @Autowired UsuarioDetailsService details;

    private MockMvc mvc;
    private Usuario admin;
    private Usuario vendedor;
    private Rol rolAdmin;
    private Rol rolVendedor;
    private Rol rolAlmacen;
    private String tokenAdmin;
    private String tokenVendedor;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        rolAdmin = roles.save(new Rol(null, "ADMIN", "Administrador"));
        rolVendedor = roles.save(new Rol(null, "VENDEDOR", "Vendedor"));
        rolAlmacen = roles.save(new Rol(null, "ALMACENERO", "Almacenero"));
        admin = crear("admin", "11111111", "Admin2026*", rolAdmin);
        vendedor = crear("vendedor", "22222222", "Vendedor2026*", rolVendedor);
        tokenAdmin = jwt.generarToken(details.loadUserByUsername("admin"));
        tokenVendedor = jwt.generarToken(details.loadUserByUsername("vendedor"));
    }

    private Usuario crear(String username, String dni, String password, Rol rol) {
        Empleado empleado = new Empleado();
        empleado.setDni(dni);
        empleado.setNombres(username);
        empleado.setApellidos("Prueba");
        empleado.setCargo("Pruebas");
        empleado.setEmail(username + "@test.local");
        empleado.setEstado(true);
        empleados.save(empleado);
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(encoder.encode(password));
        usuario.setEstado(true);
        usuario.setEmpleado(empleado);
        usuario.setRoles(new HashSet<>(Set.of(rol)));
        return usuarios.saveAndFlush(usuario);
    }

    private MockHttpServletRequestBuilder json(
            MockHttpServletRequestBuilder request, String token, String body) {
        return request.header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private String password(String actual, String nueva) {
        return "{\"passwordActual\":\"" + actual + "\",\"passwordNueva\":\"" + nueva + "\"}";
    }

    private String rolesBody(Long... ids) {
        return "{\"rolIds\":" + java.util.Arrays.toString(ids) + "}";
    }

    private void login(String username, String password, int status) throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().is(status));
    }

    @Test void cambiaPasswordConBCryptYConservaElTokenAnterior() throws Exception {
        String hashAnterior = vendedor.getPassword();
        mvc.perform(json(put("/api/v1/auth/password"), tokenVendedor,
                        password("Vendedor2026*", "NuevaClave2026")))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        usuarios.flush();
        Usuario guardado = usuarios.findById(vendedor.getId()).orElseThrow();
        assertThat(guardado.getPassword()).isNotEqualTo(hashAnterior);
        assertThat(encoder.matches("NuevaClave2026", guardado.getPassword())).isTrue();
        assertThat(encoder.matches("Vendedor2026*", guardado.getPassword())).isFalse();
        assertThat(encoder.matches("Admin2026*", admin.getPassword())).isTrue();
        login("vendedor", "Vendedor2026*", 401);
        login("vendedor", "NuevaClave2026", 200);
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + tokenVendedor))
                .andExpect(status().isOk()).andExpect(jsonPath("$.username").value("vendedor"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test void passwordActualIncorrectaEs409() throws Exception {
        mvc.perform(json(put("/api/v1/auth/password"), tokenVendedor,
                        password("Incorrecta2026", "NuevaClave2026")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("La contraseña actual no es correcta"));
        assertThat(encoder.matches("Vendedor2026*", vendedor.getPassword())).isTrue();
    }

    @Test void passwordRepetidaEs409() throws Exception {
        mvc.perform(json(put("/api/v1/auth/password"), tokenVendedor,
                        password("Vendedor2026*", "Vendedor2026*")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("La nueva contraseña debe ser distinta de la actual"));
    }

    @Test void passwordNuevaDebeCumplirLongitudLetraYNumero() throws Exception {
        for (String nueva : new String[]{"", "abc123", "abcdefgh", "12345678", "a1".repeat(37)}) {
            mvc.perform(json(put("/api/v1/auth/password"), tokenVendedor,
                            password("Vendedor2026*", nueva)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.validationErrors.passwordNueva").exists());
        }
    }

    @Test void passwordActualEsObligatoria() throws Exception {
        mvc.perform(json(put("/api/v1/auth/password"), tokenVendedor,
                        "{\"passwordNueva\":\"NuevaClave2026\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.passwordActual").exists());
    }

    @Test void bcryptRechazaMasDe72BytesSinCambiarElHash() throws Exception {
        mvc.perform(json(put("/api/v1/auth/password"), tokenVendedor,
                        password("Vendedor2026*", "a1" + "ñ".repeat(36))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("La contraseña no puede superar 72 bytes"));
        assertThat(encoder.matches("Vendedor2026*", vendedor.getPassword())).isTrue();
    }

    @Test void passwordRequiereToken() throws Exception {
        mvc.perform(put("/api/v1/auth/password").contentType(MediaType.APPLICATION_JSON)
                        .content(password("Vendedor2026*", "NuevaClave2026")))
                .andExpect(status().isUnauthorized());
    }

    @Test void cualquierRolAutenticadoPuedeCambiarSuPassword() throws Exception {
        Usuario almacen = crear("almacen", "33333333", "Almacen2026*", rolAlmacen);
        String token = jwt.generarToken(details.loadUserByUsername("almacen"));
        mvc.perform(json(put("/api/v1/auth/password"), token,
                        password("Almacen2026*", "OtraClave2026")))
                .andExpect(status().isNoContent());
        assertThat(encoder.matches("OtraClave2026", almacen.getPassword())).isTrue();
    }

    @Test void rolesReemplazanElConjuntoYSeReflejanConElMismoToken() throws Exception {
        mvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer " + tokenVendedor))
                .andExpect(status().isForbidden());
        mvc.perform(json(put("/api/v1/usuarios/{id}/roles", vendedor.getId()), tokenAdmin,
                        rolesBody(rolAdmin.getId(), rolAlmacen.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles", containsInAnyOrder("ADMIN", "ALMACENERO")))
                .andExpect(jsonPath("$.permisos", org.hamcrest.Matchers.hasItem("USUARIOS_ADMINISTRAR")))
                .andExpect(jsonPath("$.password").doesNotExist());
        usuarios.flush();
        assertThat(usuarios.findById(vendedor.getId()).orElseThrow().getRoles())
                .extracting(Rol::getNombre).containsExactlyInAnyOrder("ADMIN", "ALMACENERO");
        mvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer " + tokenVendedor))
                .andExpect(status().isOk());
    }

    @Test void vendedorYAlmacenNoPuedenAdministrarRolesNiEstado() throws Exception {
        crear("almacen", "33333333", "Almacen2026*", rolAlmacen);
        for (String token : new String[]{tokenVendedor, jwt.generarToken(details.loadUserByUsername("almacen"))}) {
            mvc.perform(json(put("/api/v1/usuarios/{id}/roles", admin.getId()), token,
                            rolesBody(rolVendedor.getId())))
                    .andExpect(status().isForbidden());
            mvc.perform(json(patch("/api/v1/usuarios/{id}/estado", admin.getId()), token,
                            "{\"estado\":false}"))
                    .andExpect(status().isForbidden());
        }
    }

    @Test void rolesVaciosNulosOAusentesSon400() throws Exception {
        for (String body : new String[]{"{\"rolIds\":[]}", "{\"rolIds\":[null]}", "{}"}) {
            mvc.perform(json(put("/api/v1/usuarios/{id}/roles", vendedor.getId()), tokenAdmin, body))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test void rolInexistenteEs404SinCambiarRolesActuales() throws Exception {
        mvc.perform(json(put("/api/v1/usuarios/{id}/roles", vendedor.getId()), tokenAdmin,
                        rolesBody(rolAlmacen.getId(), Long.MAX_VALUE)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Roles no encontrados: [" + Long.MAX_VALUE + "]"));
        assertThat(vendedor.getRoles()).extracting(Rol::getNombre).containsExactly("VENDEDOR");
    }

    @Test void usuarioInexistenteEs404EnRolesYEstado() throws Exception {
        mvc.perform(json(put("/api/v1/usuarios/{id}/roles", Long.MAX_VALUE), tokenAdmin,
                        rolesBody(rolAlmacen.getId())))
                .andExpect(status().isNotFound());
        mvc.perform(json(patch("/api/v1/usuarios/{id}/estado", Long.MAX_VALUE), tokenAdmin,
                        "{\"estado\":false}"))
                .andExpect(status().isNotFound());
    }

    @Test void adminNoPuedeQuitarseAdmin() throws Exception {
        mvc.perform(json(put("/api/v1/usuarios/{id}/roles", admin.getId()), tokenAdmin,
                        rolesBody(rolAlmacen.getId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("No puede quitarse a sí mismo el rol ADMIN"));
        assertThat(admin.getRoles()).extracting(Rol::getNombre).containsExactly("ADMIN");
    }

    @Test void adminPuedeModificarSusRolesSiConservaAdmin() throws Exception {
        mvc.perform(json(put("/api/v1/usuarios/{id}/roles", admin.getId()), tokenAdmin,
                        rolesBody(rolAdmin.getId(), rolAlmacen.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles", containsInAnyOrder("ADMIN", "ALMACENERO")));
    }

    @Test void desactivarInvalidaElTokenExistenteYBloqueaLoginYReactivarRestauraAcceso() throws Exception {
        mvc.perform(json(patch("/api/v1/usuarios/{id}/estado", vendedor.getId()), tokenAdmin,
                        "{\"estado\":false}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value(false))
                .andExpect(jsonPath("$.password").doesNotExist());
        usuarios.flush();
        assertThat(usuarios.findById(vendedor.getId()).orElseThrow().getEstado()).isFalse();
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + tokenVendedor))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("El usuario está deshabilitado"));
        login("vendedor", "Vendedor2026*", 401);
        mvc.perform(json(patch("/api/v1/usuarios/{id}/estado", vendedor.getId()), tokenAdmin,
                        "{\"estado\":true}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value(true));
        login("vendedor", "Vendedor2026*", 200);
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + tokenVendedor))
                .andExpect(status().isOk());
    }

    @Test void adminNoPuedeDesactivarse() throws Exception {
        mvc.perform(json(patch("/api/v1/usuarios/{id}/estado", admin.getId()), tokenAdmin,
                        "{\"estado\":false}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("No puede desactivarse a sí mismo"));
        assertThat(admin.getEstado()).isTrue();
    }

    @Test void estadoNuloOAusenteEs400() throws Exception {
        for (String body : new String[]{"{\"estado\":null}", "{}"}) {
            mvc.perform(json(patch("/api/v1/usuarios/{id}/estado", vendedor.getId()), tokenAdmin, body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.validationErrors.estado").exists());
        }
    }

    @Test void administrarRolesYEstadoRequiereToken() throws Exception {
        mvc.perform(put("/api/v1/usuarios/{id}/roles", vendedor.getId())
                        .contentType(MediaType.APPLICATION_JSON).content(rolesBody(rolAdmin.getId())))
                .andExpect(status().isUnauthorized());
        mvc.perform(patch("/api/v1/usuarios/{id}/estado", vendedor.getId())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":false}"))
                .andExpect(status().isUnauthorized());
    }
}

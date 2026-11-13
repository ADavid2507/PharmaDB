package pe.edu.upeu.PharmaBackend;
import java.math.BigDecimal;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.upeu.PharmaBackend.entity.*;
import pe.edu.upeu.PharmaBackend.repository.*;
/** Solo en classpath de pruebas y perfil e2e. Nunca carga datos en Oracle. */
@Configuration @Profile("e2e")
public class E2EDatos {
 @Bean CommandLineRunner datosE2E(UsuarioRepository usuarios, EmpleadoRepository empleados, RolRepository roles,
 CategoriaRepository categorias, ClienteRepository clientes, ProductoRepository productos, PasswordEncoder encoder,
 org.springframework.core.env.Environment env) {
  return args -> {
   String url=env.getRequiredProperty("spring.datasource.url");
   if (!url.startsWith("jdbc:h2:mem:")) throw new IllegalStateException("E2E solo permite H2 en memoria");
   String[][] cuentas={{"admin","ADMIN","Admin2026*"},{"vendedor","VENDEDOR","Vendedor2026*"},{"almacen","ALMACENERO","Almacen2026*"}};
   int i=0;
   for (String[] cuenta:cuentas) {
    Rol rol=new Rol(); rol.setNombre(cuenta[1]); roles.save(rol);
    Empleado empleado=new Empleado(); empleado.setDni("9100000"+(++i)); empleado.setNombres(cuenta[0]); empleado.setApellidos("E2E"); empleado.setCargo("Pruebas"); empleado.setEmail(cuenta[0]+"@example.invalid"); empleados.save(empleado);
    Usuario u=new Usuario(); u.setUsername(cuenta[0]); u.setPassword(encoder.encode(cuenta[2])); u.setEmpleado(empleado); u.setRoles(Set.of(rol)); usuarios.save(u);
   }
   Categoria c=new Categoria(); c.setNombre("E2E catalogo"); categorias.save(c);
   Cliente cliente=new Cliente(); cliente.setDni("92000001"); cliente.setNombres("Ana"); cliente.setApellidos("Mamani"); cliente.setEmail("cliente@example.invalid"); clientes.save(cliente);
   for(int n=0;n<45;n++) {
    Producto p=new Producto(); p.setNombre(n==0 ? "Paracetamol 500 mg" : "Producto E2E " + String.format("%03d",n));
    p.setPrecio(new BigDecimal("12.50")); p.setStock(1000); p.setCategoria(c); productos.save(p);
   }
  };
 }
}

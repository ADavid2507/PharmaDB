package pe.edu.upeu.PharmaBackend;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import pe.edu.upeu.PharmaBackend.entity.Cliente;
import pe.edu.upeu.PharmaBackend.exception.ReglaNegocioException;
import pe.edu.upeu.PharmaBackend.mapper.ClienteMapper;
import pe.edu.upeu.PharmaBackend.repository.ClienteRepository;
import pe.edu.upeu.PharmaBackend.service.impl.ClienteServiceImpl;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClienteServiceTests {
    private final ClienteRepository repository = mock(ClienteRepository.class);
    private final ClienteServiceImpl service = new ClienteServiceImpl(repository, new ClienteMapper());

    @Test void returnsRequestedPageWithStableSorting() {
        Cliente cliente = new Cliente();
        cliente.setId(7L);
        when(repository.findAll(any(Pageable.class))).thenAnswer(invocation -> {
            Pageable pageable = invocation.getArgument(0);
            assertEquals(1, pageable.getPageNumber());
            assertEquals(5, pageable.getPageSize());
            assertTrue(pageable.getSort().getOrderFor("apellidos").isDescending());
            assertNotNull(pageable.getSort().getOrderFor("id"));
            return new PageImpl<>(List.of(cliente), pageable, 6);
        });
        var pagina = service.listar(1, 5, "apellidos", "desc");
        assertEquals(7L, pagina.contenido().getFirst().getId());
        assertEquals(6, pagina.totalElementos());
        assertEquals(2, pagina.totalPaginas());
        assertTrue(pagina.ultima());
    }

    @Test void rejectsInvalidPaginationAndSortBeforeQuerying() {
        assertThrows(ReglaNegocioException.class, () -> service.listar(-1, 10, "id", "asc"));
        assertThrows(ReglaNegocioException.class, () -> service.listar(0, 0, "id", "asc"));
        assertThrows(ReglaNegocioException.class, () -> service.listar(0, 10, "inexistente", "asc"));
        assertThrows(ReglaNegocioException.class, () -> service.listar(0, 10, "id", "invalida"));
        verifyNoInteractions(repository);
    }

    @Test void softDeletePreservesRecordAndRejectsSecondDelete() {
        Cliente cliente = new Cliente();
        cliente.setId(7L);
        cliente.setEstado(true);
        when(repository.findById(7L)).thenReturn(Optional.of(cliente));
        service.delete(7L);
        assertFalse(cliente.getEstado());
        verify(repository).save(cliente);
        verify(repository, never()).delete(any(Cliente.class));
        assertThrows(ReglaNegocioException.class, () -> service.delete(7L));
        verify(repository, times(1)).save(cliente);
    }
}

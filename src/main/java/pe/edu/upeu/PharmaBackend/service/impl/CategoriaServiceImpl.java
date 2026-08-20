package pe.edu.upeu.PharmaBackend.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.PharmaBackend.dto.CategoriaRequestDTO;
import pe.edu.upeu.PharmaBackend.dto.CategoriaResponseDTO;
import pe.edu.upeu.PharmaBackend.exception.RecursosNoEncontradosException;
import pe.edu.upeu.PharmaBackend.exception.ReglaNegocioException;
import pe.edu.upeu.PharmaBackend.entity.Categoria;
import pe.edu.upeu.PharmaBackend.repository.CategoriaRepository;
import pe.edu.upeu.PharmaBackend.service.service.CategoriaService;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaServiceImpl implements CategoriaService {
    private static final Logger LOG = LoggerFactory.getLogger(CategoriaServiceImpl.class);

    private final CategoriaRepository categoriaRepository;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }


    @Override
    @Transactional
    public CategoriaResponseDTO create(CategoriaRequestDTO request) {
        String nombre = request.getNombre().trim();
        if(categoriaRepository.existsByNombreIgnoreCase(nombre)){
            throw new ReglaNegocioException("Ya existe una categoría con el nombre: " + nombre);
        }
        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        categoria.setDescripcion(request.getDescripcion());
        categoria.setEstado(request.getEstado());

        Categoria catCreate = categoriaRepository.save(categoria);

        return convertirResponse(catCreate);
    }

    @Override
    @Transactional
    public CategoriaResponseDTO update(Long id, CategoriaRequestDTO request) {
        Categoria categoria = categoriaRepository.findById(id).orElseThrow(()->
                new RecursosNoEncontradosException(
                        "Categoría con id " + id + " no encontrada"
                )
        );
        String nombre = request.getNombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ReglaNegocioException("Ya existe una categoría con el nombre: " + nombre);
        }
        categoria.setNombre(nombre);
        categoria.setDescripcion(request.getDescripcion());
        categoria.setEstado(request.getEstado());

        Categoria catUpdate = categoriaRepository.save(categoria);

        return convertirResponse(catUpdate);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoriaResponseDTO> read(Long id) {
        return categoriaRepository.findById(id).map(this::convertirResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> readAll() {
        return categoriaRepository.findAll().stream().map(this::convertirResponse).toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Categoria categoria = categoriaRepository.findById(id).orElseThrow(()->
                new RecursosNoEncontradosException(
                        "Categoría con id " + id + " no encontrada"
                )
        );
        categoriaRepository.delete(categoria);
    }

    private CategoriaResponseDTO convertirResponse(Categoria categoria) {
        return new CategoriaResponseDTO(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getDescripcion(),
                categoria.getEstado(),
                categoria.getFechaCreacion(),
                categoria.getFechaModificacion()
        );
    }
}

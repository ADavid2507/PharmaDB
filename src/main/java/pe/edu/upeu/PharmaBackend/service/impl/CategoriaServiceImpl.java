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
import pe.edu.upeu.PharmaBackend.repository.CatergoriaRepository;
import pe.edu.upeu.PharmaBackend.service.service.CategoriaService;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaServiceImpl implements CategoriaService {
    private static final Logger LOG = LoggerFactory.getLogger(CategoriaServiceImpl.class);

    private final CatergoriaRepository catergoriaRepository;

    public CategoriaServiceImpl(CatergoriaRepository catergoriaRepository) {
        this.catergoriaRepository = catergoriaRepository;
    }


    @Override
    @Transactional
    public CategoriaResponseDTO create(CategoriaRequestDTO request) {
        String nombre = request.getNombre().trim();
        if(catergoriaRepository.existsByNombreIgnoreCase(nombre)){
            throw new ReglaNegocioException("Ya existe una categoría con el nombre: " + nombre);
        }
        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        categoria.setDescripcion(request.getDescripcion());
        categoria.setEstado(request.getEstado());

        Categoria catCreate = catergoriaRepository.save(categoria);

        return convertirResponse(catCreate);
    }

    @Override
    @Transactional
    public CategoriaResponseDTO update(Long id, CategoriaRequestDTO request) {
        Categoria categoria = catergoriaRepository.findById(id).orElseThrow(()->
                new RecursosNoEncontradosException(
                        "Categoría con id " + id + " no encontrada"
                )
        );
        String nombre = request.getNombre().trim();
        if (catergoriaRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ReglaNegocioException("Ya existe una categoría con el nombre: " + nombre);
        }
        categoria.setNombre(nombre);
        categoria.setDescripcion(request.getDescripcion());
        categoria.setEstado(request.getEstado());

        Categoria catUpdate = catergoriaRepository.save(categoria);

        return convertirResponse(catUpdate);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoriaResponseDTO> read(Long id) {
        return catergoriaRepository.findById(id).map(this::convertirResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> readAll() {
        return catergoriaRepository.findAll().stream().map(this::convertirResponse).toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Categoria categoria = catergoriaRepository.findById(id).orElseThrow(()->
                new RecursosNoEncontradosException(
                        "Categoría con id " + id + " no encontrada"
                )
        );
        catergoriaRepository.delete(categoria);
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

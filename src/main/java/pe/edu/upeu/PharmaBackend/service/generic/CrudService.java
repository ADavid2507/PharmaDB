package pe.edu.upeu.PharmaBackend.service.generic;

import java.util.List;

public interface CrudService<REQ,RES, ID> {
    RES create(REQ request);

    RES update(ID id, REQ request);

    RES read(ID id);

    List<RES> readAll();

    void delete(ID id);
}

package pe.edu.upeu.PharmaBackend.auditoria;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pe.edu.upeu.PharmaBackend.service.service.AuditoriaService;

@Component
public class AuditoriaListener {
    private static final Logger log = LoggerFactory.getLogger(AuditoriaListener.class);
    private final AuditoriaService auditoriaService;

    public AuditoriaListener(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alConfirmar(EventoAuditoria evento) {
        try {
            auditoriaService.registrar(evento);
        } catch (RuntimeException ex) {
            // La venta ya se confirmo; un fallo de auditoria no debe informar que fallo la venta.
            log.error("No se pudo auditar {} {}", evento.accion(), evento.entidadId(), ex);
        }
    }
}

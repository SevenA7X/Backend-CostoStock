package cl.duoc.costostock.core.repository;

import cl.duoc.costostock.core.model.LoteCoreEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoteCoreRepository extends JpaRepository<LoteCoreEntity, Long> {
    List<LoteCoreEntity> findByIdProveedor(Long idProveedor);
}
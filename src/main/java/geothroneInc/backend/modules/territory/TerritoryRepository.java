package geothroneInc.backend.modules.territory;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TerritoryRepository extends JpaRepository<Territory, String> {
    // Busca todas as terras (para desenhar o mapa global)
    List<Territory> findAll();
}
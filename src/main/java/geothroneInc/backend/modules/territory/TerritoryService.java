package geothroneInc.backend.modules.territory;

import geothroneInc.backend.modules.player.Player;
import geothroneInc.backend.modules.player.PlayerRepository;
import geothroneInc.backend.modules.territory.dto.RunRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class TerritoryService {

    @Autowired
    private TerritoryRepository territoryRepository;

    @Autowired
    private PlayerRepository playerRepository;

    // Tamanho do quadrado (~45 metros)
    private static final double GRID_SIZE = 0.0004;

    // Valor de pontuação por território
    private static final double TILE_VALUE = 10.0;

    @Transactional
    public void processRun(RunRequest data) {
        // 1. Localizar o Jogador Atual
        var player = playerRepository.findById(data.playerId())
                .orElseThrow(() -> new RuntimeException("Jogador não encontrado!"));

        // 2. Atualizar Odômetro Pessoal (KM acumulado)
        double currentKm = player.getKilometersWalked() == null ? 0.0 : player.getKilometersWalked();
        player.setKilometersWalked(currentKm + data.totalDistanceKm());

        // 3. Processar o Caminho Percorrido
        Set<String> uniqueGridsTouched = new HashSet<>();

        for (var coord : data.path()) {
            long latIndex = (long) Math.floor(coord.latitude() / GRID_SIZE);
            long lonIndex = (long) Math.floor(coord.longitude() / GRID_SIZE);
            String gridId = "LAT" + latIndex + "_LON" + lonIndex;

            // Processar cada quadrado apenas uma vez por corrida
            if (!uniqueGridsTouched.contains(gridId)) {
                uniqueGridsTouched.add(gridId);

                // Busca o território no banco ou cria um novo se nunca foi explorado
                Territory territory = territoryRepository.findById(gridId).orElse(new Territory());

                if (territory.getId() == null) {
                    territory.setId(gridId);
                    territory.setCenterLat((latIndex * GRID_SIZE) + (GRID_SIZE / 2));
                    territory.setCenterLon((lonIndex * GRID_SIZE) + (GRID_SIZE / 2));
                }

                // --- LÓGICA DE CONQUISTA INDIVIDUAL ---
                Player oldOwner = territory.getOwner();

                // Caso A: O território já é do próprio jogador? Ignora e segue.
                if (oldOwner != null && oldOwner.getId().equals(player.getId())) {
                    continue;
                }

                // Caso B: ATAQUE (O território pertence a outro jogador)
                if (oldOwner != null) {
                    double oldOwnerScore = oldOwner.getScore() == null ? 0.0 : oldOwner.getScore();
                    // Subtrai os pontos do antigo dono (mínimo zero)
                    oldOwner.setScore(Math.max(0, oldOwnerScore - TILE_VALUE));
                    playerRepository.save(oldOwner);
                }

                // Caso C: POSSE E PONTUAÇÃO
                // Define o novo dono, remove qualquer vínculo de guilda e aplica a cor do jogador
                territory.setOwner(player);
                territory.setColor(player.getPlayerColor());
                territoryRepository.save(territory);

                // Incrementa o score global (XP) do conquistador
                double myScore = player.getScore() == null ? 0.0 : player.getScore();
                player.setScore(myScore + TILE_VALUE);
            }
        }

        // Salva as alterações finais do jogador (Score e KM)
        playerRepository.save(player);
    }

    /**
     * Auditoria de Scores: Recalcula todos os pontos baseado no estado atual do mapa.
     * Útil para sincronizar o ranking individual após mudanças estruturais.
     */
    @Transactional
    public void auditScores() {
        // 1. Zera o score de todos os jogadores para o recálculo
        playerRepository.findAll().forEach(p -> {
            p.setScore(0.0);
            playerRepository.save(p);
        });

        // 2. Distribui pontos baseados na posse atual dos territórios
        territoryRepository.findAll().forEach(t -> {
            if (t.getOwner() != null) {
                Player p = t.getOwner();
                p.setScore((p.getScore() == null ? 0.0 : p.getScore()) + TILE_VALUE);
                playerRepository.save(p);
            }
        });
    }
}
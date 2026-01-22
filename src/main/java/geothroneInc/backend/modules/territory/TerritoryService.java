package geothroneInc.backend.modules.territory;

import geothroneInc.backend.modules.player.Player;
import geothroneInc.backend.modules.player.PlayerRepository;
import geothroneInc.backend.modules.territory.dto.RunRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importante para salvar tudo junto

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class TerritoryService {

    @Autowired
    private TerritoryRepository territoryRepository;

    @Autowired
    private PlayerRepository playerRepository;

    // Tamanho do quadrado (~45 metros)
    private static final double GRID_SIZE = 0.0004;

    // Quanto vale cada quadradinho conquistado? (Ex: 0.05 km de território)
    private static final double TILE_VALUE = 10.0;

    @Transactional // Garante que se der erro, desfaz tudo (rollback)
    public void processRun(RunRequest data) {
        // 1. Achar o Jogador Atual (O Conquistador)
        var player = playerRepository.findById(data.playerId())
                .orElseThrow(() -> new RuntimeException("Jogador não encontrado!"));

        // 2. Atualizar APENAS o Histórico Físico (Suor)
        // Isso aqui só sobe, nunca desce (é o odômetro do corpo)
        double currentKm = player.getKilometersWalked() == null ? 0.0 : player.getKilometersWalked();
        player.setKilometersWalked(currentKm + data.totalDistanceKm());

        // NOTA: Não aumentamos o 'score' aqui cegamente mais.
        // O Score agora depende de QUANTOS QUADRADOS ele conquistou no loop abaixo.

        // 3. Calcular Territórios Conquistados (Grid a Grid)
        Set<String> uniqueGridsTouched = new HashSet<>();

        for (var coord : data.path()) {
            long latIndex = (long) Math.floor(coord.latitude() / GRID_SIZE);
            long lonIndex = (long) Math.floor(coord.longitude() / GRID_SIZE);
            String gridId = "LAT" + latIndex + "_LON" + lonIndex;

            // Se ainda não processamos esse quadrado NESTA corrida
            if (!uniqueGridsTouched.contains(gridId)) {
                uniqueGridsTouched.add(gridId);

                // Busca o território (ou cria um novo vazio)
                Territory territory = territoryRepository.findById(gridId).orElse(new Territory());

                // Configura coordenadas se for novo
                if (territory.getId() == null) {
                    territory.setId(gridId);
                    territory.setCenterLat((latIndex * GRID_SIZE) + (GRID_SIZE / 2));
                    territory.setCenterLon((lonIndex * GRID_SIZE) + (GRID_SIZE / 2));
                }

                Player oldOwner = territory.getOwner();

                // LÓGICA DE CONQUISTA E ROUBO

                // Caso A: O território já é meu -> Não faz nada (só mantem)
                if (oldOwner != null && oldOwner.getId().equals(player.getId())) {
                    continue;
                }

                // Caso B: Território tem dono (INIMIGO) -> ROUBAR PONTOS
                if (oldOwner != null && !oldOwner.getId().equals(player.getId())) {
                    // Tira pontos do inimigo
                    double enemyScore = oldOwner.getScore() == null ? 0.0 : oldOwner.getScore();
                    // Evita pontuação negativa se quiser (Math.max)
                    oldOwner.setScore(Math.max(0, enemyScore - TILE_VALUE));
                    playerRepository.save(oldOwner); // Salva o prejuízo do inimigo
                }

                // Aplica a Conquista (Para Casos A e B, e Territórios Vazios)
                territory.setOwner(player);
                territoryRepository.save(territory);

                // Adiciona pontos ao Jogador Atual
                double myScore = player.getScore() == null ? 0.0 : player.getScore();
                player.setScore(myScore + TILE_VALUE);
            }
        }

        // Salva o jogador atual com o novo Score e novo Km Físico
        playerRepository.save(player);
    }


    // Método para corrigir a pontuação de todo mundo baseado nos territórios reais
    public void auditScores() {
        // 1. Zera o score de pontuação de todos (O Km físico mantém)
        List<Player> allPlayers = playerRepository.findAll();
        for (Player p : allPlayers) {
            p.setScore(0.0);
        }
        playerRepository.saveAll(allPlayers);

        // 2. Conta quantos territórios cada um tem
        List<Territory> allTerritories = territoryRepository.findAll();

        for (Territory t : allTerritories) {
            if (t.getOwner() != null) {
                Player owner = t.getOwner();
                // O objeto 'owner' aqui pode estar desatualizado, buscamos a referência atual
                // ou somamos direto na lista em memória se preferir performance.
                // Para simplificar e garantir consistência:

                // Vamos re-buscar o player para garantir
                var freshPlayer = playerRepository.findById(owner.getId()).orElse(null);
                if (freshPlayer != null) {
                    freshPlayer.setScore(freshPlayer.getScore() + TILE_VALUE);
                    playerRepository.save(freshPlayer);
                }
            }
        }
    }
}
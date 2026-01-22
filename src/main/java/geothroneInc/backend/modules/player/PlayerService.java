package geothroneInc.backend.modules.player;

import geothroneInc.backend.modules.player.dto.ProfileResponse;
import geothroneInc.backend.modules.player.dto.RegisterPlayer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlayerService {


    private final PlayerRepository repository;


    public PlayerService(PlayerRepository repository) {
        this.repository = repository;
    }


    public Player createPlayer(RegisterPlayer data) {

        if (repository.findByEmail(data.email()).isPresent()) {
            throw new RuntimeException("Este email já está em uso por outro player!");
        }

        if (repository.existsByPlayerColor(data.playerColor())) {
            throw new RuntimeException("Esta cor ja pertence a outro reino! Escolha outra cor.");
        }

        if (data.avatar() ==  null || data.avatar().isBlank()) {
            throw new RuntimeException("Voce precisa escolher um avatar!");
        }

        // 2. Mapear DTO para Entidade
        Player newPlayer = new Player();
        newPlayer.setName(data.name());
        newPlayer.setCity(data.city());
        newPlayer.setEmail(data.email());
        newPlayer.setPlayerColor(data.playerColor());
        newPlayer.setPassword(data.password());
        newPlayer.setAvatar(data.avatar());
        newPlayer.setPlayerColor(data.playerColor());

        // 3. Salvar no banco
        return repository.save(newPlayer);
    }


    public Player authenticate(String email, String password) {
        // 1. Busca o player pelo email
        var playerOptional = repository.findByEmail(email);

        // 2. Se não achar o email -> Erro (Usuário não existe)
        if (playerOptional.isEmpty()) {
            throw new RuntimeException("Email não encontrado no reino.");
        }

        Player player = playerOptional.get();

        // 3. Se achou, confere a senha
        // (Lembre-se: strings em Java se compara com .equals(), nunca com ==)
        if (!player.getPassword().equals(password)) {
            throw new RuntimeException("Senha incorreta!");
        }

        // 4. Se passou por tudo, retorna o Jogador (para o App saber quem logou)
        return player;
    }


    public List<ProfileResponse> getLeaderboard() {
        // Aqui sim podemos usar o 'repository', pois estamos no Service
        return repository.findAll().stream()
                // Ordena do maior score para o menor
                // Se score for null, considera 0.0 para não dar erro
                .sorted((p1, p2) -> Double.compare(
                        p2.getScore() == null ? 0.0 : p2.getScore(),
                        p1.getScore() == null ? 0.0 : p1.getScore()
                ))
                // Converte cada Player para o DTO de resposta
                .map(p -> new ProfileResponse(
                        p.getId(),
                        p.getName(),
                        p.getCity(),
                        p.getEmail(),
                        p.getPlayerColor(),
                        p.getAvatar(),
                        p.getScore() == null ? 0.0 : p.getScore() // Garante que não vá nulo
                ))
                .toList();
    }
}
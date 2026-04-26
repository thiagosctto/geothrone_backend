package geothroneInc.backend.modules.player;


import geothroneInc.backend.modules.player.dto.LoginRequest;
import geothroneInc.backend.modules.player.dto.ProfileResponse;
import geothroneInc.backend.modules.player.dto.RegisterPlayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/players")
public class PlayerController {

    private final PlayerService service;

    @Autowired
    private PlayerRepository repository;

    public PlayerController(PlayerService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody RegisterPlayer data) {
        try {
            var player = service.createPlayer(data);
            return ResponseEntity.ok(player);
        } catch (Exception e) {
            // Retorna erro 400 se algo der errado (ex: email duplicado)
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody LoginRequest data) {
        try {
            // Tenta autenticar
            var player = service.authenticate(data.email(), data.password());


            var response = new ProfileResponse(
                    player.getId(),
                    player.getName(),
                    player.getCity(),
                    player.getEmail(),
                    player.getPlayerColor(),
                    player.getAvatar(),
                    player.getScore()
            );
            // Retorna 200 OK com os dados do jogador
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Retorna 401 Unauthorized (Não autorizado) se a senha ou email estiverem errados
            // É o código HTTP padrão para "falha de login"
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<ProfileResponse>> getRanking() {
        var ranking = service.getLeaderboard();
        return ResponseEntity.ok(ranking);
    }

    // GET /players/me?id=1
    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(@RequestParam Long id) {
        Optional<Player> player = repository.findById(id);

        if (player.isEmpty()) {
            return ResponseEntity.status(404).body("Player não encontrado");
        }
        return ResponseEntity.ok(player.get());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayerById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
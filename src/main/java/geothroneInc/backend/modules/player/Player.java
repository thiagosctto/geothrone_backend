package geothroneInc.backend.modules.player;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tb_player")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String city;
    private String email;
    private String password;

    @Column(name = "score")
    private Double score = 0.0;

    @Column(name = "km_walked")
    private Double kilometersWalked = 0.0;

    @Column(name = "avatar", columnDefinition = "TEXT")
    private String avatar;

    // O Segredo: Mapear o camelCase do Java para o snake_case do Postgres
    @Column(name = "player_color")
    private String playerColor;
}
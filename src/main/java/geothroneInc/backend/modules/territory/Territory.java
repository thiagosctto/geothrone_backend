package geothroneInc.backend.modules.territory;

import geothroneInc.backend.modules.player.Player;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "tb_territory")
public class Territory {

    // O ID será uma String composta, ex: "LAT-5345_LON-2030"
    // Isso torna a busca MUITO rápida, pois não precisa de cálculo geográfico no banco
    @Id
    private String id;

    // Coordenadas centrais (apenas para o Front saber onde desenhar o quadrado depois)
    private Double centerLat;
    private Double centerLon;

    // Quem é o dono atual desse pedaço de chão?
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private Player owner;
}
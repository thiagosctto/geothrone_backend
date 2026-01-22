package geothroneInc.backend.modules.player.dto;

public record RegisterPlayer (
    String name,
    String city,
    String email,
    String password,
    String playerColor,
    String avatar
){}

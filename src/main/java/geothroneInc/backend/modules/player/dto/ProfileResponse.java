package geothroneInc.backend.modules.player.dto;

public record ProfileResponse(
        Long id,
        String name,
        String city,
        String email,
        String playerColor,
        String avatar,
        Double score
){}

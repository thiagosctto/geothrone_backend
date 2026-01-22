package geothroneInc.backend.modules.player.dto;

public record LoginRequest (
    String email,
    String password
) {}

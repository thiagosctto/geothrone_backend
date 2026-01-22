package geothroneInc.backend.modules.territory.dto;

import java.util.List;

public record RunRequest(
        Long playerId,
        Double totalDistanceKm,
        List<Coordinate> path // O rastro do GPS
) {
    // Um DTO internozinho para as coordenadas
    public record Coordinate(Double latitude, Double longitude) {}
}
package geothroneInc.backend.modules.territory;

import geothroneInc.backend.modules.territory.dto.RunRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/territories")
public class TerritoryController {

    @Autowired
    private TerritoryService service;

    @Autowired
    private TerritoryRepository repository;

    // 1. Receber uma corrida e conquistar terras
    @PostMapping("/run")
    public ResponseEntity<String> submitRun(@RequestBody RunRequest data) {
        service.processRun(data);
        return ResponseEntity.ok("Corrida processada e territórios conquistados!");
    }

    // 2. Enviar todos os territórios para o Mapa Global
    @GetMapping
    public ResponseEntity<List<Territory>> getAllTerritories() {
        return ResponseEntity.ok(repository.findAll());
    }

    @PostMapping("/sync")
    public ResponseEntity<String> syncAllScores() {
        service.auditScores();
        return ResponseEntity.ok("Auditoria completa! Scores recalculados com base no mapa.");
    }
}
package geothroneInc.backend.modules.auth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusServer {

    @GetMapping("/status")
    public String StatusServer() {
        return "OK";
    }
}
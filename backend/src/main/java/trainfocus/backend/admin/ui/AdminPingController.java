package trainfocus.backend.admin.ui;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import trainfocus.backend.auth.ui.AdminOnly;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminPingController {

    @AdminOnly
    @GetMapping("/ping")
    public Map<String, Boolean> ping() {
        return Map.of("ok", true);
    }
}

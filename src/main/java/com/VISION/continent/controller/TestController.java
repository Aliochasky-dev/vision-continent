package com.VISION.continent.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Health Check", description = "Vérifier l'état du serveur")
public class TestController {

    @GetMapping("/")
    @Operation(summary = "Vérifier que le serveur fonctionne",
            description = "Endpoint simple pour vérifier que le backend est opérationnel.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Serveur opérationnel")
    })
    public String home() {
        return "✅ Backend fonctionne !";
    }
}

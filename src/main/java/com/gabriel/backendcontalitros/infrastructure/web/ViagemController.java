package com.gabriel.backendcontalitros.infrastructure.web;

import com.gabriel.backendcontalitros.application.port.in.ConsultarViagensUseCase;
import com.gabriel.backendcontalitros.infrastructure.web.dto.ViagemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Viagens")
@RestController
@RequestMapping("/viagens")
public class ViagemController {

    private final ConsultarViagensUseCase consultarViagens;

    public ViagemController(ConsultarViagensUseCase consultarViagens) {
        this.consultarViagens = consultarViagens;
    }

    @Operation(summary = "Detalhe de uma viagem", description = "404 se a viagem nao existe.")
    @GetMapping("/{id}")
    public ViagemResponse detalhar(@PathVariable Long id) {
        return ViagemResponse.de(consultarViagens.buscar(id));
    }
}

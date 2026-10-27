package com.gabriel.backendcontalitros.infrastructure.web;

import com.gabriel.backendcontalitros.application.port.in.ConsultarViagensUseCase;
import com.gabriel.backendcontalitros.infrastructure.web.dto.ViagemResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/viagens")
public class ViagemController {

    private final ConsultarViagensUseCase consultarViagens;

    public ViagemController(ConsultarViagensUseCase consultarViagens) {
        this.consultarViagens = consultarViagens;
    }

    @GetMapping("/{id}")
    public ViagemResponse detalhar(@PathVariable Long id) {
        return ViagemResponse.de(consultarViagens.buscar(id));
    }
}

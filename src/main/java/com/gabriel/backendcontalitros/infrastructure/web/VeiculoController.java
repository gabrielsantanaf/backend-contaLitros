package com.gabriel.backendcontalitros.infrastructure.web;

import com.gabriel.backendcontalitros.application.port.in.CadastrarVeiculoUseCase;
import com.gabriel.backendcontalitros.application.port.in.ConsultarLeiturasUseCase;
import com.gabriel.backendcontalitros.application.port.in.ConsultarViagensUseCase;
import com.gabriel.backendcontalitros.application.port.in.EditarVeiculoUseCase;
import com.gabriel.backendcontalitros.application.port.in.ListarVeiculosUseCase;
import com.gabriel.backendcontalitros.infrastructure.web.dto.CadastroVeiculoRequest;
import com.gabriel.backendcontalitros.infrastructure.web.dto.EdicaoVeiculoRequest;
import com.gabriel.backendcontalitros.infrastructure.web.dto.LeituraResponse;
import com.gabriel.backendcontalitros.infrastructure.web.dto.VeiculoResponse;
import com.gabriel.backendcontalitros.infrastructure.web.dto.ViagemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Veiculos")
@RestController
@RequestMapping("/veiculos")
public class VeiculoController {

    private final CadastrarVeiculoUseCase cadastrarVeiculo;
    private final EditarVeiculoUseCase editarVeiculo;
    private final ListarVeiculosUseCase listarVeiculos;
    private final ConsultarViagensUseCase consultarViagens;
    private final ConsultarLeiturasUseCase consultarLeituras;

    public VeiculoController(CadastrarVeiculoUseCase cadastrarVeiculo, EditarVeiculoUseCase editarVeiculo,
                             ListarVeiculosUseCase listarVeiculos, ConsultarViagensUseCase consultarViagens,
                             ConsultarLeiturasUseCase consultarLeituras) {
        this.cadastrarVeiculo = cadastrarVeiculo;
        this.editarVeiculo = editarVeiculo;
        this.listarVeiculos = listarVeiculos;
        this.consultarViagens = consultarViagens;
        this.consultarLeituras = consultarLeituras;
    }

    @Operation(summary = "Cadastra um veiculo",
            description = "O deviceId precisa ser igual ao DEVICE_ID configurado no Conta Litro. "
                    + "Responde 409 se o deviceId ja existe. O token do ThingsBoard nunca volta nas respostas.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VeiculoResponse cadastrar(@Valid @RequestBody CadastroVeiculoRequest request) {
        return VeiculoResponse.de(cadastrarVeiculo.cadastrar(request.paraComando()));
    }

    @Operation(summary = "Edita o cadastro de um veiculo",
            description = "Envie so os campos que mudam; ausente ou nulo mantem o valor atual e texto vazio "
                    + "remove placa, modelo ou token. Serve para cadastrar o token do ThingsBoard depois. "
                    + "O deviceId nao muda. Responde 404 se o veiculo nao existe.")
    @PatchMapping("/{id}")
    public VeiculoResponse editar(@PathVariable Long id, @Valid @RequestBody EdicaoVeiculoRequest request) {
        return VeiculoResponse.de(editarVeiculo.editar(id, request.paraComando()));
    }

    @Operation(summary = "Lista os veiculos")
    @GetMapping
    public List<VeiculoResponse> listar() {
        return listarVeiculos.listar().stream().map(VeiculoResponse::de).toList();
    }

    @Operation(summary = "Viagens do veiculo", description = "Da mais recente para a mais antiga.")
    @GetMapping("/{id}/viagens")
    public List<ViagemResponse> viagens(@PathVariable Long id) {
        return consultarViagens.listarPorVeiculo(id).stream().map(ViagemResponse::de).toList();
    }

    @Operation(summary = "Leituras do veiculo no periodo", description = "Periodo [de, ate). Padrao: ultima hora.")
    @GetMapping("/{id}/leituras")
    public List<LeituraResponse> leituras(@PathVariable Long id,
                                          @Parameter(description = "Inicio, ISO-8601 UTC", example = "2026-10-01T00:00:00Z")
                                          @RequestParam(required = false) Instant de,
                                          @Parameter(description = "Fim (exclusivo), ISO-8601 UTC", example = "2026-10-02T00:00:00Z")
                                          @RequestParam(required = false) Instant ate) {
        return consultarLeituras.listar(id, de, ate).stream().map(LeituraResponse::de).toList();
    }
}

package com.gabriel.backendcontalitros.application.service;

import com.gabriel.backendcontalitros.application.port.out.LeituraRepositoryPort;
import com.gabriel.backendcontalitros.application.port.out.PainelPort;
import com.gabriel.backendcontalitros.application.port.out.TransacaoPort;
import com.gabriel.backendcontalitros.application.port.out.VeiculoRepositoryPort;
import com.gabriel.backendcontalitros.application.port.out.ViagemRepositoryPort;
import com.gabriel.backendcontalitros.domain.model.Leitura;
import com.gabriel.backendcontalitros.domain.model.Veiculo;
import com.gabriel.backendcontalitros.domain.model.Viagem;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/** Implementacoes em memoria das portas de saida, para testar a aplicacao sem banco. */
final class RepositoriosEmMemoria {

    final Veiculos veiculos = new Veiculos();
    final Viagens viagens = new Viagens();
    final Leituras leituras = new Leituras();
    final PainelGravador painel = new PainelGravador();
    final TransacaoPort transacao = new TransacaoPort() {
        @Override
        public <T> T executar(Supplier<T> trabalho) {
            return trabalho.get();
        }
    };

    static final class Veiculos implements VeiculoRepositoryPort {
        private final Map<Long, Veiculo> dados = new LinkedHashMap<>();
        private final AtomicLong sequencia = new AtomicLong();

        @Override
        public Veiculo salvar(Veiculo v) {
            Veiculo salvo = v.getId() != null ? v : new Veiculo(sequencia.incrementAndGet(), v.getDeviceId(),
                    v.getPlaca(), v.getModelo(), v.getCapacidadeTanqueL(), v.getMetaConsumoKml(),
                    v.getThingsboardToken(), v.isEquipamentoOnline(), v.getUltimaComunicacao(), v.getCriadoEm());
            dados.put(salvo.getId(), salvo);
            return salvo;
        }

        @Override
        public Optional<Veiculo> buscarPorId(Long id) {
            return Optional.ofNullable(dados.get(id));
        }

        @Override
        public Optional<Veiculo> buscarPorDeviceId(String deviceId) {
            return dados.values().stream().filter(v -> v.getDeviceId().equals(deviceId)).findFirst();
        }

        @Override
        public boolean existePorDeviceId(String deviceId) {
            return buscarPorDeviceId(deviceId).isPresent();
        }

        @Override
        public List<Veiculo> listarTodos() {
            return new ArrayList<>(dados.values());
        }
    }

    static final class Viagens implements ViagemRepositoryPort {
        private final Map<Long, Viagem> dados = new LinkedHashMap<>();
        private final AtomicLong sequencia = new AtomicLong();

        @Override
        public Viagem salvar(Viagem v) {
            Viagem salva = v.getId() != null ? v : new Viagem(sequencia.incrementAndGet(), v.getVeiculoId(),
                    v.getStatus(), v.getInicio(), v.getFim(), v.getUltimaLeituraEm(), v.getMotivoFechamento(),
                    v.getTotais(), v.getNotaEficiencia(), v.getBase());
            dados.put(salva.getId(), salva);
            return salva;
        }

        @Override
        public Optional<Viagem> buscarPorId(Long id) {
            return Optional.ofNullable(dados.get(id));
        }

        @Override
        public Optional<Viagem> buscarAbertaPorVeiculo(Long veiculoId) {
            return dados.values().stream()
                    .filter(v -> v.getVeiculoId().equals(veiculoId) && v.estaAberta())
                    .findFirst();
        }

        @Override
        public List<Viagem> listarPorVeiculo(Long veiculoId) {
            return dados.values().stream()
                    .filter(v -> v.getVeiculoId().equals(veiculoId))
                    .sorted(Comparator.comparing(Viagem::getInicio).reversed())
                    .toList();
        }

        @Override
        public List<Viagem> listarAbertasSemLeituraDesde(Instant limite) {
            return dados.values().stream()
                    .filter(v -> v.estaAberta() && v.getUltimaLeituraEm().isBefore(limite))
                    .toList();
        }

        @Override
        public List<Viagem> listarIniciadasEntre(Instant de, Instant ate) {
            return dados.values().stream()
                    .filter(v -> !v.getInicio().isBefore(de) && v.getInicio().isBefore(ate))
                    .toList();
        }

        List<Viagem> todas() {
            return new ArrayList<>(dados.values());
        }
    }

    static final class Leituras implements LeituraRepositoryPort {
        private final List<Leitura> dados = new ArrayList<>();
        private final AtomicLong sequencia = new AtomicLong();

        @Override
        public Leitura salvar(Leitura l) {
            Leitura salva = new Leitura(sequencia.incrementAndGet(), l.veiculoId(), l.viagemId(), l.recebidaEm(),
                    l.instanteEquipamento(), l.velocidadeKmh(), l.rpm(), l.marcha(), l.tanquePct(), l.vazaoLph(),
                    l.consumoKml(), l.ignicao(), l.payload());
            dados.add(salva);
            return salva;
        }

        @Override
        public boolean existe(Long veiculoId, Instant instanteEquipamento) {
            return dados.stream().anyMatch(l -> l.veiculoId().equals(veiculoId)
                    && Objects.equals(l.instanteEquipamento(), instanteEquipamento));
        }

        @Override
        public List<Leitura> listarPorVeiculo(Long veiculoId, Instant de, Instant ate) {
            return dados.stream()
                    .filter(l -> l.veiculoId().equals(veiculoId)
                            && !l.recebidaEm().isBefore(de) && l.recebidaEm().isBefore(ate))
                    .toList();
        }

        List<Leitura> todas() {
            return List.copyOf(dados);
        }
    }

    static final class PainelGravador implements PainelPort {
        record Envio(String token, Instant instante, String dadosJson) {
        }

        final List<Envio> envios = new ArrayList<>();

        @Override
        public void enviar(String token, Instant instante, String dadosJson) {
            envios.add(new Envio(token, instante, dadosJson));
        }
    }
}

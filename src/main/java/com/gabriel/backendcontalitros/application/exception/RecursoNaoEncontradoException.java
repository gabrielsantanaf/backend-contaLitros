package com.gabriel.backendcontalitros.application.exception;

public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }

    public static RecursoNaoEncontradoException veiculo(Long id) {
        return new RecursoNaoEncontradoException("Veiculo " + id + " nao encontrado");
    }

    public static RecursoNaoEncontradoException viagem(Long id) {
        return new RecursoNaoEncontradoException("Viagem " + id + " nao encontrada");
    }
}

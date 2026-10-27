package com.gabriel.backendcontalitros.infrastructure.web;

import com.gabriel.backendcontalitros.application.exception.DeviceIdJaCadastradoException;
import com.gabriel.backendcontalitros.application.exception.PeriodoInvalidoException;
import com.gabriel.backendcontalitros.application.exception.RecursoNaoEncontradoException;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Converte as excecoes da aplicacao em respostas RFC 9457 (application/problem+json). */
@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    ProblemDetail naoEncontrado(RecursoNaoEncontradoException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(DeviceIdJaCadastradoException.class)
    ProblemDetail deviceIdDuplicado(DeviceIdJaCadastradoException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    /** Cadastro simultaneo do mesmo deviceId: a restricao UNIQUE do banco decide. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail violacaoDeIntegridade(DataIntegrityViolationException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Registro conflita com um existente");
    }

    @ExceptionHandler(PeriodoInvalidoException.class)
    ProblemDetail periodoInvalido(PeriodoInvalidoException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validacao(MethodArgumentNotValidException e) {
        Map<String, String> campos = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField,
                        erro -> String.valueOf(erro.getDefaultMessage()), (a, b) -> a));
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Dados invalidos");
        problema.setProperty("campos", campos);
        return problema;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ProblemDetail parametroInvalido(MethodArgumentTypeMismatchException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Parametro '" + e.getName() + "' invalido. Datas usam ISO-8601 UTC, ex.: 2026-10-01T00:00:00Z");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail corpoIlegivel(HttpMessageNotReadableException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Corpo da requisicao malformado");
    }
}

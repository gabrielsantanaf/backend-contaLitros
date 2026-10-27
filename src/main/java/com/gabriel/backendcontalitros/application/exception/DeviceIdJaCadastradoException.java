package com.gabriel.backendcontalitros.application.exception;

public class DeviceIdJaCadastradoException extends RuntimeException {

    public DeviceIdJaCadastradoException(String deviceId) {
        super("Ja existe um veiculo com o deviceId " + deviceId);
    }
}

package com.websocket.WebsocketProject.dto;

public class OtpLoginResponseDTO {

    private String message;

    public OtpLoginResponseDTO() {
    }

    public OtpLoginResponseDTO(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

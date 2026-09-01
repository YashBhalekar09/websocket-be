package com.websocket.WebsocketProject.dto;

public class LoginResponseDTO {

    private String token;
    private String refreshToken;
    private String username;
    private Long userId;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(String token, String refreshToken, String username, Long userId) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.username = username;
        this.userId = userId;
    }

    public String getToken() {return token;}

    public void setToken(String token) {this.token = token;}

    public String getRefreshToken() {return refreshToken;}

    public void setRefreshToken(String refreshToken) {this.refreshToken = refreshToken;}

    public String getUsername() {return username;}

    public void setUsername(String username) {this.username = username;}

    public Long getUserId() {return userId;}

    public void setUserId(Long userId) {this.userId = userId;}
}

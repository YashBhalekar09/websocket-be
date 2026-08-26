package com.websocket.WebsocketProject.dto;

public class UserResponseDTO {

    private Long userId;
    private String username;
    private String email;
    private boolean online;

    public UserResponseDTO() {
    }

    public UserResponseDTO(Long userId, String username, String email, boolean online) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.online = online;
    }

    public Long getUserId() {return userId;}

    public void setUserId(Long userId) {this.userId = userId;}

    public String getUsername() {return username;}

    public void setUsername(String username) {this.username = username;}

    public String getEmail() {return email;}

    public void setEmail(String email) {this.email = email;}

    public boolean isOnline() {return online;}

    public void setOnline(boolean online) {this.online = online;}
}

package com.websocket.WebsocketProject.dto;

public class ConversationMemberDTO {

    private Long userId;
    private String username;
    private String profilePicture;

    public ConversationMemberDTO(
            Long userId,
            String username,
            String profilePicture
    ) {
        this.userId = userId;
        this.username = username;
        this.profilePicture = profilePicture;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }
}
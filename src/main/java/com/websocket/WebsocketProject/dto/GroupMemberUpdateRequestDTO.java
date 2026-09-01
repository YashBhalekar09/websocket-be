package com.websocket.WebsocketProject.dto;

import java.util.List;

public class GroupMemberUpdateRequestDTO {

    private List<Long> addUserIds;
    private List<Long> removeUserIds;

    public List<Long> getAddUserIds() {
        return addUserIds;
    }

    public void setAddUserIds(List<Long> addUserIds) {
        this.addUserIds = addUserIds;
    }

    public List<Long> getRemoveUserIds() {
        return removeUserIds;
    }

    public void setRemoveUserIds(List<Long> removeUserIds) {
        this.removeUserIds = removeUserIds;
    }
}

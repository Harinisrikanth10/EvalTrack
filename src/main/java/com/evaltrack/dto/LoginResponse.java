package com.evaltrack.dto;

import com.evaltrack.model.Role;
import java.util.Map;

public class LoginResponse {

    private String token;
    private Role role;
    private Map<String, Object> profile;

    public LoginResponse() {}

    public LoginResponse(String token, Role role, Map<String, Object> profile) {
        this.token = token;
        this.role = role;
        this.profile = profile;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Map<String, Object> getProfile() {
        return profile;
    }

    public void setProfile(Map<String, Object> profile) {
        this.profile = profile;
    }
}

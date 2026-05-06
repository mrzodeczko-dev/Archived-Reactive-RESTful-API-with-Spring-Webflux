package com.rzodeczko.domain.security;

import com.rzodeczko.domain.generic.GenericEntity;
import com.rzodeczko.domain.security.enums.Role;

import java.util.Objects;

public abstract sealed class BaseUser implements GenericEntity permits Admin, User {

    private String id;
    private String username;
    private String password;
    private Role role;

    protected BaseUser() {
    }

    protected BaseUser(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseUser)) return false;
        BaseUser b = (BaseUser) o;
        return Objects.equals(id, b.id)
                && Objects.equals(username, b.username)
                && Objects.equals(password, b.password)
                && role == b.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, password, role);
    }
}

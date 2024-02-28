package eu.assina.rssp.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")
})
public class User extends DateAudit implements UserBase {
    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @NotBlank
    private String username;

    @Email
    private String email;

    private String imageUrl;

    @Column(name="email_verified", nullable = false)
    private Boolean emailVerified = false;

    @JsonIgnore // neither in nor out
    private String password;


    @NotNull
    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    @Column(name="provider_id")
    private String providerId;
    
    private String role;

    // User PIN Must be Bcrypt encoded
    @Column(name="encodedpin")
    private String encodedPIN;

    @Column(name="plainpin")
    @JsonProperty("plainPIN") // must be uppercase
    private String plainPIN; // PIN starts in plaintext - input only

    @Column(name="plain_password")
    private String plainPassword; // in only - see setter

    public User() {
        id = UUID.randomUUID().toString();
    }

    public User(String username, String name, String email, AuthProvider provider) {
        id = UUID.randomUUID().toString();
        this.email = email;
        this.name = name;
        this.username = username;
        this.provider = provider;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public AuthProvider getProvider() {
        return provider;
    }

    public void setProvider(AuthProvider provider) {
        this.provider = provider;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setEncodedPIN(String encodedPIN) {
        this.encodedPIN = encodedPIN;
    }

    public String getEncodedPIN() {
        return encodedPIN;
    }

    @JsonIgnore // ignore plain pin on GET but allow it on set
    public String getPlainPIN() {
        return plainPIN;
    }

    public void setPlainPIN(String plainPIN) {
        this.plainPIN = plainPIN;
    }

    @JsonIgnore // ignore plain password on GET but allow it on set
    public String getPlainPassword() {
        return plainPassword;
    }

    public void setPlainPassword(String plainPassword) {
        this.plainPassword = plainPassword;
    }
}

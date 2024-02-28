package eu.assina.rssp.api.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

@Entity
@Table(name="useroid4vp", uniqueConstraints = @UniqueConstraint(columnNames = {"hash"}))
public class UserOID4VP extends DateAudit implements UserBase {
    @Id
    private String id;

    @NotNull
    private String role;

    @NotNull
    private String hash;

    @NotNull
    @Column(name="issuing_country")
    private String issuingCountry;

    @Column(name="issuance_authority")
    private String issuanceAuthority;

    public String determineHash(String familyName, String givenName, String birthDate) throws NoSuchAlgorithmException {
        String familyNameAndGivenNameAndBirthDate = familyName+";"+givenName+";"+birthDate;

        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] result = sha.digest(familyNameAndGivenNameAndBirthDate.getBytes());

        return Base64.getEncoder().encodeToString(result);
    }

    public UserOID4VP(){
        this.id = UUID.randomUUID().toString();
    }

    public UserOID4VP(String familyName, String givenName, String birthDate, String issuingCountry, String issuanceAuthority, String role) throws NoSuchAlgorithmException {
        this.id = UUID.randomUUID().toString();
        this.role = role;
        this.hash = determineHash(familyName, givenName, birthDate);
        this.issuingCountry = issuingCountry;
        this.issuanceAuthority = issuanceAuthority;
    }

    public String getId(){
        return this.id;
    }

    public String getUsername(){
        return this.hash;
    }

    public String getName(){
        return this.hash;
    }

    public String getPassword(){
        return null;
    }

    public String getHash(){
        return this.hash;
    }

    public String getIssuingCountry() {
        return issuingCountry;
    }

    public void setIssuingCountry(String issuingCountry) {
        this.issuingCountry = issuingCountry;
    }

    public String getIssuanceAuthority() {
        return issuanceAuthority;
    }

    public void setIssuanceAuthority(String issuanceAuthority) {
        this.issuanceAuthority = issuanceAuthority;
    }

    public String getRole(){
        return this.role;
    }

    public void setRole(String role){
        this.role = role;
    }

    @Override
    public String toString() {
        String sb = "UserOID4VP{" +
                "id='" + id + '\'' +
                ", hash='" + hash + '\'' +
                ", role='" + role + '\'' +
                ", issuingCountry='" + issuingCountry + '\'' +
                ", issuanceAuthority='" + issuanceAuthority + '\'' +
                '}';
        return sb;
    }
}

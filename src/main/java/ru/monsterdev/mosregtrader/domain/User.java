package ru.monsterdev.mosregtrader.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import lombok.Data;
import ru.monsterdev.mosregtrader.model.ContactInfo;

@Data
@Entity
@Table(name = "users", uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private long id;

  @Column(name = "name", length = 50, nullable = false, unique = true)
  private String name;

  @Column(name = "email", length = 50, nullable = false)
  private String email;

  @Column(name = "fax", length = 25)
  private String fax;

  @Column(name = "phone", length = 25, nullable = false)
  private String phone;

  @Column(name = "firstname", length = 20, nullable = false)
  private String firstName;

  @Column(name = "lastname", length = 20, nullable = false)
  private String lastName;

  @Column(name = "fathername", length = 20, nullable = false)
  private String fatherName;

  @Column(name = "use_NDS", nullable = false)
  private boolean useNDS;

  @Column(name = "NDS")
  private int NDS;

  @Column(name = "cert_name")
  private String certName;

  @Column(name = "cert_hash")
  private String certHash;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Trade> trades = new ArrayList<>();

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "create_dt", updatable = false)
  private Date createDT;

  @Override
  public String toString() {
    return name;
  }

  public ContactInfo getContactInfo() {
    ContactInfo result = new ContactInfo();
    result.setLastName(lastName);
    result.setFirstName(firstName);
    result.setFatherName(fatherName);
    result.setEmail(email);
    result.setPhone(phone);
    result.setFax(fax);
    return result;
  }
}

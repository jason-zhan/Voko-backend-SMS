package com.adbest.smsmarketingentity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "persistent_logins")
public class PersistentLogins {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String series;
    private String token;
    private String last_used;
}

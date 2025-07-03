package com.example.allrealmen.domain.user.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "members")
@Getter @Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Member {
    
    @Id
    private String id;
    
    @Indexed(unique = true, sparse = true)
    private String phoneNumber;
    
    private String password;
    
    private Role role = Role.USER;
    
    public enum Role {
        USER, ADMIN
    }
    
    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }
} 
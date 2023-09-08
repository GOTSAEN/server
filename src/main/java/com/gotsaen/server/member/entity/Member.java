package com.gotsaen.server.member.entity;

import com.gotsaen.server.audit.Auditable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@Entity
public class Member extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(length = 30, nullable = false, updatable = false, unique = true)
    private String email;

    @Column(length = 100, nullable = true)
    private String password;

    @Column(length = 20, nullable = false)
    @Size(min = 2)
    private String businessName;

    @Column(length = 10, nullable = false)
    private Long businessNumber;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles = new ArrayList<>();

    @Builder
    public Member(Long memberId, String businessName, String email, String password, Long businessNumber, List<String> roles) {
        this.memberId = memberId;;
        this.email = email;
        this.password = password;
        this.businessName = businessName;
        this.businessNumber = businessNumber;
        this.roles = roles;
    }

    public Member update(String nickname){
        this.businessName = nickname;
        return this;
    }

    public Member(String email) {
        this.email = email;
    }
}

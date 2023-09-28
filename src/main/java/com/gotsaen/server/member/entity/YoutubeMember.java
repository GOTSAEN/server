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
public class YoutubeMember extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long youtubeMemberId;

    @Column(length = 30, nullable = false, updatable = false, unique = true)
    private String email;

    @Column(length = 10, nullable = false)
    @Size(min = 2)
    private String nickname;

    @Column(length = 500, nullable = true)
    private String avatarUri;

    @Column(length = 500, nullable = true)
    private String channelId;
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles = new ArrayList<>();
    @Builder
    public YoutubeMember(Long youtubeMemberId, String email, String nickname, String avatarUri, String channelId, List<String> roles) {
        this.youtubeMemberId = youtubeMemberId;
        this.email = email;
        this.nickname = nickname;
        this.avatarUri = avatarUri;
        this.channelId = channelId;
        this.roles = roles;
    }
}

package com.gotsaen.server.application.entity;

import com.gotsaen.server.audit.Auditable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Application extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applicationId;
    @Column
    private Long memberId;
    @Column
    private Long advertisementId;
    @Column
    private Long youtubeMemberId;
    @Enumerated(EnumType.STRING)
    private Status status = Status.WAIT;
    @Getter
    public enum Status {
        WAIT, PROGRESS, FINISHED;
    }
}

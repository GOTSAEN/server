package com.gotsaen.server.bookmark.entity;

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
public class Bookmark extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookmarkId;
    @Column
    private Long memberId;
    @Column
    private Long advertisementId;
    @Column
    private Long youtubeMemberId;
}

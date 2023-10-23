package com.gotsaen.server.member.repository;

import com.gotsaen.server.member.entity.YoutubeMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface YoutubeMemberRepository extends JpaRepository<YoutubeMember, Long> {
    Optional<YoutubeMember> findByEmail(String email);

    Page<YoutubeMember> findByCategory(String category, Pageable pageable);
}

package com.gotsaen.server.bookmark.repository;

import com.gotsaen.server.bookmark.entity.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Bookmark findByYoutubeMemberIdAndAdvertisementId(Long youtubeMemberId, Long advertisementId);

    Page<Bookmark> findByYoutubeMemberId(Long youtubeMemberId, Pageable pageable);
}

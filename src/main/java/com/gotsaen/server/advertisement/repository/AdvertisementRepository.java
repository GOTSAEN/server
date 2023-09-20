package com.gotsaen.server.advertisement.repository;

import com.gotsaen.server.advertisement.entity.Advertisement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {
   Page<Advertisement> findAll(Pageable pageable);
   Page<Advertisement> findByCategory(String category, Pageable pageable);
   Optional<Advertisement> findByMemberId(String memberId);
}
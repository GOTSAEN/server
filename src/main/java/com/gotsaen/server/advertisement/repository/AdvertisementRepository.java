package com.gotsaen.server.advertisement.repository;

import com.gotsaen.server.advertisement.entity.Advertisement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {
   Page<Advertisement> findAll(Pageable pageable);
   Page<Advertisement> findByCategoryAndStatus(String category, Advertisement.Status status, Pageable pageable);
   Page<Advertisement> findByStatus(Advertisement.Status status, Pageable pageable);
   Page<Advertisement> findByStatusAndMemberId(Advertisement.Status status, Long memberId, Pageable pageable);
   Optional<Advertisement> findByAdvertisementIdAndMemberId(Long advertisementId, Long memberId);
   List<Advertisement> findByEndDateLessThan(Date date);
   Page<Advertisement> findByProductNameContainingIgnoreCase(String keyword, Pageable pageable);
   Page<Advertisement> findByEndDateBetweenAndStatus(Date currentDate, Date fiveDaysLater, Advertisement.Status status, Pageable pageable);

}
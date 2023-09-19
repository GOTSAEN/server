package com.gotsaen.server.advertisement.repository;

import com.gotsaen.server.advertisement.entity.Advertisement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {
   Page<Advertisement> findAll(Pageable pageable);
}

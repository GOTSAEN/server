package com.gotsaen.server.advertisement.entity;

import com.gotsaen.server.audit.Auditable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Date;

@NoArgsConstructor
@Getter
@Setter
@Entity
public class Advertisement extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long advertisementId;

    @Column(length = 20, nullable = false)
    private String productName;

    @Column(length = 3, nullable = false)
    private Long personnel;

    @Column(nullable = false)
    private Date startDate;

    @Column(nullable = false)
    private Date endDate;

    @Column(length = 10, nullable = false)
    private String category;

    @Column(length = 50, nullable = false)
    private String offer;

    @Column(length = 1000, nullable = false)
    private String productDescription;

    @Column(length = 1000, nullable = false)
    private String precaution;

    @Builder
    public Advertisement(Long advertisementId, String productName, Long personnel,Date startDate, Date endDate, String category, String offer, String productDescription, String precaution) {
        this.advertisementId = advertisementId;
        this.productName = productName;
        this.personnel = personnel;
        this.startDate = startDate;
        this.endDate = endDate;
        this.category = category;
        this.offer = offer;
        this.productDescription = productDescription;
        this.precaution = precaution;
    }
    public Advertisement update(String productName){
        this.productName = productName;
        return this;
    }

    public Advertisement(Long advertisementId) {
        this.advertisementId = advertisementId;
    }
}
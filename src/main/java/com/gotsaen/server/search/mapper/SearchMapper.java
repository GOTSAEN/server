package com.gotsaen.server.search.mapper;

import com.gotsaen.server.advertisement.entity.Advertisement;
import com.gotsaen.server.search.dto.SearchAdvertisementDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SearchMapper {
    SearchAdvertisementDto advertisementToSearchAdvertisementDto(Advertisement advertisement);
}

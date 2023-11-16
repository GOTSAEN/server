package com.gotsaen.server.bookmark.mapper;

import com.gotsaen.server.bookmark.dto.BookmarkAndAdInfoDto;
import com.gotsaen.server.bookmark.entity.Bookmark;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookmarkMapper {
    BookmarkAndAdInfoDto bookmarkToBookmarkAndAdInfoDto(Bookmark bookmark);
}

package com.gotsaen.server.bookmark.service;

import com.gotsaen.server.advertisement.entity.Advertisement;
import com.gotsaen.server.advertisement.repository.AdvertisementRepository;
import com.gotsaen.server.bookmark.dto.BookmarkDto;
import com.gotsaen.server.bookmark.entity.Bookmark;
import com.gotsaen.server.bookmark.repository.BookmarkRepository;
import com.gotsaen.server.dto.MultiResponseDto;
import com.gotsaen.server.exception.BusinessLogicException;
import com.gotsaen.server.exception.ExceptionCode;
import com.gotsaen.server.member.entity.YoutubeMember;
import com.gotsaen.server.member.repository.YoutubeMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkService {
    private final YoutubeMemberRepository youtubeMemberRepository;
    private final AdvertisementRepository advertisementRepository;
    private final BookmarkRepository bookmarkRepository;
    @Transactional
    public void createOrDeleteBookmark(BookmarkDto requestBody, String youtubeMemberEmail) {
        YoutubeMember youtubeMember = youtubeMemberRepository.findByEmail(youtubeMemberEmail)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.YOUTUBER_NOT_FOUND));
        Advertisement advertisement = advertisementRepository.findById(requestBody.getAdvertisementId())
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.ADVERTISEMENT_NOT_FOUND));
        Bookmark existingBookmark = bookmarkRepository
                .findByYoutubeMemberIdAndAdvertisementId(youtubeMember.getYoutubeMemberId(), advertisement.getAdvertisementId());

        if(existingBookmark != null){
            bookmarkRepository.delete(existingBookmark);
        }
        else {
            Bookmark bookmark = new Bookmark();
            bookmark.setAdvertisementId(requestBody.getAdvertisementId());
            bookmark.setMemberId(requestBody.getMemberId());
            bookmark.setYoutubeMemberId(youtubeMember.getYoutubeMemberId());
            bookmarkRepository.save(bookmark);
        }
    }

    public MultiResponseDto findBookmarkByYoutubeMember(String youtubeMemberEmail, int page, int size) {
        Optional<YoutubeMember> optionalYoutubeMember = youtubeMemberRepository.findByEmail(youtubeMemberEmail);
        YoutubeMember findYoutubeMember = optionalYoutubeMember.orElseThrow(() ->
                new BusinessLogicException(ExceptionCode.YOUTUBER_NOT_FOUND));
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Bookmark> bookmarksPage = bookmarkRepository.findByYoutubeMemberId(findYoutubeMember.getYoutubeMemberId(), pageable);
        List<Bookmark> bookmarks = new ArrayList<>();
        bookmarks = bookmarksPage.getContent().stream()
                .collect(Collectors.toList());
        return new MultiResponseDto<>(bookmarks, bookmarksPage);
    }
}

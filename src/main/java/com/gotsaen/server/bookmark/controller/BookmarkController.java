package com.gotsaen.server.bookmark.controller;

import com.gotsaen.server.auth.service.OAuth2MemberService;
import com.gotsaen.server.bookmark.dto.BookmarkDto;
import com.gotsaen.server.bookmark.service.BookmarkService;
import com.gotsaen.server.dto.MultiResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/bookmarks")
@Validated
@Slf4j
@RequiredArgsConstructor
public class BookmarkController {
    private final BookmarkService bookmarkService;
    private final OAuth2MemberService oAuth2MemberService;
    @PostMapping
    public ResponseEntity postBookmark(@Valid @RequestBody BookmarkDto requestBody, Authentication authentication){
        oAuth2MemberService.checkYoutuber(authentication);
        bookmarkService.createOrDeleteBookmark(requestBody, authentication.getPrincipal().toString());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<MultiResponseDto> getBookmarks(
            Authentication authentication,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "1000") int size){
        oAuth2MemberService.checkYoutuber(authentication);
        MultiResponseDto bookmarks = bookmarkService.findBookmarkByYoutubeMember(authentication.getPrincipal().toString(), page, size);

        return new ResponseEntity<>(bookmarks, HttpStatus.OK);
    }

    @GetMapping("/{advertisementId}")
    public ResponseEntity<Boolean> checkAdvertisementBookmark(@PathVariable Long advertisementId, Authentication authentication){
        boolean check = bookmarkService.checkAdvertisementBookmark(advertisementId, authentication.getPrincipal().toString());
        return ResponseEntity.ok(check);
    }
}

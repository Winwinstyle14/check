package com.vhc.ec.notification.service;

import com.vhc.ec.notification.dto.NoticeDto;
import com.vhc.ec.notification.dto.PageDto;
import com.vhc.ec.notification.entity.Notice;
import com.vhc.ec.notification.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final ModelMapper modelMapper;

    public Notice save(Notice notice) {

        return noticeRepository.save(notice);
    }

    /**
     * Find my notice
     *
     * @param email
     * @param fromDate
     * @param toDate
     * @param status
     * @param pageable
     * @return
     */
    public PageDto<NoticeDto> findMyNotice(String email, Date fromDate, Date toDate, Integer status, Pageable pageable) {

        try {
            final var contractPage = noticeRepository.findMyNotice(
                    email,
                    fromDate,
                    toDate,
                    status,
                    pageable
            );

            return modelMapper.map(
                    contractPage, new TypeToken<PageDto<NoticeDto>>() {
                    }.getType()
            );
        } catch (Exception e) {
            log.error("error, {}, {}, {}, {}", email, fromDate, toDate, status, e);
        }

        return PageDto.<NoticeDto>builder()
                .totalPages(0)
                .totalElements(0)
                .content(Collections.emptyList())
                .build();
    }

    /**
     * Cập nhật trạng thái notice
     *
     * @param email
     * @param id
     */
    public Optional<NoticeDto> changeStatus(String email, int id, int status) {

        var noticeOptional = noticeRepository.findById(id);

        if (noticeOptional.isPresent()) {
            Notice notice = noticeOptional.get();

            if (notice.getEmail().equals(email)) {
                notice.setStatus(status);

                final var noticeDto = modelMapper.map(
                        noticeRepository.save(notice),
                        NoticeDto.class
                );

                return Optional.of(noticeDto);
            }
            

        }

        return Optional.empty();
    }
}

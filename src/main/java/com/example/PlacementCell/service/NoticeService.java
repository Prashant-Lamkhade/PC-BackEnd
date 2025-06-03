package com.example.PlacementCell.service;

import com.example.PlacementCell.dto.NoticeRequestDTO;
import com.example.PlacementCell.dto.NoticeResponseDTO;
import com.example.PlacementCell.entity.*;
import com.example.PlacementCell.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NoticeService {

        private final NoticeRepository noticeRepository;
        private final NoticeMappingRepository noticeMappingRepository;
        private final PCRepository pcRepository;
        private final TpoRepository tpoRepository;
        private final CampusRepository campusRepository;
        private final StreamRepository streamRepository;

        public List<NoticeResponseDTO> getAllNotices() {
                List<Notice> notices = noticeRepository.findAll();
                return notices.stream()
                                .map(this::convertToResponseDTO)
                                .collect(Collectors.toList());
        }

        public NoticeResponseDTO createNotice(NoticeRequestDTO requestDTO) {
                // Validate and fetch PC (creator)
                PC creator = pcRepository.findById(requestDTO.getCreatedBy())
                                .orElseThrow(() -> new RuntimeException("Placement Coordinator not found"));
                Tpo approver = tpoRepository.findById(requestDTO.getApprovedBy())
                                .orElseThrow(() -> new RuntimeException("TPO not found"));

                // Create notice entity
                Notice notice = new Notice();
                notice.setJobType(requestDTO.getJobType());
                notice.setCompanyName(requestDTO.getCompanyName());
                notice.setJobRole(requestDTO.getJobRole());
                notice.setJobLocation(requestDTO.getJobLocation());
                notice.setPackageDetails(requestDTO.getPackageDetails());
                notice.setPerformanceBased(requestDTO.getPerformanceBased());
                notice.setModeOfWork(Notice.ModeOfWork.valueOf(requestDTO.getModeOfWork().toUpperCase()));
                notice.setLastDateToApply(requestDTO.getLastDateToApply());
                notice.setJoiningDetails(Notice.JoiningDetails.valueOf(requestDTO.getJoiningDetails().toUpperCase()));
                notice.setCustomJoiningText(requestDTO.getCustomJoiningText());
                notice.setJobResponsibilities(
                                Notice.JobResponsibilities.valueOf(requestDTO.getJobResponsibilities().toUpperCase()));
                notice.setCustomResponsibilities(requestDTO.getCustomResponsibilities());
                notice.setGoogleFormLink(requestDTO.getGoogleFormLink());
                notice.setWhatsappGroupLink(requestDTO.getWhatsappGroupLink());
                notice.setCreatedBy(creator);
                notice.setApprovedBy(approver);
                notice.setStatus(Notice.NoticeStatus.PENDING);

                // Save notice
                Notice savedNotice = noticeRepository.save(notice);

                // Create notice mappings
                createNoticeMappings(savedNotice, requestDTO.getSelectedCampusIds(),
                                requestDTO.getSelectedStreamIds(), creator, approver);

                return convertToResponseDTO(savedNotice);
        }

        private void createNoticeMappings(Notice notice, List<Long> campusIds,
                        List<String> streamIds, PC creator, Tpo approver) {

                System.out.println("=== SIMPLE VERSION ===");

                // Don't filter by campus-stream relationship, just create all combinations
                for (Long campusId : campusIds) {
                        Campus campus = campusRepository.findById(campusId).orElse(null);
                        if (campus == null) {
                                System.out.println("Campus not found: " + campusId);
                                continue;
                        }

                        for (String streamId : streamIds) {
                                Stream stream = streamRepository.findById(streamId).orElse(null);
                                if (stream == null) {
                                        System.out.println("Stream not found: " + streamId);
                                        continue;
                                }

                                try {
                                        NoticeMapping mapping = new NoticeMapping(notice, creator, approver, campus,
                                                        stream);
                                        NoticeMapping saved = noticeMappingRepository.save(mapping);
                                        System.out.println("Saved mapping ID: " + saved.getMappingId());
                                } catch (Exception e) {
                                        System.out.println("Error: " + e.getMessage());
                                        e.printStackTrace();
                                }
                        }
                }
        }

        public NoticeResponseDTO approveNotice(Long noticeId, Long tpoId) {
                Notice notice = noticeRepository.findById(noticeId)
                                .orElseThrow(() -> new RuntimeException("Notice not found"));

                Tpo tpo = tpoRepository.findById(tpoId)
                                .orElseThrow(() -> new RuntimeException("TPO not found"));

                notice.setStatus(Notice.NoticeStatus.APPROVED);
                notice.setApprovedBy(tpo);
                notice.setApprovedAt(LocalDateTime.now());

                Notice savedNotice = noticeRepository.save(notice);
                return convertToResponseDTO(savedNotice);
        }

        public NoticeResponseDTO rejectNotice(Long noticeId, Long tpoId, String rejectionReason) {
                Notice notice = noticeRepository.findById(noticeId)
                                .orElseThrow(() -> new RuntimeException("Notice not found"));

                Tpo tpo = tpoRepository.findById(tpoId)
                                .orElseThrow(() -> new RuntimeException("TPO not found"));

                notice.setStatus(Notice.NoticeStatus.REJECTED);
                notice.setApprovedBy(tpo);
                notice.setRejectionReason(rejectionReason);

                Notice savedNotice = noticeRepository.save(notice);
                return convertToResponseDTO(savedNotice);
        }

        public List<NoticeResponseDTO> getNoticesByStatus(Notice.NoticeStatus status) {
                return noticeRepository.findByStatus(status).stream()
                                .map(this::convertToResponseDTO)
                                .collect(Collectors.toList());
        }

        public List<NoticeResponseDTO> getNoticesByPC(Long pcId) {
                PC pc = pcRepository.findById(pcId)
                                .orElseThrow(() -> new RuntimeException("PC not found"));

                return noticeRepository.findByCreatedBy(pc).stream()
                                .map(this::convertToResponseDTO)
                                .collect(Collectors.toList());
        }

        public List<NoticeResponseDTO> getNoticesByTpo(Long tpoId) {
                Tpo tpo = tpoRepository.findById(tpoId)
                                .orElseThrow(() -> new RuntimeException("TPO not found"));

                return noticeRepository.findByTpo(tpo).stream()
                                .map(this::convertToResponseDTO)
                                .collect(Collectors.toList());
        }

        public Page<NoticeResponseDTO> getPendingNotices(Pageable pageable) {
                return noticeRepository.findByStatusOrderByCreatedAtDesc(Notice.NoticeStatus.PENDING, pageable)
                                .map(this::convertToResponseDTO);
        }

        public NoticeResponseDTO getNoticeById(Long noticeId) {
                Notice notice = noticeRepository.findById(noticeId)
                                .orElseThrow(() -> new RuntimeException("Notice not found"));
                return convertToResponseDTO(notice);
        }

        public void deleteNotice(Long noticeId) {
                Notice notice = noticeRepository.findById(noticeId)
                                .orElseThrow(() -> new RuntimeException("Notice not found"));

                // Delete mappings first
                noticeMappingRepository.deleteByNotice(notice);

                // Delete notice
                noticeRepository.delete(notice);
        }

        private NoticeResponseDTO convertToResponseDTO(Notice notice) {
                NoticeResponseDTO dto = new NoticeResponseDTO();
                dto.setNoticeId(notice.getNoticeId());
                dto.setJobType(notice.getJobType());
                dto.setCompanyName(notice.getCompanyName());
                dto.setJobRole(notice.getJobRole());
                dto.setJobLocation(notice.getJobLocation());
                dto.setPackageDetails(notice.getPackageDetails());
                dto.setPerformanceBased(notice.getPerformanceBased());
                dto.setModeOfWork(notice.getModeOfWork().toString());
                dto.setLastDateToApply(notice.getLastDateToApply());
                dto.setJoiningDetails(notice.getJoiningDetails().toString());
                dto.setCustomJoiningText(notice.getCustomJoiningText());
                dto.setJobResponsibilities(notice.getJobResponsibilities().toString());
                dto.setCustomResponsibilities(notice.getCustomResponsibilities());
                dto.setGoogleFormLink(notice.getGoogleFormLink());
                dto.setWhatsappGroupLink(notice.getWhatsappGroupLink());
                dto.setStatus(notice.getStatus().toString());
                dto.setRejectionReason(notice.getRejectionReason());
                dto.setCreatedAt(notice.getCreatedAt());
                dto.setUpdatedAt(notice.getUpdatedAt());
                dto.setApprovedAt(notice.getApprovedAt());

                // Creator info
                if (notice.getCreatedBy() != null) {
                        dto.setCreatorName(notice.getCreatedBy().getName());
                        dto.setCreatorEmail(notice.getCreatedBy().getCollegeEmail());
                }

                // Approver info
                if (notice.getApprovedBy() != null) {
                        dto.setApproverName(notice.getApprovedBy().getName());
                        dto.setApproverEmail(notice.getApprovedBy().getCollegeEmail());
                }

                // Campus and Stream info
                List<NoticeResponseDTO.CampusStreamInfo> campusStreams = noticeMappingRepository.findByNotice(notice)
                                .stream()
                                .map(mapping -> {
                                        NoticeResponseDTO.CampusStreamInfo info = new NoticeResponseDTO.CampusStreamInfo();
                                        info.setCampusId(mapping.getCampus().getCampusId());
                                        info.setCampusName(mapping.getCampus().getCampusName());
                                        info.setStreamId(mapping.getStream().getStreamId());
                                        info.setStreamName(mapping.getStream().getStreamName());
                                        return info;
                                })
                                .collect(Collectors.toList());

                dto.setCampusStreams(campusStreams);
                return dto;
        }
}
package vn.rikkei.exam.trainingroom.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.rikkei.exam.trainingroom.exception.BadRequestException;
import vn.rikkei.exam.trainingroom.exception.ResourceNotFoundException;
import vn.rikkei.exam.trainingroom.model.AppUser;
import vn.rikkei.exam.trainingroom.model.ReservationRequest;
import vn.rikkei.exam.trainingroom.model.ReservationStatus;
import vn.rikkei.exam.trainingroom.model.ResourceInventory;
import vn.rikkei.exam.trainingroom.model.ResourceType;
import vn.rikkei.exam.trainingroom.repository.AppUserRepository;
import vn.rikkei.exam.trainingroom.repository.ReservationRequestRepository;
import vn.rikkei.exam.trainingroom.repository.ResourceInventoryRepository;
import vn.rikkei.exam.trainingroom.repository.ResourceTypeRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationRequestService {

    private final AppUserRepository userRepository;
    private final ResourceTypeRepository resourceTypeRepository;
    private final ResourceInventoryRepository inventoryRepository;
    private final ReservationRequestRepository requestRepository;

    public ReservationRequest createReservationRequest(
            String userId,
            String resourceCode,
            LocalDate startDate,
            LocalDate endDate,
            Integer participantCount,
            String purpose
    ) {
        log.info("Creating reservation request: userId={}, resourceCode={}, startDate={}, endDate={}, count={}",
                userId, resourceCode, startDate, endDate, participantCount);

        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        ResourceType resourceType = resourceTypeRepository.findByResourceCode(resourceCode)
                .orElseGet(() -> resourceTypeRepository.findById(resourceCode)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phòng đào tạo với mã: " + resourceCode)));

        if (startDate == null || endDate == null) {
            throw new BadRequestException("Ngày bắt đầu và ngày kết thúc không được để trống");
        }
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("Ngày bắt đầu không được sau ngày kết thúc");
        }
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (days > 14) {
            throw new BadRequestException("Một yêu cầu đặt phòng chỉ được kéo dài tối đa 14 ngày");
        }

        if (purpose == null || purpose.trim().length() < 10 || purpose.trim().length() > 200) {
            throw new BadRequestException("Mục đích phải mô tả rõ từ 10 đến 200 ký tự");
        }

        if (participantCount == null || participantCount <= 0) {
            throw new BadRequestException("Số lượng người tham gia phải lớn hơn 0");
        }
        if (participantCount > resourceType.getMaxParticipants()) {
            throw new BadRequestException("Số người tham gia vượt quá sức chứa tối đa (" + resourceType.getMaxParticipants() + ") của loại phòng " + resourceType.getDisplayName());
        }
        if ("PRM".equalsIgnoreCase(resourceType.getResourceCode()) && participantCount < 2) {
            throw new BadRequestException("Nhóm PREMIUM chỉ dành cho yêu cầu có từ 2 người trở lên");
        }

        List<ResourceInventory> inventories = inventoryRepository.findByResourceTypeAndAvailableDateBetween(resourceType, startDate, endDate);
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            final LocalDate currentDate = date;
            boolean available = inventories.stream()
                    .anyMatch(inv -> currentDate.equals(inv.getAvailableDate()) && inv.getAvailableSlots() != null && inv.getAvailableSlots() > 0);
            if (!available && !inventories.isEmpty()) {
                log.warn("Resource inventory slot not available for date {}", currentDate);
            }
        }

        ReservationRequest request = ReservationRequest.builder()
                .requestId("REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .requester(user)
                .resourceType(resourceType)
                .startDate(startDate)
                .endDate(endDate)
                .participantCount(participantCount)
                .purpose(purpose.trim())
                .status(ReservationStatus.PENDING)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        return requestRepository.save(request);
    }

    public ReservationRequest cancelReservationRequest(String requestId) {
        log.info("Cancelling reservation request: requestId={}", requestId);
        ReservationRequest request = getReservationRequest(requestId);
        if (request.getStatus() != ReservationStatus.PENDING) {
            throw new BadRequestException("Chỉ được hủy yêu cầu đang ở trạng thái PENDING");
        }
        request.setStatus(ReservationStatus.CANCELLED);
        request.setUpdatedAt(Instant.now());
        return requestRepository.save(request);
    }

    public ReservationRequest getReservationRequest(String requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu đặt phòng với ID: " + requestId));
    }

    public ReservationRequest approveOrRejectRequest(String requestId, ReservationStatus targetStatus, String decisionNote) {
        log.info("Processing reservation request: requestId={}, status={}", requestId, targetStatus);
        ReservationRequest request = getReservationRequest(requestId);
        if (request.getStatus() != ReservationStatus.PENDING) {
            throw new BadRequestException("Chỉ được xử lý yêu cầu đang ở trạng thái PENDING");
        }
        if (targetStatus != ReservationStatus.APPROVED && targetStatus != ReservationStatus.REJECTED) {
            throw new BadRequestException("Trạng thái quyết định phải là APPROVED hoặc REJECTED");
        }
        request.setStatus(targetStatus);
        request.setDecisionNote(decisionNote);
        request.setUpdatedAt(Instant.now());
        return requestRepository.save(request);
    }
}

package vn.rikkei.exam.trainingroom.tool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import vn.rikkei.exam.trainingroom.model.ReservationRequest;
import vn.rikkei.exam.trainingroom.model.ResourceInventory;
import vn.rikkei.exam.trainingroom.service.ReservationRequestService;
import vn.rikkei.exam.trainingroom.service.ResourceTypeService;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BusinessTools {

    private final ResourceTypeService resourceTypeService;
    private final ReservationRequestService reservationRequestService;

    @Tool(
            name = "search_resource",
            description = "Tìm kiếm thông tin phòng đào tạo còn khả dụng trong khoảng thời gian"
    )
    public List<ResourceInventory> searchResource(
            @ToolParam(description = "Mã hoặc tên loại phòng cần tìm (STD/PRM/Standard/Premium...)", required = false)
            String name,
            @ToolParam(description = "Ngày bắt đầu (yyyy-MM-dd)", required = true)
            LocalDate startDate,
            @ToolParam(description = "Ngày kết thúc (yyyy-MM-dd)", required = true)
            LocalDate endDate
    ) {
        log.info("Tool search_resource: name={}, startDate={}, endDate={}", name, startDate, endDate);
        return resourceTypeService.getTrainingRoomAvailability(name, startDate, endDate);
    }

    @Tool(
            name = "create_reservation_request",
            description = "Tạo một yêu cầu đặt phòng đào tạo mới"
    )
    public ReservationRequest createReservationRequest(
            @ToolParam(description = "Mã người dùng yêu cầu (ví dụ: USR-001)", required = true)
            String userId,
            @ToolParam(description = "Mã loại phòng đào tạo (STD hoặc PRM)", required = true)
            String resourceCode,
            @ToolParam(description = "Ngày bắt đầu đặt phòng (yyyy-MM-dd)", required = true)
            LocalDate startDate,
            @ToolParam(description = "Ngày kết thúc đặt phòng (yyyy-MM-dd)", required = true)
            LocalDate endDate,
            @ToolParam(description = "Số lượng người tham gia", required = true)
            Integer participantCount,
            @ToolParam(description = "Mục đích sử dụng phòng (từ 10 đến 200 ký tự)", required = true)
            String purpose
    ) {
        log.info("Tool create_reservation_request: userId={}, resourceCode={}", userId, resourceCode);
        return reservationRequestService.createReservationRequest(userId, resourceCode, startDate, endDate, participantCount, purpose);
    }

    @Tool(
            name = "cancel_reservation_request",
            description = "Hủy yêu cầu đặt phòng đào tạo (chỉ dành cho yêu cầu ở trạng thái PENDING)"
    )
    public ReservationRequest cancelReservationRequest(
            @ToolParam(description = "Mã ID của yêu cầu đặt phòng (ví dụ: REQ-XXXXX)", required = true)
            String requestId
    ) {
        log.info("Tool cancel_reservation_request: requestId={}", requestId);
        return reservationRequestService.cancelReservationRequest(requestId);
    }

    @Tool(
            name = "get_reservation_status",
            description = "Tra cứu thông tin và trạng thái chi tiết của yêu cầu đặt phòng"
    )
    public ReservationRequest getReservationStatus(
            @ToolParam(description = "Mã ID của yêu cầu đặt phòng (ví dụ: REQ-XXXXX)", required = true)
            String requestId
    ) {
        log.info("Tool get_reservation_status: requestId={}", requestId);
        return reservationRequestService.getReservationRequest(requestId);
    }
}
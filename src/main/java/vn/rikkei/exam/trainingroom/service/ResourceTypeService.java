package vn.rikkei.exam.trainingroom.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.rikkei.exam.trainingroom.model.ResourceInventory;
import vn.rikkei.exam.trainingroom.model.ResourceType;
import vn.rikkei.exam.trainingroom.repository.AppUserRepository;
import vn.rikkei.exam.trainingroom.repository.ReservationRequestRepository;
import vn.rikkei.exam.trainingroom.repository.ResourceInventoryRepository;
import vn.rikkei.exam.trainingroom.repository.ResourceTypeRepository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceTypeService {
    private final AppUserRepository users;
    private final ResourceTypeRepository resourceTypes;
    private final ResourceInventoryRepository inventory;
    private final ReservationRequestRepository requests;

    public List<ResourceInventory> getTrainingRoomAvailability(String resourceType, LocalDate startDate, LocalDate endDate) {
        if (resourceType == null || resourceType.isBlank()) {
            List<ResourceType> allTypes = resourceTypes.findAll();
            if (allTypes.isEmpty()) {
                return Collections.emptyList();
            }
            return inventory.findByResourceTypeInAndAvailableDateBetween(allTypes, startDate, endDate);
        }
        List<ResourceType> matchedTypes = resourceTypes.findByResourceCodeContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(resourceType, resourceType);
        if (matchedTypes.isEmpty()) {
            return Collections.emptyList();
        }
        return inventory.findByResourceTypeInAndAvailableDateBetween(matchedTypes, startDate, endDate);
    }
}

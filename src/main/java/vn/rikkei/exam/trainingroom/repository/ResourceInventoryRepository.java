package vn.rikkei.exam.trainingroom.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.rikkei.exam.trainingroom.model.ResourceInventory;
import vn.rikkei.exam.trainingroom.model.ResourceType;

import java.time.LocalDate;
import java.util.List;

public interface ResourceInventoryRepository extends JpaRepository<ResourceInventory, Long> {
    List<ResourceInventory> findByResourceTypeAndAvailableDateBetween(ResourceType resourceType, LocalDate startDate, LocalDate endDate);
    List<ResourceInventory> findByResourceTypeInAndAvailableDateBetween(List<ResourceType> resourceTypes, LocalDate startDate, LocalDate endDate);
}

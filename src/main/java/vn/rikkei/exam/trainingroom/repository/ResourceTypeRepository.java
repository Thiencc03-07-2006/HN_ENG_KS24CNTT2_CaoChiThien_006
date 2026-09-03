package vn.rikkei.exam.trainingroom.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.rikkei.exam.trainingroom.model.ResourceInventory;
import vn.rikkei.exam.trainingroom.model.ResourceType;

import java.util.List;
import java.util.Optional;

public interface ResourceTypeRepository extends JpaRepository<ResourceType, String> {
    List<ResourceType> findByResourceCodeContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(String resourceCode, String displayName);
    Optional<ResourceType> findByResourceCode(String resourceCode);
}

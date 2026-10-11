package TrustCircle.Project.repository;

import TrustCircle.Project.model.SafetyAlert;
import TrustCircle.Project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SafetyAlertRepository extends JpaRepository<SafetyAlert, Long> {
    List<SafetyAlert> findAllByOrderByCreatedAtDesc();
    List<SafetyAlert> findByUser(User user);
    List<SafetyAlert> findByIsResolvedFalseOrderByCreatedAtDesc();
    long countByIsResolvedFalse();
}

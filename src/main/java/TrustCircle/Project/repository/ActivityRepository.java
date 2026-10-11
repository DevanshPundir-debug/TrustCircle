package TrustCircle.Project.repository;

import TrustCircle.Project.model.Activity;
import TrustCircle.Project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByStatusOrderByActivityDateAsc(String status);
    List<Activity> findByCategoryAndStatusOrderByActivityDateAsc(String category, String status);
    List<Activity> findByCreator(User creator);
    List<Activity> findAllByOrderByCreatedAtDesc();
}

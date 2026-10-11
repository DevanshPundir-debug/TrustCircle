package TrustCircle.Project.repository;

import TrustCircle.Project.model.Activity;
import TrustCircle.Project.model.ActivityParticipant;
import TrustCircle.Project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityParticipantRepository extends JpaRepository<ActivityParticipant, Long> {
    List<ActivityParticipant> findByActivity(Activity activity);
    List<ActivityParticipant> findByUser(User user);
    Optional<ActivityParticipant> findByActivityAndUser(Activity activity, User user);
    long countByActivityAndStatus(Activity activity, String status);
    boolean existsByActivityAndUser(Activity activity, User user);
    List<ActivityParticipant> findByUserAndStatusIn(User user, List<String> statuses);
    List<ActivityParticipant> findByActivityInAndStatus(List<Activity> activities, String status);
}

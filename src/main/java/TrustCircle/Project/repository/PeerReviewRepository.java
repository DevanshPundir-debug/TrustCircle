package TrustCircle.Project.repository;

import TrustCircle.Project.model.Activity;
import TrustCircle.Project.model.PeerReview;
import TrustCircle.Project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PeerReviewRepository extends JpaRepository<PeerReview, Long> {
    List<PeerReview> findByRevieweeOrderByCreatedAtDesc(User reviewee);
    List<PeerReview> findByActivity(Activity activity);
    boolean existsByActivityAndReviewerAndReviewee(Activity activity, User reviewer, User reviewee);
}

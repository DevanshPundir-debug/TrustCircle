package TrustCircle.Project.repository;

import TrustCircle.Project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByIsIdentityVerifiedTrue();
    List<User> findByIsFlaggedTrue();
    List<User> findByTrustScoreGreaterThanEqual(Integer minScore);
}

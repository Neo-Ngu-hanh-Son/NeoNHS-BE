package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);
    Optional<User> findByPhoneNumber(String phoneNumber);

    @Query(value = "SELECT DATE_FORMAT(created_at, '%Y-%m') as period, COUNT(*) as count " +
            "FROM users GROUP BY period ORDER BY period DESC LIMIT :limit", nativeQuery = true)
    List<Map<String, Object>> getMonthlyRegistrationStats(@Param("limit") Integer limit);
}

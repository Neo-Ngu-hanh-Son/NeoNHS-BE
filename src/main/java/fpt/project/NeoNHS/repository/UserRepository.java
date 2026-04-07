package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.User;
import fpt.project.NeoNHS.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

        @Query(value = "SELECT DATE_FORMAT(created_at, '%Y-%m') as period, COUNT(*) as count " +
                        "FROM users " +
                        "WHERE role = 'TOURIST' " +
                        "  AND created_at >= :start " +
                        "  AND created_at < :end " +
                        "GROUP BY period", nativeQuery = true)
        List<Map<String, Object>> getMonthlyTouristRegistrationStatsBetween(
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        @Query(value = "SELECT DATE_FORMAT(created_at, '%Y-Week %u') as period, COUNT(*) as count " +
                        "FROM users " +
                        "WHERE role = 'TOURIST' " +
                        "  AND created_at >= :start " +
                        "  AND created_at < :end " +
                        "GROUP BY period", nativeQuery = true)
        List<Map<String, Object>> getWeeklyTouristRegistrationStatsBetween(
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        @Query(value = "SELECT CONCAT(DATE_FORMAT(created_at, '%Y-%m'), '-W', (FLOOR((DAYOFMONTH(created_at) - 1) / 7) + 1)) as period, "
                        +
                        "COUNT(*) as count " +
                        "FROM users " +
                        "WHERE role = 'TOURIST' " +
                        "  AND created_at >= :start " +
                        "  AND created_at < :end " +
                        "GROUP BY period", nativeQuery = true)
        List<Map<String, Object>> getMonthWeekTouristRegistrationStatsBetween(
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        long countByRole(UserRole role);

        long countByRoleAndIsActiveTrueAndIsBannedFalse(UserRole role);

        Optional<User> findFirstByRole(UserRole role);
        
        List<User> findByIsActiveTrueAndIsBannedFalse();
}

package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.User;
import fpt.project.NeoNHS.entity.UserCheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface UserCheckInRepository extends JpaRepository<UserCheckIn, UUID> {
    Page<UserCheckIn> findAllByUser_Id(UUID userId, Pageable pageable);
    Optional<UserCheckIn> findByIdAndUser_Id(UUID id, UUID userId);

    /**
     * Get the list of checkin point ID that user has already checked-in
     * @param userId
     * @return
     */
    @Query("SELECT uci.checkinPoint.id FROM UserCheckIn uci WHERE uci.user.id = :userId")
    Set<UUID> findCheckedInPointIdsFromUser(@Param("userId") UUID userId);

}

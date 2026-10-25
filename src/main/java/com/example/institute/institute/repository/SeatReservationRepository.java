package com.example.institute.institute.repository;

import com.example.institute.institute.model.ReservationStatus;
import com.example.institute.institute.model.SeatReservation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SeatReservationRepository extends JpaRepository<SeatReservation, Long> {

    @Query("select count(r) from SeatReservation r " +
            "where r.aClass.id = :classId and r.status = :status and r.expiresAt > :now")
    long countActiveHolds(@Param("classId") Long classId,
                          @Param("status") ReservationStatus status,
                          @Param("now") LocalDateTime now);

    @Query("select count(r) > 0 from SeatReservation r " +
            "where r.student.id = :studentId and r.aClass.id = :classId " +
            "and r.status = :status and r.expiresAt > :now")
    boolean hasActiveHold(@Param("studentId") Long studentId,
                          @Param("classId") Long classId,
                          @Param("status") ReservationStatus status,
                          @Param("now") LocalDateTime now);

    List<SeatReservation> findByStatusAndExpiresAtBefore(ReservationStatus status, LocalDateTime time);
    List<SeatReservation> findByStudentIdOrderByReservedAtDesc(Long studentId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SeatReservation> findWithLockById(Long id);
}
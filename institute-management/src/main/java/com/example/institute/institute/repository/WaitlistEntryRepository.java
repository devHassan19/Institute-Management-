package com.example.institute.institute.repository;

import com.example.institute.institute.model.WaitlistEntry;
import com.example.institute.institute.model.WaitlistStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WaitlistEntryRepository extends JpaRepository<WaitlistEntry, Long> {

    @Query("select w from WaitlistEntry w " +
            "where w.aClass.id = :classId and w.status = :status order by w.createdAt asc")
    List<WaitlistEntry> findQueue(@Param("classId") Long classId,
                                  @Param("status") WaitlistStatus status);

    @Query("select count(w) > 0 from WaitlistEntry w " +
            "where w.student.id = :studentId and w.aClass.id = :classId and w.status = :status")
    boolean existsByStudentAndClassAndStatus(@Param("studentId") Long studentId,
                                             @Param("classId") Long classId,
                                             @Param("status") WaitlistStatus status);

    List<WaitlistEntry> findByStudentId(Long studentId);
    @Query("select w from WaitlistEntry w " +
            "where w.student.id = :studentId and w.aClass.id = :classId and w.status = :status")
    List<WaitlistEntry> findEntries(@Param("studentId") Long studentId,
                                    @Param("classId") Long classId,
                                    @Param("status") WaitlistStatus status);
}
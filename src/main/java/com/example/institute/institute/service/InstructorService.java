package com.example.institute.institute.service;

import com.example.institute.institute.exception.BadRequestException;
import com.example.institute.institute.exception.ForbiddenException;
import com.example.institute.institute.exception.InformationExistException;
import com.example.institute.institute.exception.InformationNotFoundException;
import com.example.institute.institute.model.Class;
import com.example.institute.institute.model.Instructor;
import com.example.institute.institute.model.User;
import com.example.institute.institute.repository.InstructorRepository;
import com.example.institute.institute.security.MyUserDetails;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class InstructorService {

    private InstructorRepository instructorRepository;
    private AuditLogService auditLogService;

    public static User getCurrentLogginUser() {
        MyUserDetails myUserDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return myUserDetails.getUser();
    }

    public Instructor createInstructor(Instructor instructor) {
        User currentUser = getCurrentLogginUser();
        String role = currentUser.getRole();
        Long userId = currentUser.getId();
        System.out.println("Service Calling createInstructor ==> ");

        if (!"ADMIN".equals(role)) {
            throw new ForbiddenException("Only admin can add a instructor");
        }

        if (instructor.getName() == null || instructor.getName().isBlank()) {
            throw new BadRequestException("Instructor name is required");
        }

        if (instructorRepository.findByName(instructor.getName()) != null) {
            throw new InformationExistException("Instructor already exists");
        }

        Instructor savedInstructor = instructorRepository.save(instructor);
        auditLogService.createLog(userId, "CREATE", "Instructor",
                savedInstructor.getId(), "Created Instructor: " + savedInstructor.getName());

        return savedInstructor;
    }

    public List<Instructor> getInstructors() {
        System.out.println("Service Calling getInstructor ==> ");
        return instructorRepository.findAll();
    }

    public Instructor getInstructor(Long instructorId) {
        System.out.println("Service Calling getInstructor ==> ");
        return instructorRepository.findById(instructorId)
                .orElseThrow(() -> new InformationNotFoundException(
                        "Instructor with id " + instructorId + " not found"));
    }

    public Instructor updateInstructor(Long insInstructorId, Instructor instructorObject) {
        System.out.println("Service Calling updateInstructor ==> ");
        User currentUser = getCurrentLogginUser();
        if (!"ADMIN".equals(currentUser.getRole())) {
            throw new ForbiddenException("Only admin can update a instructor");
        }

        Instructor instructor = getInstructor(insInstructorId);

        if (instructorObject.getName() == null || instructorObject.getName().isBlank()) {
            throw new BadRequestException("Instructor name is required");
        }

        Instructor existingInstructor = instructorRepository.findByName(instructorObject.getName());
        if (existingInstructor != null && existingInstructor.getName().equals(instructorObject.getName())) {
            throw new InformationExistException("Instructor already exists");
        }
        instructor.setName(instructorObject.getName());
        instructor.setEmail(instructorObject.getEmail());
        instructor.setPhone(instructorObject.getPhone());
        return instructorRepository.save(instructor);
    }

    public void deleteInstructor(Long instructorId) {
        System.out.println("Service Calling deleteInstructor ==> ");

        User currentUser = getCurrentLogginUser();
        if (!"ADMIN".equals(currentUser.getRole())) {
            throw new ForbiddenException("Only admin can Delete a Instructor");
        }

        Instructor instructor = getInstructor(instructorId);
        instructorRepository.delete(instructor);
    }
}

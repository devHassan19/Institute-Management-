package com.example.institute.institute.service;

import com.example.institute.institute.exception.BadRequestException;
import com.example.institute.institute.exception.ForbiddenException;
import com.example.institute.institute.exception.InformationExistException;
import com.example.institute.institute.exception.InformationNotFoundException;
import com.example.institute.institute.model.Class;
import com.example.institute.institute.model.Course;
import com.example.institute.institute.model.Instructor;
import com.example.institute.institute.model.User;
import com.example.institute.institute.repository.ClassRepository;
import com.example.institute.institute.repository.CourseRepository;
import com.example.institute.institute.repository.InstructorRepository;
import com.example.institute.institute.security.MyUserDetails;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ClassService {

    private ClassRepository classRepository;
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    private AuditLogService auditLogService;

    public static User getCurrentLogginUser() {
        MyUserDetails myUserDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return myUserDetails.getUser();
    }

    public Class createClass(Class classObject) {
        User currentUser = getCurrentLogginUser();
        String role = currentUser.getRole();
        Long userId = currentUser.getId();
        System.out.println("Service Calling createClass ==> ");

        if (!"ADMIN".equals(role)) {
            throw new ForbiddenException("Only admin can add a class");
        }

        if (classObject.getName() == null || classObject.getName().isBlank()) {
            throw new BadRequestException("Class name is required");
        }

        if (classRepository.findByName(classObject.getName()) != null) {
            throw new InformationExistException("Class already exists");
        }

        if (classObject.getCourse() == null || classObject.getCourse().getId() == null) {
            throw new BadRequestException("Course is required");
        }

        if (classObject.getInstructor() == null || classObject.getInstructor().getId() == null) {
            throw new BadRequestException("Instructor is required");
        }

        Course course = courseRepository.findById(classObject.getCourse()
                .getId()).orElseThrow(() ->
                new InformationNotFoundException("Course not found"));

        Instructor instructor = instructorRepository.findById(classObject.getInstructor()
                .getId()).orElseThrow(() ->
                new InformationNotFoundException("Instructor not found"));

        classObject.setCourse(course);
        classObject.setInstructor(instructor);

        Class savedClass = classRepository.save(classObject);
        auditLogService.createLog(userId, "CREATE", "Class",
                savedClass.getId(), "Created course: " + savedClass.getName());
        return savedClass;
    }

    public List<Class> getClasses() {
        System.out.println("Service Calling getClasses ==> ");
        return classRepository.findAll();
    }

    public Class getClass(Long classId) {
        System.out.println("Service Calling getClass ==> ");
        return classRepository.findById(classId)
                .orElseThrow(() -> new InformationNotFoundException(
                        "Class with id: " + classId + " not found"));
    }

    public Class updateClass(Long classId, Class classObject) {
        System.out.println("Service Calling updateClass ==> ");
        User currentUser = getCurrentLogginUser();
        Long userId = currentUser.getId();
        if (!"ADMIN".equals(currentUser.getRole())) {
            throw new ForbiddenException("Only admin can update a instructor");
        }

        Class existingClass = getClass(classId);

        if (classObject.getName() == null || classObject.getName().isBlank()) {
            throw new BadRequestException("Class name is required");
        }

        Class existing = classRepository.findByName(classObject.getName());
        if (existing != null && existing.getName().equals(classObject.getName())) {
            throw new InformationExistException("Class already exists");
        }

        existingClass.setName(classObject.getName());
        existingClass.setDays(classObject.getDays());
        existingClass.setStartDate(classObject.getStartDate());
        existingClass.setEndDate(classObject.getEndDate());
        existingClass.setCapacity(classObject.getCapacity());

        Class savedClass = classRepository.save(classObject);

        auditLogService.createLog(userId, "UPDATE", "Class",
                savedClass.getId(), "UPDATE Class: " + savedClass.getName());

        return savedClass;
    }

    public void deleteClass(Long classId) {
        System.out.println("Service Calling deleteClass ==> ");

        User currentUser = getCurrentLogginUser();
        Long userId = currentUser.getId();
        if (!"ADMIN".equals(currentUser.getRole())) {
            throw new ForbiddenException("Only admin can Delete a Instructor");
        }

        Class existingClass = getClass(classId);

        auditLogService.createLog(userId, "DELETE", "Class",
                existingClass.getId(), "DELETE Class: " + existingClass.getName());

        classRepository.delete(existingClass);
    }

    //    Search / Filtering
    public List<Class> searchClasses(String name) {
        System.out.println("Service Calling searchClasses ==> ");
        return classRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Class> filterByCourse(Long courseId) {
        System.out.println("Service Calling filterByCourse ==> ");
        return classRepository.findByCourseId(courseId);
    }

    //    Sorting
    public List<Class> getAllClasses(Sort sort) {
        return classRepository.findAll(sort);
    }

    //    Pagination
    public Page<Class> getClassesByPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return classRepository.findAll(pageable);
    }
}

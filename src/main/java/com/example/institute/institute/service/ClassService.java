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

    public static User getCurrentLogginUser() {
        MyUserDetails myUserDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return myUserDetails.getUser();
    }

    public Class createClass(Class classObject) {
        User currentUser = getCurrentLogginUser();
        String role = currentUser.getUserProfile().getRole();
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

        return classRepository.save(classObject);
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
        if (!"ADMIN".equals(currentUser.getUserProfile().getRole())) {
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
        return classRepository.save(classObject);
    }

    public void deleteClass(Long classId) {
        System.out.println("Service Calling deleteClass ==> ");

        User currentUser = getCurrentLogginUser();
        if (!"ADMIN".equals(currentUser.getUserProfile().getRole())) {
            throw new ForbiddenException("Only admin can Delete a Instructor");
        }

        Class existingClass = getClass(classId);
        classRepository.delete(existingClass);
    }
}

package com.example.institute.institute.service;

import com.example.institute.institute.exception.*;
import com.example.institute.institute.model.Course;
import com.example.institute.institute.model.User;
import com.example.institute.institute.repository.CourseRepository;
import com.example.institute.institute.security.MyUserDetails;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CourseService {

    private CourseRepository courseRepository;

    public static User getCurrentLogginUser() {
        MyUserDetails myUserDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return myUserDetails.getUser();
    }

    public Course createCourse(Course course) {
        User currentUser = getCurrentLogginUser();
        String role = currentUser.getUserProfile().getRole();
        System.out.println("Service Calling createCourse ==> ");
        // 403: مسجّل دخول بس ما عنده صلاحية
        if (!"ADMIN".equals(role)) {
            throw new ForbiddenException("Only admin can create a course");
        }

        // 400: الطلب ناقص
        if (course.getName() == null || course.getName().isBlank()) {
            throw new BadRequestException("Course name is required");
        }

        // 422: الشكل صحيح بس القيمة غير مقبولة
        if (course.getName().length() < 3) {
            throw new UnprocessableEntityException("Course name must be at least 3 characters");
        }

        // 409: موجود من قبل
        if (courseRepository.findByName(course.getName()) != null) {
            throw new InformationExistException("Course already exists");
        }

        return courseRepository.save(course);
    }

    public List<Course> getCourses() {
        System.out.println("Service Calling getCourses ==> ");
        return courseRepository.findAll();
    }

    public Course getCourse(Long courseId) {
        System.out.println("Service Calling getCourse ==> ");
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new InformationNotFoundException(
                        "Course with id " + courseId + " not found"));          // 404
    }

    public Course updateCourse(Long courseId, Course courseObject) {
        System.out.println("Service Calling updateCourse ==> ");

        // 403: مو أدمن
        User currentUser = getCurrentLogginUser();
        if (!"ADMIN".equals(currentUser.getUserProfile().getRole())) {
            throw new ForbiddenException("Only admin can update a course");
        }

        // 404: الكورس مو موجود
        Course course = getCourse(courseId);

        // 400: الاسم فاضي
        if (courseObject.getName() == null || courseObject.getName().isBlank()) {
            throw new BadRequestException("Course name is required");
        }

        // 409: اسم مستخدم في كورس ثاني
        Course existing = courseRepository.findByName(courseObject.getName());
        if (existing != null && !existing.getId().equals(courseId)) {
            throw new InformationExistException(
                    "Course " + courseObject.getName() + " already exists");
        }

        course.setName(courseObject.getName());
        course.setDescription(courseObject.getDescription());
        return courseRepository.save(course);
    }

    public void deleteCourse(Long courseId) {
        System.out.println("Service Calling deleteCourse ==> ");

        // 403: مو أدمن
        User currentUser = getCurrentLogginUser();
        if (!"ADMIN".equals(currentUser.getUserProfile().getRole())) {
            throw new ForbiddenException("Only admin can delete a course");
        }

        Course course = getCourse(courseId);
        courseRepository.delete(course);
    }
}

package com.hustsimulator.context.enrollment;

import com.hustsimulator.context.entity.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping
    public List<Enrollment> findAll() {
        return enrollmentService.findAll();
    }

    @GetMapping("/{id}")
    public Enrollment findById(@PathVariable UUID id) {
        return enrollmentService.findById(id);
    }

    @GetMapping("/student/{studentId}")
    public List<Enrollment> findByStudentId(@PathVariable UUID studentId) {
        return enrollmentService.findByStudentId(studentId);
    }

    @GetMapping("/teacher/{teacherId}")
    public List<Enrollment> findByTeacherId(@PathVariable UUID teacherId) {
        return enrollmentService.findByTeacherId(teacherId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Enrollment create(@Valid @RequestBody Enrollment enrollment) {
        return enrollmentService.create(enrollment);
    }

    @PatchMapping("/{id}/status")
    public Enrollment updateStatus(@PathVariable UUID id, @RequestParam EnrollmentStatus status) {
        return enrollmentService.updateStatus(id, status);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        enrollmentService.delete(id);
    }
}

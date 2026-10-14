package com.placement.controller;

import com.placement.model.Job;
import com.placement.model.JobApplication;
import com.placement.model.Student;
import com.placement.repository.ApplicationRepository;
import com.placement.repository.JobRepository;
import com.placement.repository.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/student")
public class StudentController {
    private final StudentRepository students;
    private final JobRepository jobs;
    private final ApplicationRepository apps;

    public StudentController(StudentRepository students, JobRepository jobs, ApplicationRepository apps) {
        this.students = students;
        this.jobs = jobs;
        this.apps = apps;
    }

    record ProfileReq(String name, String email, String phone, String branch, Double cgpa, Integer backlogs, String skills) {}

    private Student me(Principal p) {
        return students.findByUserUsername(p.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student profile not found"));
    }

    @GetMapping("/profile")
    public Student profile(Principal p) { return me(p); }

    @PutMapping("/profile")
    public Student update(Principal p, @RequestBody ProfileReq r) {
        Student s = me(p);
        if (r.name() != null && !r.name().isBlank()) s.name = r.name().trim();
        if (r.email() != null) s.email = r.email();
        if (r.phone() != null) s.phone = r.phone();
        if (r.branch() != null) s.branch = r.branch();
        if (r.cgpa() != null) s.cgpa = r.cgpa();
        if (r.backlogs() != null) s.backlogs = r.backlogs();
        if (r.skills() != null) s.skills = r.skills();
        return students.save(s);
    }

    @GetMapping("/jobs")
    public List<Map<String, Object>> jobs(Principal p) {
        Student s = me(p);
        Set<Long> applied = apps.findByStudent(s).stream().map(a -> a.job.id).collect(Collectors.toSet());
        return jobs.findAll().stream().map(j -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("job", j);
            m.put("eligible", j.allows(s));
            m.put("applied", applied.contains(j.id));
            m.put("closed", j.lastDate != null && j.lastDate.isBefore(LocalDate.now()));
            return m;
        }).collect(Collectors.toList());
    }

    @PostMapping("/jobs/{id}/apply")
    public JobApplication apply(Principal p, @PathVariable Long id) {
        Student s = me(p);
        Job j = jobs.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));
        if (apps.existsByStudentAndJob(s, j))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already applied for this job");
        if (j.lastDate != null && j.lastDate.isBefore(LocalDate.now()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Application deadline has passed");
        if (!j.allows(s))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You do not meet the eligibility criteria");
        JobApplication a = new JobApplication();
        a.student = s;
        a.job = j;
        return apps.save(a);
    }

    @GetMapping("/applications")
    public List<JobApplication> applications(Principal p) {
        return apps.findByStudent(me(p));
    }
}

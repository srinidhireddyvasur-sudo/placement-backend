package com.placement.controller;

import com.placement.model.Company;
import com.placement.model.Job;
import com.placement.model.JobApplication;
import com.placement.model.Student;
import com.placement.repository.ApplicationRepository;
import com.placement.repository.CompanyRepository;
import com.placement.repository.JobRepository;
import com.placement.repository.StudentRepository;
import com.placement.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final CompanyRepository cr;
    private final JobRepository jr;
    private final StudentRepository sr;
    private final ApplicationRepository ar;
    private final UserRepository ur;
    private final PasswordEncoder enc;

    public AdminController(CompanyRepository cr, JobRepository jr, StudentRepository sr,
                           ApplicationRepository ar, UserRepository ur, PasswordEncoder enc) {
        this.cr = cr;
        this.jr = jr;
        this.sr = sr;
        this.ar = ar;
        this.ur = ur;
        this.enc = enc;
    }

    record JobReq(Long companyId, String companyName, String title, String description, Double minCgpa,
                  Integer maxBacklogs, String branches, Double salaryLpa, LocalDate lastDate) {}
    record StatusReq(String status) {}
    record StudentReq(String name, String email, String phone, String branch, Double cgpa,
                      Integer backlogs, String skills, String password) {}

    private static final Set<String> STATUSES = Set.of("APPLIED", "SHORTLISTED", "INTERVIEW", "SELECTED", "REJECTED");

    private static ResponseStatusException bad(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, m); }
    private static ResponseStatusException notFound(String m) { return new ResponseStatusException(HttpStatus.NOT_FOUND, m); }

    // ---- Companies ----
    @GetMapping("/companies")
    public List<Company> companies() { return cr.findAll(); }

    @PostMapping("/companies")
    public Company addCompany(@RequestBody Company c) {
        if (c.name == null || c.name.isBlank()) throw bad("Company name is required");
        c.id = null;
        return cr.save(c);
    }

    @PutMapping("/companies/{id}")
    public Company updateCompany(@PathVariable Long id, @RequestBody Company c) {
        Company e = cr.findById(id).orElseThrow(() -> notFound("Company not found"));
        if (c.name == null || c.name.isBlank()) throw bad("Company name is required");
        e.name = c.name.trim();
        e.location = c.location;
        e.description = c.description;
        return cr.save(e);
    }

    @DeleteMapping("/companies/{id}")
    @Transactional
    public void deleteCompany(@PathVariable Long id) {
        Company c = cr.findById(id).orElseThrow(() -> notFound("Company not found"));
        for (Job j : jr.findByCompany(c)) {
            ar.deleteByJob(j);
            jr.delete(j);
        }
        cr.delete(c);
    }

    // ---- Jobs ----
    @GetMapping("/jobs")
    public List<Job> jobs() { return jr.findAll(); }

    @PostMapping("/jobs")
    public Job addJob(@RequestBody JobReq r) {
        if ((r.companyName() == null || r.companyName().isBlank()) && r.companyId() == null)
            throw bad("Enter a company name");
        if (r.title() == null || r.title().isBlank()) throw bad("Job title is required");
        Job j = new Job();
        j.company = resolveCompany(r);
        applyJob(j, r);
        return jr.save(j);
    }

    @PutMapping("/jobs/{id}")
    public Job updateJob(@PathVariable Long id, @RequestBody JobReq r) {
        Job j = jr.findById(id).orElseThrow(() -> notFound("Job not found"));
        if (r.title() == null || r.title().isBlank()) throw bad("Job title is required");
        if (r.companyId() != null || (r.companyName() != null && !r.companyName().isBlank())) {
            j.company = resolveCompany(r);
        }
        applyJob(j, r);
        return jr.save(j);
    }

    private Company resolveCompany(JobReq r) {
        if (r.companyName() != null && !r.companyName().isBlank()) {
            String n = r.companyName().trim();
            List<Company> found = cr.findByNameIgnoreCase(n);
            if (!found.isEmpty()) return found.get(0);
            Company c = new Company();
            c.name = n;
            return cr.save(c);
        }
        return cr.findById(r.companyId()).orElseThrow(() -> notFound("Company not found"));
    }

    private void applyJob(Job j, JobReq r) {
        j.title = r.title().trim();
        j.description = r.description();
        j.minCgpa = r.minCgpa() == null ? 0 : r.minCgpa();
        j.maxBacklogs = r.maxBacklogs() == null ? 0 : r.maxBacklogs();
        j.branches = (r.branches() == null || r.branches().isBlank()) ? "ALL" : r.branches().trim();
        j.salaryLpa = r.salaryLpa() == null ? 0 : r.salaryLpa();
        j.lastDate = r.lastDate();
    }

    @DeleteMapping("/jobs/{id}")
    @Transactional
    public void deleteJob(@PathVariable Long id) {
        Job j = jr.findById(id).orElseThrow(() -> notFound("Job not found"));
        ar.deleteByJob(j);
        jr.delete(j);
    }

    @GetMapping("/jobs/{id}/eligible")
    public List<Student> eligible(@PathVariable Long id) {
        Job j = jr.findById(id).orElseThrow(() -> notFound("Job not found"));
        return sr.findAll().stream().filter(j::allows).collect(Collectors.toList());
    }

    // ---- Students ----
    @GetMapping("/students")
    public List<Student> students() { return sr.findAll(); }

    @PutMapping("/students/{id}")
    @Transactional
    public Student updateStudent(@PathVariable Long id, @RequestBody StudentReq r) {
        Student s = sr.findById(id).orElseThrow(() -> notFound("Student not found"));
        if (r.name() != null && !r.name().isBlank()) s.name = r.name().trim();
        s.email = r.email();
        s.phone = r.phone();
        s.branch = r.branch();
        if (r.cgpa() != null) s.cgpa = r.cgpa();
        if (r.backlogs() != null) s.backlogs = r.backlogs();
        s.skills = r.skills();
        if (r.password() != null && !r.password().isBlank()) {
            if (r.password().length() < 6) throw bad("Password must be at least 6 characters");
            s.user.password = enc.encode(r.password());
            ur.save(s.user);
        }
        return sr.save(s);
    }

    @DeleteMapping("/students/{id}")
    @Transactional
    public void deleteStudent(@PathVariable Long id) {
        Student s = sr.findById(id).orElseThrow(() -> notFound("Student not found"));
        ar.deleteAll(ar.findByStudent(s));
        sr.delete(s);
        sr.flush();
        if (s.user != null) ur.delete(s.user);
    }

    // ---- Applications ----
    @GetMapping("/applications")
    public List<JobApplication> applications() { return ar.findAll(); }

    @PutMapping("/applications/{id}/status")
    public JobApplication setStatus(@PathVariable Long id, @RequestBody StatusReq r) {
        if (r.status() == null || !STATUSES.contains(r.status())) throw bad("Invalid status");
        JobApplication a = ar.findById(id).orElseThrow(() -> notFound("Application not found"));
        a.status = r.status();
        return ar.save(a);
    }

    @DeleteMapping("/applications/{id}")
    public void deleteApplication(@PathVariable Long id) {
        if (!ar.existsById(id)) throw notFound("Application not found");
        ar.deleteById(id);
    }

    // ---- Reports ----
    @GetMapping("/reports")
    public Map<String, Object> reports() {
        List<JobApplication> all = ar.findAll();
        long students = sr.count();
        long placed = all.stream().filter(a -> "SELECTED".equals(a.status)).map(a -> a.student.id).distinct().count();
        Map<String, Long> byStatus = all.stream()
                .collect(Collectors.groupingBy(a -> a.status, TreeMap::new, Collectors.counting()));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("students", students);
        m.put("companies", cr.count());
        m.put("jobs", jr.count());
        m.put("applications", all.size());
        m.put("placedStudents", placed);
        m.put("placementPercent", students == 0 ? 0 : Math.round(placed * 1000.0 / students) / 10.0);
        m.put("byStatus", byStatus);
        return m;
    }
}

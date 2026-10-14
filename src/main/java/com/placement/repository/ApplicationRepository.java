package com.placement.repository;

import com.placement.model.Job;
import com.placement.model.JobApplication;
import com.placement.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApplicationRepository extends JpaRepository<JobApplication, Long> {
    boolean existsByStudentAndJob(Student student, Job job);
    List<JobApplication> findByStudent(Student student);
    void deleteByJob(Job job);
}

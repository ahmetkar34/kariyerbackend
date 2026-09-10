package org.example.kariyerbackend.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "job_postings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employer_id", nullable = false)
    private Long employerId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 200)
    private String company;

    @Column(nullable = false, length = 150)
    private String location;

    @Column(name = "job_type", nullable = false, length = 50)
    private String type;

    @Column(nullable = false)
    private boolean remote;

    @Column(length = 100)
    private String salary;

    @Lob
    @Column(nullable = false)
    private String description;

    @Lob
    @Column(name = "about_company")
    private String aboutCompany;

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private long viewCount = 0L;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "job_posting_tags", joinColumns = @JoinColumn(name = "job_posting_id"))
    @OrderColumn(name = "position")
    @Column(name = "tag", length = 100)
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "job_posting_responsibilities", joinColumns = @JoinColumn(name = "job_posting_id"))
    @OrderColumn(name = "position")
    @Column(name = "item", length = 500)
    @Builder.Default
    private List<String> responsibilities = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "job_posting_requirements", joinColumns = @JoinColumn(name = "job_posting_id"))
    @OrderColumn(name = "position")
    @Column(name = "item", length = 500)
    @Builder.Default
    private List<String> requirements = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

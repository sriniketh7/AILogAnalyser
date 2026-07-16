package com.proj1.ailoganlzr.Model;

import com.proj1.ailoganlzr.enums.AnalysisStatus;
import com.proj1.ailoganlzr.enums.AnalysisType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "analysis_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRequest extends TimeBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String rawLog;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnalysisStatus status;

    @Column(name = "request_hash")
    private String requestHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "analysis_type", nullable = false)
    private AnalysisType analysisType;


    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;


    @OneToOne(
            mappedBy = "analysisRequest",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY
    )
    private AnalysisResult analysisResult;

    @PrePersist
    public void initializeStatus() {

        if (status == null) {
            status = AnalysisStatus.PENDING;
        }

    }

    public void addAnalysisResult(AnalysisResult result){

        this.analysisResult = result;

        result.setAnalysisRequest(this);

    }

}

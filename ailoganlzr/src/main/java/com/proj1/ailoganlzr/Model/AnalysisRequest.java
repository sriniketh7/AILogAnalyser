package com.proj1.ailoganlzr.Model;

import com.proj1.ailoganlzr.enums.AnalysisStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_request")
@Data
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

    @PrePersist
    public void initializeStatus() {

        if (status == null) {
            status = AnalysisStatus.PENDING;
        }

    }

}

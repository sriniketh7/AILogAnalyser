package com.proj1.ailoganlzr.Model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "analysis_result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResult extends TimeBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String summary;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String rootCause;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String solution;

    @Column(nullable = false)
    private Integer confidence;

    @Column(nullable = false, length = 100)
    private String aiModel;

    @Column(nullable = false)
    private String promptVersion;

    @Column(nullable = false)
    private Long processingTimeMs;

    @Column(nullable = false)
    private Boolean cached = false;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_request_id", nullable = false, unique = true)
    private AnalysisRequest analysisRequest;
}

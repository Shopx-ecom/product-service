package com.shopx.product.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @author Sameer Shaikh
 * @date 30-03-2026
 * @description
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@MappedSuperclass
@SuperBuilder
@EntityListeners(AuditingEntityListener.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now(ZoneOffset.UTC);

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @Column(name = "deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "created_by")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long createdBy;

    @Column(name = "updated_by")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long updatedBy;

    @Column(name = "deleted_by")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long deletedBy;

    @CreatedDate
    @Column(name = "created_at_epoch", updatable = false)
    private Long createdAtEpoch;

    @Column(name = "last_updated_at_epoch")
    private Long lastUpdatedAtEpoch;

    @PrePersist
    public void prePersist() {
        this.createdAtEpoch = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC);
    }

    @PreUpdate
    public void preUpdate() {
        this.lastUpdatedAtEpoch = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC);
    }
}

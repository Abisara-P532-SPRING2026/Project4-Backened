package com.rpl.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.Instant;

@Entity
public class AssetUtilisationRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private ImplementedAction implementedAction;

    @ManyToOne(optional = false)
    @JsonIgnore
    private ResourceAllocation resourceAllocation;

    private String assetId;
    private Instant startTime;
    private Instant endTime;
    private Long durationMinutes;

    public Long getId() {
        return id;
    }

    public ImplementedAction getImplementedAction() {
        return implementedAction;
    }

    public void setImplementedAction(ImplementedAction implementedAction) {
        this.implementedAction = implementedAction;
    }

    public ResourceAllocation getResourceAllocation() {
        return resourceAllocation;
    }

    public void setResourceAllocation(ResourceAllocation resourceAllocation) {
        this.resourceAllocation = resourceAllocation;
    }

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public Long getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Long durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
}

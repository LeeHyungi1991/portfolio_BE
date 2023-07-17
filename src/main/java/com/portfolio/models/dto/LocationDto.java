package com.portfolio.models.dto;

import com.portfolio.models.entity.Location;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@RequiredArgsConstructor
public class LocationDto implements Serializable {
    public LocationDto(Location location) {
        this.seq = location.getSeq();
        this.name = location.getName();
        this.details = location.getDetails();
        this.address = location.getAddress();
        this.xAxis = location.getXAxis();
        this.yAxis = location.getYAxis();
        this.userSeq = location.getUser().getSeq();
        this.userName = location.getUser().getName();
        this.createAt = location.getCreateAt();
        this.imagePath = location.getImagePath();
    }
    private Long seq;
    private String name;
    private String details;
    private String address;
    private Double xAxis;
    private Double yAxis;
    private Long userSeq;
    private String userName;
    private Timestamp createAt;
    private String imagePath;
    private boolean on = false;
}

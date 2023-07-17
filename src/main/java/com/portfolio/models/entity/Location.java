package com.portfolio.models.entity;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "locations")
public class Location implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq", nullable = false)
    private Long seq;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "details")
    private String details;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "x_axis", nullable = false)
    private Double xAxis;

    @Column(name = "y_axis", nullable = false)
    private Double yAxis;

    @Column(name = "create_at", nullable = false)
    private Timestamp createAt;

    @Column(name = "imagePath")
    private String imagePath;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_seq", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @OneToMany(mappedBy = "one", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Line> linesOne;

    @OneToMany(mappedBy = "two", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Line> linesTwo;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Location location = (Location) o;
        return seq != null && Objects.equals(seq, location.seq);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

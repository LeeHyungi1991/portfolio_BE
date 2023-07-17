package com.portfolio.models.entity;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "lines")
public class Line {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq", nullable = false)
    private Long seq;

    @Column(name = "distance", nullable = false)
    private Double distance;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "seq_one", nullable = false, insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Location one;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "seq_two", nullable = false, insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Location two;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Line line = (Line) o;
        return seq != null && Objects.equals(seq, line.seq);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

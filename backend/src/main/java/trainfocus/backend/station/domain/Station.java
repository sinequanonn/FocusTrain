package trainfocus.backend.station.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import trainfocus.backend.common.domain.BaseEntity;

@Getter
@Entity
@Table(name = "stations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Station extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 50, nullable = false)
    private String name;

    private Station(String name) {
        this.name = name;
    }

    public static Station createNewStation(String name) {
        return new Station(name);
    }
}

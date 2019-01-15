package no.uio.ifi.trackfind.backend.dao;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "source")
@IdClass(SourceId.class)
@Data
@EqualsAndHashCode(of = {"id", "rawVersion", "curatedVersion"})
public class Source {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "source_ids_generator")
    @GenericGenerator(name = "source_ids_generator", strategy = "no.uio.ifi.trackfind.backend.dao.SourceIdGenerator")
//    @SequenceGenerator(name = "source_ids_generator", sequenceName = "source_ids_sequence", allocationSize = 1)
    private Long id;

    @Column(name = "repository", nullable = false)
    private String repository;

    @Column(name = "hub", nullable = false)
    private String hub;

    @JsonRawValue
    @Column(name = "content", nullable = false, columnDefinition = "jsonb")
    private String content;

    @Id
    @Column(name = "raw_version", nullable = false)
    private Long rawVersion;

    @Id
    @Column(name = "curated_version", nullable = false)
    private Long curatedVersion;

}

package ma.imrane.benchmark.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
@Entity @Table(name="category") @Getter @Setter @NoArgsConstructor
public class Category {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,unique=true,length=32) private String code;
    @Column(nullable=false,length=128) private String name;
    private LocalDateTime updatedAt;
    @OneToMany(mappedBy="category") private List<Item> items=new ArrayList<>();
    @PrePersist @PreUpdate void stamp() { updatedAt=LocalDateTime.now(); }
}

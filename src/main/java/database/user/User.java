package database.user;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@Table(name = "\"user\"")
@NoArgsConstructor
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "pub_id", unique = true, nullable = false)
	private Integer pubId;
	@Column(name = "name", nullable = false)
	private String name;
	@Column(name = "blocked", nullable = false)
	private Boolean blocked;
}

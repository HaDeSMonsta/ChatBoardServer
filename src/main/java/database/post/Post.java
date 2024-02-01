package database.post;

import database.user.User;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "post")
public class Post {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "content", nullable = false)
	private String content;
	@ManyToOne
	@JoinColumn(name = "author_id", nullable = false)
	private User author;
	@Column(name = "upvotes")
	private String upvotes;
	@Column(name = "downvotes")
	private String downvotes;
}

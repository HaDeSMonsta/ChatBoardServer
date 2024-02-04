package app.database.log;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "log")
public class Log {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "time_stamp")
	private LocalDateTime timeStamp;

	@Column(name = "matr_num")
	private int matrNum;
}

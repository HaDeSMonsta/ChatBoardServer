package app.database.log;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface LogRepository extends JpaRepository<Log, Long> {

	@Query("Select min(l.timeStamp) as first, max(l.timeStamp) as last, count(l) as count " +
			"from Log l where l.matrNum = :matrNum")
	LogResult findFirstAndLastLogPerMatrNum(@Param("matrNum") int matrNum);

	@Query("Select count(l) from Log l where l.timeStamp between :startHour and :endHour")
	Long countLogsBetween(@Param("startHour") LocalDateTime startHour, @Param("endHour") LocalDateTime endHour);
}
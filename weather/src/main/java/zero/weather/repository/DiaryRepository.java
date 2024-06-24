package zero.weather.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zero.weather.domain.Diary;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {
    List<Diary> findAllByDate(LocalDate date);

    List<Diary> findAllByDateBetween(LocalDate fromDate, LocalDate toDate);

    Diary getFirstByDate(LocalDate date);

    @Transactional
    void deleteByDate(LocalDate date);
}

package zero.weather.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zero.weather.domain.DateWeather;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Repository
public interface DateWeatherRepository extends JpaRepository<DateWeather, Date> {
    List<DateWeather> getAllByDate(LocalDate date);
}

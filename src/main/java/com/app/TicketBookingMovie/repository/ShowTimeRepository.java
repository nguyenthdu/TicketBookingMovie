package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.Room;
import com.app.TicketBookingMovie.models.ShowTime;
import jakarta.validation.constraints.Past;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShowTimeRepository extends JpaRepository<ShowTime, Long> {


    Page<ShowTime> findByCode(String code, Pageable pageable);

    long countByCodeContaining(String code);


    //phải khớp với cả ngày và id phim
    Page<ShowTime> findByMovieIdAndShowDate(Long movieId, LocalDate date, Pageable pageable);

    @Query("select count(s) from ShowTime s where s.movie.id = ?1 and s.showDate = ?2")
    long countByMovieIdAndShowDate(Long movieId, LocalDate date);


    List<ShowTime> findByRoomAndShowDate(Room room, @Past LocalDate showDate);

    Page<ShowTime> findByMovieIdAndShowDateAndRoomId(Long movieId, LocalDate date, Long roomId, Pageable pageable);

    long countByCode(String code);

    long countByMovieIdAndShowDateAndRoomId(Long movieId, LocalDate date, Long roomId);
}

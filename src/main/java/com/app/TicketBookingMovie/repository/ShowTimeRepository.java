package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.Room;
import com.app.TicketBookingMovie.models.ShowTime;
import jakarta.validation.constraints.Past;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShowTimeRepository extends JpaRepository<ShowTime, Long> {

    long countByCodeContaining(String code);

    @Query("select count(s) from ShowTime s where s.movie.id = ?1 and s.showDate = ?2")
    long countByMovieIdAndShowDate(Long movieId, LocalDate date);

    List<ShowTime> findByRoomAndShowDate(Room room, @Past LocalDate showDate);

    long countByCode(String code);

    @Query("select count(s) from ShowTime s where s.movie.id = ?1 and s.room.cinema.id = ?2")
    long countByMovieIdAndCinemaId(Long movieId, Long cinemaId);

    @Query("select count(s) from ShowTime s where s.movie.id = ?1 and s.room.cinema.id = ?2 and s.room.id = ?3 and s.showDate = ?4")
    long countByMovieIdAndCinemaIdAndRoomIdAndShowDate(Long movieId, Long cinemaId, Long roomId, LocalDate date);

    @Query("select count(s) from ShowTime s where s.movie.id = ?1 and s.room.cinema.id = ?2 and s.room.id = ?3")
    long countByMovieIdAndCinemaIdAndRoomId(Long movieId, Long cinemaId, Long roomId);

    @Query("SELECT DISTINCT s.showDate FROM ShowTime s WHERE s.movie.id = ?1 AND s.room.cinema.id = ?2 AND s.showDate >= CURRENT_DATE")
    List<LocalDate> findDistinctShowDatesByMovieIdAndCinemaIdAfterToday(Long movieId, Long cinemaId);

    List<ShowTime> findAllByStatus(boolean b);
}

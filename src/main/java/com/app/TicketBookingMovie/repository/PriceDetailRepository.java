package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.PriceDetail;
import com.app.TicketBookingMovie.models.PriceHeader;
import com.app.TicketBookingMovie.models.enums.EDetailType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PriceDetailRepository extends JpaRepository<PriceDetail, Long> {


    @Query("SELECT pd FROM PriceDetail pd " +
            "JOIN pd.priceHeader ph " +
            "WHERE ph.startDate <= :currentTime AND ph.endDate >= :currentTime " +
            "AND pd.status = true")
    List<PriceDetail> findCurrentSalePriceDetails(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT pd FROM PriceDetail pd " +
            "JOIN pd.priceHeader ph " +
            "WHERE ph = :priceHeader " +
            "AND pd.type = :detailType " +
            "AND (pd.food.id = :itemId OR pd.room.id = :itemId OR pd.typeSeat.id = :itemId)")
    List<PriceDetail> findAllByPriceHeaderAndTypeAndItemId(@Param("priceHeader") PriceHeader priceHeader,
                                                           @Param("detailType") EDetailType detailType,
                                                           @Param("itemId") Long itemId);

    Page<PriceDetail> findAllByPriceHeaderId(Long priceHeaderId, Pageable pageable);

    @Query("SELECT pd FROM PriceDetail pd " +
            "JOIN pd.priceHeader ph " +
            "WHERE ph.id = :priceHeaderId " +
            "AND pd.type = :detailType")
    Page<PriceDetail> findAllByType(Long priceHeaderId, EDetailType detailType, Pageable pageable);

    @Query("SELECT pd FROM PriceDetail pd " +
            "JOIN pd.priceHeader ph " +
            "WHERE ph.id = :priceHeaderId " +
            "AND pd.food.code = :foodCode")
    Page<PriceDetail> findAllByFoodCode(Long priceHeaderId, String foodCode, Pageable pageable);

    @Query("SELECT pd FROM PriceDetail pd " +
            "JOIN pd.priceHeader ph " +
            "WHERE ph.id = :priceHeaderId " +
            "AND pd.room.code = :roomCode")
    Page<PriceDetail> findAllByRoomCode(Long priceHeaderId, String roomCode, Pageable pageable);

    @Query("SELECT pd FROM PriceDetail pd " +
            "JOIN pd.priceHeader ph " +
            "WHERE ph.id = :priceHeaderId " +
            "AND pd.typeSeat.code = :typeSeatCode")
    Page<PriceDetail> findAllByTypeSeatCode(Long priceHeaderId, String typeSeatCode, Pageable pageable);

    @Query("SELECT COUNT(pd) FROM PriceDetail pd " +
            "JOIN pd.priceHeader ph " +
            "WHERE ph.id = :priceHeaderId " +
            "AND pd.type = :detailType ")
    long countAllByType(Long priceHeaderId, EDetailType detailType);

    @Query("SELECT COUNT(pd) FROM PriceDetail pd " +
            "JOIN pd.priceHeader ph " +
            "WHERE ph.id = :priceHeaderId " +
            "AND pd.food.code = :foodCode")
    long countAllByFoodCode(Long priceHeaderId, String foodCode);

    @Query("SELECT COUNT(pd) FROM PriceDetail pd " +
            "JOIN pd.priceHeader ph " +
            "WHERE ph.id = :priceHeaderId " +
            "AND pd.room.code = :roomCode")
    long countAllByRoomCode(Long priceHeaderId, String roomCode);

    @Query("SELECT COUNT(pd) FROM PriceDetail pd " +
            "JOIN pd.priceHeader ph " +
            "WHERE ph.id = :priceHeaderId " +
            "AND pd.typeSeat.code = :typeSeatCode")
    long countAllByTypeSeatCode(Long priceHeaderId, String typeSeatCode);

    long countAllByPriceHeaderId(Long priceHeaderId);
}

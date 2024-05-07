package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.PriceDetailDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.*;
import com.app.TicketBookingMovie.models.enums.EDetailType;
import com.app.TicketBookingMovie.repository.PriceDetailRepository;
import com.app.TicketBookingMovie.services.*;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class PriceDetailServiceImpl implements PriceDetailService {
    private final PriceDetailRepository priceDetailRepository;
    private final ModelMapper modelMapper;
    private final PriceHeaderService priceHeaderService;
    private final FoodService foodService;
    private final TypeSeatService typeSeatService;
    private final RoomService roomService;


    public PriceDetailServiceImpl(PriceDetailRepository priceDetailRepository, ModelMapper modelMapper, PriceHeaderService priceHeaderService, FoodService foodService, TypeSeatService typeSeatService, RoomService roomService) {
        this.priceDetailRepository = priceDetailRepository;
        this.modelMapper = modelMapper;
        this.priceHeaderService = priceHeaderService;
        this.foodService = foodService;
        this.typeSeatService = typeSeatService;
        this.roomService = roomService;
    }


    @Override
    @Transactional
    public void createPriceDetail(Set<PriceDetailDto> priceDetailDtos) {
        // Lặp qua từng DTO để tạo PriceDetail
        for (PriceDetailDto priceDetailDto : priceDetailDtos) {
            // Lấy thông tin của chương trình thay đổi giá từ ID
            PriceHeader priceHeader = priceHeaderService.findById(priceDetailDto.getPriceHeaderId());
            // Kiểm tra xem chương trình thay đổi giá có còn hiệu lực hay không
            if (!priceHeader.getEndDate().isAfter(LocalDateTime.now())) {
                throw new AppException("Chương trình đã kết thúc, không thể thêm chi tiết chương trình", HttpStatus.BAD_REQUEST);
            }
            // Kiểm tra xem giá mới có hợp lệ không
            if (priceDetailDto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new AppException("Giá mới phải lớn hơn 0", HttpStatus.BAD_REQUEST);
            }

            // Tạo một đối tượng PriceDetail mới
            PriceDetail priceDetail = new PriceDetail();
            priceDetail.setPrice(priceDetailDto.getPrice());
            priceDetail.setPriceHeader(priceHeader);
            priceDetail.setStatus(false);
            priceDetail.setStatus(priceDetailDto.isStatus());
            priceDetail.setCreatedDate(LocalDateTime.now());

            // Thiết lập loại chi tiết dựa trên dữ liệu từ DTO
            switch (priceDetailDto.getType()) {
                case "FOOD":
                    // Nếu loại chi tiết là FOOD, kiểm tra xem đã tồn tại trong PriceHeader không
                    if (checkDuplicateItemInPriceHeader(priceHeader, EDetailType.FOOD, priceDetailDto.getFoodId())) {
                        throw new AppException("Sản phẩm đã tồn tại trong chương trình quản lý giá này", HttpStatus.BAD_REQUEST);
                    }
                    // Thiết lập thông tin sản phẩm
                    Food food = foodService.findById(priceDetailDto.getFoodId());
                    priceDetail.setType(EDetailType.FOOD);
                    priceDetail.setFood(food);
                    break;
                case "ROOM":
                    // Tương tự cho loại chi tiết ROOM
                    if (checkDuplicateItemInPriceHeader(priceHeader, EDetailType.ROOM, priceDetailDto.getRoomId())) {
                        throw new AppException("Sản phẩm đã tồn tại trong chương trình quản lý giá này", HttpStatus.BAD_REQUEST);
                    }
                    Room room = roomService.findById(priceDetailDto.getRoomId());
                    priceDetail.setType(EDetailType.ROOM);
                    priceDetail.setRoom(room);
                    break;
                case "TYPE_SEAT":
                    // Tương tự cho loại chi tiết TYPE_SEAT
                    if (checkDuplicateItemInPriceHeader(priceHeader, EDetailType.TYPE_SEAT, priceDetailDto.getTypeSeatId())) {
                        throw new AppException("Sản phẩm đã tồn tại trong chương trình quản lý giá này", HttpStatus.BAD_REQUEST);
                    }
                    TypeSeat typeSeat = typeSeatService.findById(priceDetailDto.getTypeSeatId());
                    priceDetail.setType(EDetailType.TYPE_SEAT);
                    priceDetail.setTypeSeat(typeSeat);
                    break;
                default:
                    throw new AppException("Loại chi tiết không hợp lệ", HttpStatus.BAD_REQUEST);
            }
            // Lưu thông tin chi tiết giá mới vào cơ sở dữ liệu
            priceDetailRepository.save(priceDetail);
        }
    }

    // Kiểm tra sự tồn tại của một sản phẩm trong PriceHeader
    private boolean checkDuplicateItemInPriceHeader(PriceHeader priceHeader, EDetailType detailType, Long itemId) {
        // Lấy danh sách PriceDetail trong PriceHeader có loại chi tiết và ID sản phẩm tương ứng
        List<PriceDetail> existingDetails = priceDetailRepository.findAllByPriceHeaderAndTypeAndItemId(priceHeader, detailType, itemId);
        return !existingDetails.isEmpty();
    }


    @Override
    public PriceDetailDto getPriceDetail(Long id) {
        PriceDetail priceDetail = priceDetailRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy chi tiết chương trình thay đổi giá với id: " + id, HttpStatus.NOT_FOUND));

        PriceDetailDto priceDetailDto = modelMapper.map(priceDetail, PriceDetailDto.class);
        switch (priceDetail.getType()) {
            case FOOD:
                priceDetailDto.setName(priceDetail.getFood().getName());
                priceDetailDto.setCode(priceDetail.getFood().getCode());
                break;
            case ROOM:
                priceDetailDto.setName(priceDetail.getRoom().getName());
                priceDetailDto.setCode(priceDetail.getRoom().getCode());
                break;
            case TYPE_SEAT:
                priceDetailDto.setName(String.valueOf(priceDetail.getTypeSeat().getName()));
                priceDetailDto.setCode(priceDetail.getTypeSeat().getCode());
                break;
        }
        return priceDetailDto;
    }

    @Override
    public PriceDetail getPriceDetailByFood(Food food) {
        //tìm price detail của food
        return priceDetailRepository.findByFood(food);
    }


    @Override
    public void updatePriceDetail(BigDecimal price, boolean status, Long id) {
        PriceDetail priceDetail = priceDetailRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy chi tiết  giá với id: " + id, HttpStatus.NOT_FOUND));
        //nếu ngày bắt đầu hoặc ngày kết thúc của header đã  qua thì không thể update giá

        if (priceDetail.getPriceHeader().getEndDate().isBefore(LocalDateTime.now())) {
            throw new AppException("Khoảng thời gian hoạt động của giá này đã kết thúc, không thể cập nhật chi tiết giá!!!", HttpStatus.BAD_REQUEST);
        }
        // Kiểm tra xem giá mới có hợp lệ không
        //nếu giá < 0 và không phải là số thực thì không được update
        if (price.compareTo(BigDecimal.ZERO) >= 0) {
            priceDetail.setPrice(price);
        } else {
            priceDetail.setPrice(priceDetail.getPrice());
        }
        if (priceDetail.isStatus() != status) {
            if (!priceDetail.getPriceHeader().isStatus() && status) {
                throw new AppException("Không thể kích hoạt giá khi chương trình quản lý giá của giá này chưa được kích hoạt!!!", HttpStatus.BAD_REQUEST);
            }
            priceDetail.setStatus(status);
        } else {
            priceDetail.setStatus(priceDetail.isStatus());
        }
        priceDetail.setCreatedDate(LocalDateTime.now());
        priceDetailRepository.save(priceDetail);
    }

    @Override
    public void deletePriceDetail(Long id) {
        PriceDetail priceDetail = priceDetailRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy chi tiết giá với id: " + id, HttpStatus.NOT_FOUND));
        //nếu ngày bắt đầu và kết thúc của Priceheader đã qua ngày hiện tại thì không được update
        if (priceDetail.getPriceHeader().getStartDate().isAfter(LocalDateTime.now())) {
            throw new AppException("Giá này nằm trong chương trình quản lý giá đang hoạt động, không thể xóa!!!", HttpStatus.BAD_REQUEST);
        }
        if (priceDetail.getPriceHeader().getEndDate().isBefore(LocalDateTime.now())) {
            throw new AppException("Giá này nằm trong chương trình quản lý giá đã kết thúc, không thể xóa!!!", HttpStatus.BAD_REQUEST);
        }
        if (priceDetail.isStatus()) {
            throw new AppException("Không thể xóa chi tiết giá đang hoạt động!!!", HttpStatus.BAD_REQUEST);
        }
        priceDetailRepository.delete(priceDetail);


    }

    @Override
    public List<PriceDetail> priceActive() {
        return priceDetailRepository.findCurrentSalePriceDetails(LocalDateTime.now());
    }

    @Override
    public List<PriceDetailDto> getAllPriceDetail(Integer page, Integer size, Long priceHeaderId, String typeDetail, String foodCode, String roomCode, String typeSeatCode) {
        List<PriceDetail> priceDetails = priceDetailRepository.findAll(Sort.by(Sort.Direction.DESC, "createdDate"));
        if (priceHeaderId != null) {
            if (typeDetail != null && !typeDetail.isEmpty()) {
                EDetailType detailType = EDetailType.valueOf(typeDetail);
                priceDetails = priceDetails.stream().filter(priceDetail -> priceDetail.getPriceHeader().getId().equals(priceHeaderId) && priceDetail.getType().equals(detailType)).toList();
            } else if (foodCode != null && !foodCode.isEmpty()) {
                priceDetails = priceDetails.stream().filter(priceDetail -> priceDetail.getPriceHeader().getId().equals(priceHeaderId) && priceDetail.getFood().getCode().equals(foodCode)).toList();
            } else if (roomCode != null && !roomCode.isEmpty()) {
                priceDetails = priceDetails.stream().filter(priceDetail -> priceDetail.getPriceHeader().getId().equals(priceHeaderId) && priceDetail.getRoom().getCode().equals(roomCode)).toList();
            } else if (typeSeatCode != null && !typeSeatCode.isEmpty()) {
                priceDetails = priceDetails.stream().filter(priceDetail -> priceDetail.getPriceHeader().getId().equals(priceHeaderId) && priceDetail.getTypeSeat().getCode().equals(typeSeatCode)).toList();
            } else {
                priceDetails = priceDetails.stream().filter(priceDetail -> priceDetail.getPriceHeader().getId().equals(priceHeaderId)).toList();
            }
        }

        int start = page * size;
        int end = Math.min(start + size, priceDetails.size());
        return priceDetails.subList(start, end).stream().map(priceDetail -> {
            PriceDetailDto priceDetailDto = modelMapper.map(priceDetail, PriceDetailDto.class);
            switch (priceDetail.getType()) {
                case FOOD:
                    priceDetailDto.setName(priceDetail.getFood().getName());
                    priceDetailDto.setCode(priceDetail.getFood().getCode());
                    break;
                case ROOM:
                    priceDetailDto.setName(priceDetail.getRoom().getName());
                    priceDetailDto.setCode(priceDetail.getRoom().getCode());
                    break;
                case TYPE_SEAT:
                    priceDetailDto.setName(String.valueOf(priceDetail.getTypeSeat().getName()));
                    priceDetailDto.setCode(priceDetail.getTypeSeat().getCode());
                    break;
            }
            return priceDetailDto;
        }).toList();

    }

    @Override
    public long countAllPriceDetail(Long priceHeaderId, String typeDetail, String foodCode, String roomCode, String typeSeatCode) {
        if (priceHeaderId != null) {
            if (typeDetail != null && !typeDetail.isEmpty()) {
                EDetailType detailType = EDetailType.valueOf(typeDetail);
                return priceDetailRepository.countAllByType(priceHeaderId, detailType);
            } else if (foodCode != null && !foodCode.isEmpty()) {
                return priceDetailRepository.countAllByFoodCode(priceHeaderId, foodCode);
            } else if (roomCode != null && !roomCode.isEmpty()) {
                return priceDetailRepository.countAllByRoomCode(priceHeaderId, roomCode);
            } else if (typeSeatCode != null && !typeSeatCode.isEmpty()) {
                return priceDetailRepository.countAllByTypeSeatCode(priceHeaderId, typeSeatCode);
            } else {
                return priceDetailRepository.countAllByPriceHeaderId(priceHeaderId);
            }
        } else {
            return priceDetailRepository.count();
        }

    }
}





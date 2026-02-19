package com.dailycodework.lakesidehotel.service;

import com.dailycodework.lakesidehotel.exception.InvalidBookingRequestException;
import com.dailycodework.lakesidehotel.exception.ResourceNotFoundException;
import com.dailycodework.lakesidehotel.model.BookedRoom;
import com.dailycodework.lakesidehotel.model.Room;
import com.dailycodework.lakesidehotel.repository.BookingRepository;
import com.dailycodework.lakesidehotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Simpson Alfred
 */

@Service
@RequiredArgsConstructor
public class BookingService implements IBookingService {
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;


    @Override
    public List<BookedRoom> getAllBookings() {
        return bookingRepository.findAll();
    }


    @Override
    public List<BookedRoom> getBookingsByUserEmail(String email) {
        return bookingRepository.findByGuestEmail(email);
    }

    @Override
    public void cancelBooking(Long bookingId) {
        bookingRepository.deleteById(bookingId);
    }

    @Override
    public List<BookedRoom> getAllBookingsByRoomId(Long roomId) {
        return bookingRepository.findByRoomId(roomId);
    }

    @Override
    @Transactional
    public String saveBooking(Long roomId, BookedRoom bookingRequest) {
        if (!bookingRequest.getCheckOutDate().isAfter(bookingRequest.getCheckInDate())) {
            throw new InvalidBookingRequestException("Check-out date must be after check-in date");
        }
        Room room = roomRepository.findByIdForUpdate(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Sorry, Room not found!"));
        boolean hasOverlap = bookingRepository.existsOverlappingBooking(
                roomId,
                bookingRequest.getCheckInDate(),
                bookingRequest.getCheckOutDate());
        if (hasOverlap) {
            throw new InvalidBookingRequestException("Sorry, This room is not available for the selected dates;");
        }
        room.addBooking(bookingRequest);
        bookingRepository.saveAndFlush(bookingRequest);
        return bookingRequest.getBookingConfirmationCode();
    }

    @Override
    public BookedRoom findByBookingConfirmationCode(String confirmationCode) {
        return bookingRepository.findByBookingConfirmationCode(confirmationCode)
                .orElseThrow(() -> new ResourceNotFoundException("No booking found with booking code :"+confirmationCode));

    }
}

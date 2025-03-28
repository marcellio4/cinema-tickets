package uk.gov.dwp.uc.pairtest;

import java.util.List;
import java.util.stream.Stream;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {

    private final SeatReservationService seatReservationService;
    private final TicketPaymentService ticketPaymentService;

    public TicketServiceImpl(SeatReservationService seatReservationService, TicketPaymentService ticketPaymentService) {
        this.seatReservationService = seatReservationService;
        this.ticketPaymentService = ticketPaymentService;
    }

    /**
     * Should only have private methods other than the one below.
     */

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        if (accountId < 1L) {
            throw new InvalidPurchaseException(
                    "Invalid account ID. Only numbers greater than zero are valid accounts.");
        }

        if (ticketTypeRequests.length > 25) {
            throw new InvalidPurchaseException("Too many requests. Max request size is 25.");
        }

        var adultTickets = getTicketTypes(Type.ADULT, ticketTypeRequests);

        if (adultTickets.isEmpty()) {
            throw new InvalidPurchaseException("Adult ticket is required when purchase child or infant ticket.");
        }

        var childTickets = getTicketTypes(Type.CHILD, ticketTypeRequests);
        var totalAmountPriceForAdults = calculateTotalAmountPrice(adultTickets);
        var totalAmountPriceForChildren = calculateTotalAmountPrice(childTickets);
        var totalTicketsForAdults = getSumNumberOfTickets(adultTickets);
        var totalTicketsForChildren = getSumNumberOfTickets(childTickets);

        ticketPaymentService.makePayment(accountId, totalAmountPriceForAdults + totalAmountPriceForChildren);
        seatReservationService.reserveSeat(accountId, totalTicketsForAdults + totalTicketsForChildren);
    }

    private List<TicketTypeRequest> getTicketTypes(TicketTypeRequest.Type type,
            TicketTypeRequest[] ticketTypeRequests) {
        return Stream.of(ticketTypeRequests)
                .filter(t -> t.getTicketType().equals(type))
                .toList();
    }

    private int calculateTotalAmountPrice(List<TicketTypeRequest> ticketTypeRequests) {
        if (ticketTypeRequests.isEmpty()) {
            return 0;
        }

        return ticketTypeRequests.stream()
                .mapToInt(ticketTypeRequest -> ticketTypeRequest.getTicketType().getPrice() * ticketTypeRequest.getNoOfTickets())
                .sum();
    }

    private int getSumNumberOfTickets(List<TicketTypeRequest> ticketTypeRequests) {
        if (ticketTypeRequests.isEmpty()) {
            return 0;
        }

        return ticketTypeRequests.stream()
                .mapToInt(ticketTypeRequest -> ticketTypeRequest.getNoOfTickets())
                .sum();
    }
}

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

@ExtendWith(MockitoExtension.class)
public class TicketServiceImplTest {

    @Mock
    private SeatReservationService seatReservationService;
    @Mock
    private TicketPaymentService ticketPaymentService;
    @Mock
    private TicketTypeRequest ticketTypeRequest;
    @InjectMocks
    private TicketServiceImpl ticketServiceImpl;

    @Test
    void shouldThrowInvalidPurchaseExceptionWhenTooManyRequests() {
        TicketTypeRequest[] requests = {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
        };

        var exception = assertThrows(InvalidPurchaseException.class, () -> {
            ticketServiceImpl.purchaseTickets(1L, requests);
        });
        
        assertEquals("Too many requests. Max request size is 25.", exception.getMessage());
    }

    @Test
    void shouldThrowInvalidPurchaseExceptionWhenticketsAreMissingAdults() {
        TicketTypeRequest[] requests = {
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
        };

        var exception = assertThrows(InvalidPurchaseException.class, () -> {
            ticketServiceImpl.purchaseTickets(1L, requests);
        });

        assertEquals("Adult ticket is required when purchase child or infant ticket.", exception.getMessage());

        verifyNoInteractions(ticketPaymentService, seatReservationService);
    }

    @Test
    void shouldThrowInvalidPurchaseExceptionWhenInvalidAccountNumber() {
        TicketTypeRequest[] requests = {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
        };

        var exception = assertThrows(InvalidPurchaseException.class, () -> {
            ticketServiceImpl.purchaseTickets(0L, requests);
        });

        assertEquals("Invalid account ID. Only numbers greater than zero are valid accounts.", exception.getMessage());

        verifyNoInteractions(ticketPaymentService, seatReservationService);
    }

    @Test
    void successfullProcessOfPruchaseTickets() {
        TicketTypeRequest[] requests = {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1),
        };

        doNothing().when(ticketPaymentService).makePayment(anyLong(), anyInt());
        doNothing().when(seatReservationService).reserveSeat(anyLong(), anyInt());

        ticketServiceImpl.purchaseTickets(1L, requests);

        verify(ticketPaymentService, times(1)).makePayment(1L, 65);
        verify(seatReservationService, times(1)).reserveSeat(1L, 3);
        verifyNoMoreInteractions(ticketPaymentService, seatReservationService);
    }

    @Test
    void successfullProcessOfPruchaseTicketsWhenOnlyAdultsAndInfantsArePresent() {
        TicketTypeRequest[] requests = {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1),
        };

        doNothing().when(ticketPaymentService).makePayment(anyLong(), anyInt());
        doNothing().when(seatReservationService).reserveSeat(anyLong(), anyInt());

        ticketServiceImpl.purchaseTickets(1L, requests);

        verify(ticketPaymentService, times(1)).makePayment(1L, 50);
        verify(seatReservationService, times(1)).reserveSeat(1L, 2);
        verifyNoMoreInteractions(ticketPaymentService, seatReservationService);
    }
}

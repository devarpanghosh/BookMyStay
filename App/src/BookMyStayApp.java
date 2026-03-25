import java.util.*;

/**
 * --- BOOK MY STAY APP: BOOKING QUEUE INTEGRATION ---
 * Features:
 * 1. Queue Data Structure: Uses a LinkedList (as a Queue) to store requests.
 * 2. FIFO Principle: Ensures the earliest request is processed first.
 * 3. Decoupling: Separates request intake from inventory mutation.
 */

// ==========================================
// 1. DOMAIN LAYER
// ==========================================

abstract class Room {
    private String roomType;
    private double pricePerNight;

    public Room(String roomType, double pricePerNight) {
        this.roomType = roomType;
        this.pricePerNight = pricePerNight;
    }

    public String getRoomType() { return roomType; }
}

class SingleRoom extends Room { public SingleRoom() { super("Single", 100.0); } }
class DoubleRoom extends Room { public DoubleRoom() { super("Double", 180.0); } }

/**
 * Represents a Guest's intent to book a room.
 */
class ReservationRequest {
    private String guestName;
    private String requestedRoomType;

    public ReservationRequest(String guestName, String requestedRoomType) {
        this.guestName = guestName;
        this.requestedRoomType = requestedRoomType;
    }

    @Override
    public String toString() {
        return "Request [Guest: " + guestName + ", Room: " + requestedRoomType + "]";
    }
}

// ==========================================
// 2. SERVICE LAYER (Booking Queue)
// ==========================================

/**
 * Manages the intake and ordering of incoming booking requests.
 */
class BookingQueue {
    // Using Queue interface with LinkedList for FIFO behavior
    private Queue<ReservationRequest> requestQueue = new LinkedList<>();

    /**
     * Adds a new request to the end of the queue.
     */
    public void submitRequest(ReservationRequest request) {
        requestQueue.add(request);
        System.out.println("Enqueued: " + request);
    }

    /**
     * Displays all pending requests in the order they arrived.
     */
    public void displayQueue() {
        System.out.println("\n--- Current Booking Queue (FIFO) ---");
        if (requestQueue.isEmpty()) {
            System.out.println("No pending requests.");
        } else {
            for (ReservationRequest req : requestQueue) {
                System.out.println(" >> " + req);
            }
        }
    }

    /**
     * Retrieves the next request to be processed (removes from head).
     */
    public ReservationRequest processNext() {
        return requestQueue.poll();
    }
}

// ==========================================
// 3. APPLICATION ENTRY POINT
// ==========================================

public class BookMyStayApp {

    public static void main(String[] args) {
        System.out.println("****************************************");
        System.out.println("   BOOK MY STAY - Request Intake System ");
        System.out.println("****************************************\n");

        BookingQueue bookingQueue = new BookingQueue();

        // Simulate multiple guests submitting requests at the same time
        System.out.println("Incoming Requests...");
        bookingQueue.submitRequest(new ReservationRequest("Alice", "Single"));
        bookingQueue.submitRequest(new ReservationRequest("Bob", "Double"));
        bookingQueue.submitRequest(new ReservationRequest("Charlie", "Single"));

        // Display the queue state to show arrival order preserved
        bookingQueue.displayQueue();

        // Demonstrate processing the first request in the queue
        System.out.println("\n[System] Ready to process the earliest request...");
        ReservationRequest next = bookingQueue.processNext();
        System.out.println("Processing: " + next);

        // Show remaining queue
        bookingQueue.displayQueue();

        System.out.println("\nApplication state preserved. No inventory mutation occurred.");
    }
}
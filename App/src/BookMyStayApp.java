import java.util.*;

/**
 * --- BOOK MY STAY APP: FINAL INTEGRATED VERSION ---
 * Features:
 * 1. Abstraction & Inheritance (Room Domain)
 * 2. Centralized Inventory (HashMap State)
 * 3. Read-Only Search (Safe Data Access)
 * 4. Request Queue (FIFO Fairness)
 * 5. Atomic Allocation (Sets for Double-Booking Prevention)
 * * @version 1.6 (Final)
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

    public void displayDetails() {
        System.out.printf("Type: %-10s | Price: $%.2f%n", roomType, pricePerNight);
    }
}

class SingleRoom extends Room { public SingleRoom() { super("Single", 100.0); } }
class DoubleRoom extends Room { public DoubleRoom() { super("Double", 180.0); } }

class ReservationRequest {
    private String guestName;
    private String requestedRoomType;

    public ReservationRequest(String guestName, String requestedRoomType) {
        this.guestName = guestName;
        this.requestedRoomType = requestedRoomType;
    }

    public String getGuestName() { return guestName; }
    public String getRequestedRoomType() { return requestedRoomType; }
}

// ==========================================
// 2. SERVICE LAYER (Inventory & Allocation)
// ==========================================

class BookingSystem {
    private Map<String, Integer> inventory = new HashMap<>();
    private Queue<ReservationRequest> requestQueue = new LinkedList<>();

    // Use Case 6: Tracking assigned IDs to prevent double-booking
    // Maps Room Type -> Set of Unique Room IDs
    private Map<String, Set<String>> assignedRooms = new HashMap<>();

    public void addInventory(Room room, int count) {
        inventory.put(room.getRoomType(), count);
        assignedRooms.putIfAbsent(room.getRoomType(), new HashSet<>());
    }

    public void enqueueRequest(ReservationRequest request) {
        requestQueue.add(request);
        System.out.println("[Queue] Added request for: " + request.getGuestName());
    }

    /**
     * Processes the next request in FIFO order with Atomic Allocation logic.
     */
    public void processNextBooking() {
        ReservationRequest request = requestQueue.poll();
        if (request == null) return;

        String type = request.getRequestedRoomType();
        int available = inventory.getOrDefault(type, 0);

        System.out.println("\n[Processing] Guest: " + request.getGuestName() + " for " + type);

        if (available > 0) {
            // Atomic Operation: Generate ID, Record it, and Decrement Inventory
            String roomID = type.toUpperCase() + "-" + (100 + assignedRooms.get(type).size() + 1);

            assignedRooms.get(type).add(roomID); // Prevents reuse of ID
            inventory.put(type, available - 1);  // Decrement immediately

            System.out.println(">> SUCCESS: Reservation Confirmed.");
            System.out.println(">> Assigned Room ID: " + roomID);
        } else {
            System.out.println(">> FAILED: No " + type + " rooms available.");
        }
    }

    public void displayStatus() {
        System.out.println("\n--- Current System State ---");
        inventory.forEach((type, count) -> System.out.println(type + " Available: " + count));
    }
}

// ==========================================
// 3. APPLICATION ENTRY POINT
// ==========================================

public class BookMyStayApp {
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("      BOOK MY STAY - VERSION 1.6       ");
        System.out.println("========================================\n");

        BookingSystem system = new BookingSystem();

        // Setup
        Room single = new SingleRoom();
        system.addInventory(single, 1); // Only 1 single room available

        // Use Case 5: Preserve Arrival Order (Alice then Bob)
        system.enqueueRequest(new ReservationRequest("Alice", "Single"));
        system.enqueueRequest(new ReservationRequest("Bob", "Single"));

        // Use Case 6: Safe Allocation
        system.processNextBooking(); // Alice gets the room
        system.processNextBooking(); // Bob fails (Inventory consistent)

        system.displayStatus();
        System.out.println("\nThank you for using Book My Stay!");
    }
}
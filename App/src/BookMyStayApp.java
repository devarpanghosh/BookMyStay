import java.util.*;

/**
 * --- BOOK MY STAY APP: FULL SYSTEM INTEGRATION ---
 * This final version implements the complete lifecycle:
 * 1. Entry Point & Domain (UC 1, 2)
 * 2. Centralized HashMap Inventory (UC 3)
 * 3. Read-Only Search (UC 4)
 * 4. FIFO Request Queueing (UC 5)
 * 5. Atomic Room ID Assignment (UC 6)
 * 6. Add-On Service Composition (UC 7)
 * 7. Historical Reporting (UC 8)
 */

// ==========================================
// 1. DOMAIN LAYER (Abstraction & Inheritance)
// ==========================================

abstract class Room {
    private String roomType;
    private double pricePerNight;

    public Room(String roomType, double pricePerNight) {
        this.roomType = roomType;
        this.pricePerNight = pricePerNight;
    }

    public String getRoomType() { return roomType; }
    public double getPrice() { return pricePerNight; }
}

class SingleRoom extends Room { public SingleRoom() { super("Single", 100.0); } }
class DoubleRoom extends Room { public DoubleRoom() { super("Double", 180.0); } }

class AddOnService {
    private String name;
    private double cost;
    public AddOnService(String name, double cost) { this.name = name; this.cost = cost; }
    public String getName() { return name; }
    public double getCost() { return cost; }
}

// ==========================================
// 2. STATE & SERVICE LAYER
// ==========================================

class HotelSystem {
    // Inventory (UC 3)
    private Map<String, Integer> inventory = new HashMap<>();
    // Request Queue (UC 5)
    private Queue<ReservationRequest> requestQueue = new LinkedList<>();
    // Double-Booking Prevention (UC 6)
    private Map<String, Set<String>> assignedRoomIDs = new HashMap<>();
    // Historical Tracking (UC 8)
    private List<String> bookingHistory = new ArrayList<>();
    // Add-On Mapping (UC 7)
    private Map<String, List<AddOnService>> reservationServices = new HashMap<>();

    public void addInventory(Room room, int count) {
        inventory.put(room.getRoomType(), count);
        assignedRoomIDs.putIfAbsent(room.getRoomType(), new HashSet<>());
    }

    public void enqueueRequest(String guest, String type) {
        requestQueue.add(new ReservationRequest(guest, type));
    }

    /**
     * Use Case 6 & 8: Processes bookings and records history.
     */
    public void processAllRequests() {
        while (!requestQueue.isEmpty()) {
            ReservationRequest req = requestQueue.poll();
            String type = req.requestedRoomType;
            int count = inventory.getOrDefault(type, 0);

            if (count > 0) {
                // Atomic ID Generation
                String roomID = type.toUpperCase() + "-" + (assignedRoomIDs.get(type).size() + 101);
                assignedRoomIDs.get(type).add(roomID);
                inventory.put(type, count - 1);
                reservationServices.put(roomID, new ArrayList<>());

                String record = "Guest: " + req.guestName + " | Room: " + roomID;
                bookingHistory.add(record);
                System.out.println("[Confirmed] " + record);
            } else {
                System.out.println("[Failed] No rooms left for " + req.guestName);
            }
        }
    }

    public void addService(String roomID, AddOnService service) {
        if (reservationServices.containsKey(roomID)) {
            reservationServices.get(roomID).add(service);
        }
    }

    /**
     * Use Case 8: Reporting Service
     */
    public void displayReport() {
        System.out.println("\n--- FINAL OPERATIONAL REPORT ---");
        System.out.println("Total Transactions: " + bookingHistory.size());
        bookingHistory.forEach(entry -> System.out.println("History: " + entry));
    }

    // Helper class for requests
    private static class ReservationRequest {
        String guestName;
        String requestedRoomType;
        ReservationRequest(String g, String t) { this.guestName = g; this.requestedRoomType = t; }
    }
}

// ==========================================
// 3. APPLICATION ENTRY POINT
// ==========================================

public class BookMyStayApp {
    public static void main(String[] args) {
        // UC 1: Metadata
        System.out.println("Welcome to Book My Stay App v1.8\n");

        HotelSystem hotel = new HotelSystem();

        // UC 2 & 3: Initialization
        hotel.addInventory(new SingleRoom(), 1);
        hotel.addInventory(new DoubleRoom(), 5);

        // UC 5: Fair Queueing
        hotel.enqueueRequest("Alice", "Single");
        hotel.enqueueRequest("Bob", "Single"); // Should fail (only 1 single room)

        // UC 6: Atomic Allocation
        hotel.processAllRequests();

        // UC 7: Optional Services
        // Assuming we knew the ID from output (e.g., SINGLE-101)
        hotel.addService("SINGLE-101", new AddOnService("Late Checkout", 20.0));

        // UC 8: Reporting
        hotel.displayReport();

        System.out.println("\n[Shutdown] System execution finished.");
    }
}
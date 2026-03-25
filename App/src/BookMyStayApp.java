import java.util.*;

/**
 * --- BOOK MY STAY APP: FINAL SYSTEM ARCHITECTURE ---
 * Features:
 * - Abstraction, Inheritance, Encapsulation (UC 2)
 * - HashMap Centralized Inventory (UC 3)
 * - FIFO Booking Queue (UC 5)
 * - Atomic Allocation with Unique IDs (UC 6)
 * - One-to-Many Add-On Services (UC 7)
 * - Historical Audit Reporting (UC 8)
 * - Fail-Fast Validation (UC 9)
 * - LIFO Cancellation Rollback (UC 10)
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
// 2. CORE BOOKING ENGINE
// ==========================================

class HotelBookingManager {
    private Map<String, Integer> inventory = new HashMap<>();
    private List<String> history = new ArrayList<>();
    private Map<String, List<AddOnService>> addOns = new HashMap<>();

    // Use Case 10: Stack for Rollback behavior
    private Stack<String> lastAllocatedIDs = new Stack<>();

    public void addInventory(Room room, int count) {
        inventory.put(room.getRoomType().toLowerCase(), count);
    }

    /**
     * Use Case 6 & 9: Atomic Allocation with Validation
     */
    public String bookRoom(String guest, String type) throws Exception {
        String key = type.toLowerCase();

        if (!inventory.containsKey(key)) throw new Exception("Invalid Room Type");
        if (inventory.get(key) <= 0) throw new Exception("Room Out of Stock");

        String resID = type.toUpperCase() + "-" + (100 + history.size() + 1);

        // Update State
        inventory.put(key, inventory.get(key) - 1);
        lastAllocatedIDs.push(resID); // Track for rollback
        addOns.put(resID, new ArrayList<>());

        String log = "Guest: " + guest + " | ID: " + resID;
        history.add(log);

        return resID;
    }

    /**
     * Use Case 10: Cancellation Service (LIFO Rollback)
     */
    public void cancelLastBooking() {
        if (lastAllocatedIDs.isEmpty()) {
            System.out.println("[Cancel] No reservations to rollback.");
            return;
        }

        String resID = lastAllocatedIDs.pop(); // Get most recent
        String type = resID.split("-")[0].toLowerCase();

        // Revert Inventory
        inventory.put(type, inventory.get(type) + 1);
        history.add("CANCELLED: " + resID);

        System.out.println("[Cancel] Successfully rolled back " + resID);
    }

    public void showReport() {
        System.out.println("\n--- Audit Trail ---");
        history.forEach(System.out::println);
    }
}

// ==========================================
// 3. APPLICATION ENTRY POINT
// ==========================================

public class BookMyStayApp {
    public static void main(String[] args) {
        // Use Case 1: Entry Point
        System.out.println("BOOK MY STAY v1.10 - Final Integrated System\n");

        HotelBookingManager hotel = new HotelBookingManager();
        hotel.addInventory(new SingleRoom(), 5);

        try {
            // Process a booking
            String id1 = hotel.bookRoom("Alice", "Single");
            System.out.println("Confirmed: " + id1);

            // Use Case 10: Demonstrate safe cancellation
            hotel.cancelLastBooking();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }

        hotel.showReport();
    }
}
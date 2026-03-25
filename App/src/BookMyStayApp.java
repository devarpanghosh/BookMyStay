import java.util.*;

/**
 * --- BOOK MY STAY APP: FINAL ROBUST INTEGRATION ---
 * This version adds Use Case 9: Structured Validation & Error Handling.
 * Key Concept: Fail-Fast Design via Custom Exceptions.
 */

// ==========================================
// 1. CUSTOM EXCEPTIONS (Use Case 9)
// ==========================================

class InvalidBookingException extends Exception {
    public InvalidBookingException(String message) {
        super(message);
    }
}

class RoomUnavailableException extends Exception {
    public RoomUnavailableException(String message) {
        super(message);
    }
}

// ==========================================
// 2. DOMAIN LAYER
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

// ==========================================
// 3. CORE SYSTEM (Inventory & Allocation)
// ==========================================

class BookingSystem {
    private Map<String, Integer> inventory = new HashMap<>();
    private Map<String, Set<String>> assignedRoomIDs = new HashMap<>();
    private List<String> bookingHistory = new ArrayList<>();

    public void addInventory(Room room, int count) {
        inventory.put(room.getRoomType().toLowerCase(), count);
        assignedRoomIDs.putIfAbsent(room.getRoomType().toLowerCase(), new HashSet<>());
    }

    /**
     * Use Case 9: Process booking with robust validation.
     * Demonstrates Fail-Fast logic.
     */
    public void processBooking(String guest, String type) throws InvalidBookingException, RoomUnavailableException {
        String searchKey = type.toLowerCase();

        // 1. Validate Input (Input Validation)
        if (!inventory.containsKey(searchKey)) {
            throw new InvalidBookingException("Error: Room type '" + type + "' does not exist in our catalog.");
        }

        // 2. Guard System State (Constraint Check)
        int available = inventory.get(searchKey);
        if (available <= 0) {
            throw new RoomUnavailableException("Error: No availability left for " + type + " rooms.");
        }

        // 3. Atomic Logic (If validation passes)
        String roomID = type.toUpperCase() + "-" + (assignedRoomIDs.get(searchKey).size() + 101);
        assignedRoomIDs.get(searchKey).add(roomID);
        inventory.put(searchKey, available - 1);

        bookingHistory.add("Guest: " + guest + " | Room: " + roomID);
        System.out.println("[Confirmed] Reservation successful for " + guest + " (" + roomID + ")");
    }

    public void displayReport() {
        System.out.println("\n--- SYSTEM AUDIT REPORT ---");
        if (bookingHistory.isEmpty()) System.out.println("No confirmed bookings.");
        bookingHistory.forEach(System.out::println);
        System.out.println("---------------------------\n");
    }
}

// ==========================================
// 4. APPLICATION ENTRY POINT
// ==========================================

public class BookMyStayApp {
    public static void main(String[] args) {
        System.out.println("BOOK MY STAY v1.9 - Robust Validation System\n");

        BookingSystem hotel = new BookingSystem();
        hotel.addInventory(new SingleRoom(), 1); // Only 1 Single room available

        // Array of test scenarios: Valid, Invalid Type, and Out of Stock
        String[][] testRequests = {
                {"Alice", "Single"},   // Valid
                {"Bob", "Penthouse"},  // Invalid Type (Triggers InvalidBookingException)
                {"Charlie", "Single"}  // Out of Stock (Triggers RoomUnavailableException)
        };

        for (String[] req : testRequests) {
            try {
                System.out.println("[Request] Guest: " + req[0] + " | Type: " + req[1]);
                hotel.processBooking(req[0], req[1]);
            } catch (InvalidBookingException | RoomUnavailableException e) {
                // Graceful Failure Handling
                System.err.println(">> FAILURE: " + e.getMessage());
            } catch (Exception e) {
                System.err.println(">> UNEXPECTED ERROR: " + e.getMessage());
            }
        }

        hotel.displayReport();
        System.out.println("System execution completed safely.");
    }
}
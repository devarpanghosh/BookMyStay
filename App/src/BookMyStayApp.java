import java.util.HashMap;
import java.util.Map;

/**
 * --- BOOK MY STAY APP: FULL INTEGRATION ---
 * This version combines:
 * 1. Application Entry & Metadata (Use Case 1)
 * 2. Room Abstraction & Inheritance (Use Case 2)
 * 3. Centralized Inventory via HashMap (Use Case 3)
 * * @author [Your Name]
 * @version 1.2
 */

// ==========================================
// 1. DOMAIN LAYER (Abstraction & Inheritance)
// ==========================================

abstract class Room {
    private String roomType;
    private int numberOfBeds;
    private double pricePerNight;

    public Room(String roomType, int numberOfBeds, double pricePerNight) {
        this.roomType = roomType;
        this.numberOfBeds = numberOfBeds;
        this.pricePerNight = pricePerNight;
    }

    public String getRoomType() { return roomType; }

    public void displayDetails() {
        System.out.printf("Type: %-10s | Beds: %d | Price: $%.2f%n",
                roomType, numberOfBeds, pricePerNight);
    }
}

class SingleRoom extends Room { public SingleRoom() { super("Single", 1, 100.0); } }
class DoubleRoom extends Room { public DoubleRoom() { super("Double", 2, 180.0); } }
class SuiteRoom  extends Room { public SuiteRoom()  { super("Suite",  4, 350.0); } }

// ==========================================
// 2. STATE LAYER (Centralized Management)
// ==========================================

/**
 * Manages room counts using a HashMap to provide O(1) lookup.
 */
class RoomInventory {
    private Map<String, Integer> inventory;

    public RoomInventory() {
        this.inventory = new HashMap<>();
    }

    public void registerRoom(Room room, int initialCount) {
        inventory.put(room.getRoomType(), initialCount);
    }

    public void updateAvailability(String roomType, int change) {
        if (inventory.containsKey(roomType)) {
            int current = inventory.get(roomType);
            inventory.put(roomType, Math.max(0, current + change));
        }
    }

    public void showStatus() {
        System.out.println("\n--- Current Inventory Status ---");
        inventory.forEach((type, count) ->
                System.out.println("Room: " + type + " | Available: " + count));
    }
}

// ==========================================
// 3. APPLICATION ENTRY POINT
// ==========================================

public class BookMyStayApp {

    public static void main(String[] args) {
        // --- Use Case 1: Welcome Message ---
        System.out.println("****************************************");
        System.out.println("   Welcome to BOOK MY STAY v1.2         ");
        System.out.println("   Hotel Management System Initialized  ");
        System.out.println("****************************************\n");

        // --- Use Case 2: Object Initialization ---
        Room single = new SingleRoom();
        Room doubleRm = new DoubleRoom();
        Room suite = new SuiteRoom();

        // --- Use Case 3: Inventory Setup ---
        RoomInventory hotelInventory = new RoomInventory();

        // Registering room types into the Centralized HashMap
        hotelInventory.registerRoom(single, 10);
        hotelInventory.registerRoom(doubleRm, 5);
        hotelInventory.registerRoom(suite, 2);

        // Display Room Specs
        System.out.println("Available Room Types:");
        single.displayDetails();
        doubleRm.displayDetails();
        suite.displayDetails();

        // Show Initial Inventory
        hotelInventory.showStatus();

        // Simulate a Transaction (Booking)
        System.out.println("\n[Action] Processing booking for 1 Suite...");
        hotelInventory.updateAvailability("Suite", -1);

        // Show Updated Inventory
        hotelInventory.showStatus();

        System.out.println("\nApplication execution finished successfully.");
    }
}
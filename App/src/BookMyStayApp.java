import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * --- BOOK MY STAY APP: SEARCH SERVICE INTEGRATION ---
 * This version introduces:
 * 1. Read-Only Search Service (Use Case 4)
 * 2. Defensive Programming (Filtering unavailable rooms)
 * 3. Separation of Concerns (Search logic isolated from Inventory updates)
 */

// ==========================================
// 1. DOMAIN LAYER
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
        System.out.printf("%-10s | Beds: %d | Price: $%.2f",
                roomType, numberOfBeds, pricePerNight);
    }
}

class SingleRoom extends Room { public SingleRoom() { super("Single", 1, 100.0); } }
class DoubleRoom extends Room { public DoubleRoom() { super("Double", 2, 180.0); } }
class SuiteRoom  extends Room { public SuiteRoom()  { super("Suite",  4, 350.0); } }

// ==========================================
// 2. STATE LAYER (Inventory Holder)
// ==========================================

class RoomInventory {
    private Map<String, Integer> inventory = new HashMap<>();

    public void registerRoom(Room room, int initialCount) {
        inventory.put(room.getRoomType(), initialCount);
    }

    // Read-only access for the Search Service
    public int getCount(String roomType) {
        return inventory.getOrDefault(roomType, 0);
    }

    public void updateAvailability(String roomType, int change) {
        if (inventory.containsKey(roomType)) {
            int current = inventory.get(roomType);
            inventory.put(roomType, Math.max(0, current + change));
        }
    }
}

// ==========================================
// 3. SERVICE LAYER (Search Service)
// ==========================================

/**
 * Handles read-only search requests without modifying system state.
 */
class SearchService {
    /**
     * Filters and displays only available room types.
     */
    public void searchAvailableRooms(List<Room> roomCatalog, RoomInventory inventory) {
        System.out.println("\n--- SEARCH RESULTS (Available Rooms Only) ---");
        boolean found = false;

        for (Room room : roomCatalog) {
            int availableCount = inventory.getCount(room.getRoomType());

            // Validation Logic: Defensive check for availability > 0
            if (availableCount > 0) {
                room.displayDetails();
                System.out.println(" | Available: " + availableCount);
                found = true;
            }
        }

        if (!found) {
            System.out.println("Sorry, no rooms are currently available.");
        }
        System.out.println("----------------------------------------------");
    }
}

// ==========================================
// 4. APPLICATION ENTRY POINT
// ==========================================

public class BookMyStayApp {

    public static void main(String[] args) {
        // Initialization
        System.out.println("Welcome to BOOK MY STAY v1.3\n");

        RoomInventory hotelInventory = new RoomInventory();
        SearchService searchService = new SearchService();

        // Setup Catalog and Inventory
        List<Room> roomCatalog = new ArrayList<>();
        roomCatalog.add(new SingleRoom());
        roomCatalog.add(new DoubleRoom());
        roomCatalog.add(new SuiteRoom());

        hotelInventory.registerRoom(roomCatalog.get(0), 5); // 5 Single Rooms
        hotelInventory.registerRoom(roomCatalog.get(1), 0); // 0 Double Rooms (Sold Out)
        hotelInventory.registerRoom(roomCatalog.get(2), 2); // 2 Suite Rooms

        // --- GUEST INTERACTION ---
        System.out.println("[Guest] Initiating room search...");

        // This operation is Read-Only; system state remains unchanged
        searchService.searchAvailableRooms(roomCatalog, hotelInventory);

        // Verification: The Double room was hidden because count was 0
        System.out.println("\nSearch complete. System state remains consistent.");
    }
}
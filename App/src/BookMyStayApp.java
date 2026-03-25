import java.util.*;

/**
 * --- BOOK MY STAY APP: FULL BUSINESS MODEL ---
 * Integrates: Inheritance, HashMaps, Queues, Sets, and Add-On Services.
 * * @version 1.7 (Final Extensible Version)
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

/**
 * Use Case 7: Add-On Service Model
 */
class AddOnService {
    private String serviceName;
    private double cost;

    public AddOnService(String serviceName, double cost) {
        this.serviceName = serviceName;
        this.cost = cost;
    }

    public String getServiceName() { return serviceName; }
    public double getCost() { return cost; }
}

// ==========================================
// 2. SERVICE LAYER (Inventory & Allocation)
// ==========================================

class BookingManager {
    private Map<String, Integer> inventory = new HashMap<>();
    private Queue<String> requestQueue = new LinkedList<>();

    // Use Case 6: Atomic Allocation tracking
    private Map<String, Set<String>> assignedRoomIDs = new HashMap<>();

    // Use Case 7: One-to-Many Relationship (Reservation ID -> List of Services)
    private Map<String, List<AddOnService>> reservationAddOns = new HashMap<>();

    public void setupInventory(Room room, int count) {
        inventory.put(room.getRoomType(), count);
        assignedRoomIDs.putIfAbsent(room.getRoomType(), new HashSet<>());
    }

    /**
     * Use Case 6 & 7: Allocates room and initializes add-on list
     */
    public String processBooking(String guestName, String roomType) {
        int available = inventory.getOrDefault(roomType, 0);

        if (available > 0) {
            String resID = roomType.toUpperCase() + "-" + (assignedRoomIDs.get(roomType).size() + 101);
            assignedRoomIDs.get(roomType).add(resID);
            inventory.put(roomType, available - 1);

            // Initialize empty add-on list for this reservation
            reservationAddOns.put(resID, new ArrayList<>());

            System.out.println("[System] Confirmed: " + guestName + " assigned to " + resID);
            return resID;
        }
        System.out.println("[System] Failed: No availability for " + roomType);
        return null;
    }

    /**
     * Use Case 7: Attaches services to a reservation
     */
    public void addServiceToReservation(String resID, AddOnService service) {
        if (reservationAddOns.containsKey(resID)) {
            reservationAddOns.get(resID).add(service);
            System.out.println("[Service] Added " + service.getServiceName() + " to " + resID);
        }
    }

    /**
     * Use Case 7: Cost Aggregation logic
     */
    public void generateInvoice(String resID, double basePrice) {
        System.out.println("\n--- INVOICE FOR " + resID + " ---");
        System.out.printf("Base Room Price: $%.2f%n", basePrice);

        double totalAddOnCost = 0;
        List<AddOnService> services = reservationAddOns.getOrDefault(resID, new ArrayList<>());

        for (AddOnService s : services) {
            System.out.printf("+ %-10s: $%.2f%n", s.getServiceName(), s.getCost());
            totalAddOnCost += s.getCost();
        }

        System.out.println("---------------------------");
        System.out.printf("TOTAL BILL: $%.2f%n", (basePrice + totalAddOnCost));
    }
}

// ==========================================
// 3. APPLICATION ENTRY POINT
// ==========================================

public class BookMyStayApp {
    public static void main(String[] args) {
        System.out.println("BOOK MY STAY - VERSION 1.7 (Full Business Logic)\n");

        BookingManager manager = new BookingManager();

        // 1. Setup
        Room single = new SingleRoom();
        manager.setupInventory(single, 5);

        // 2. Execution Flow (Booking -> Add-Ons -> Invoice)
        String myResID = manager.processBooking("Alice", "Single");

        if (myResID != null) {
            // Guest selects optional services
            manager.addServiceToReservation(myResID, new AddOnService("Breakfast", 15.0));
            manager.addServiceToReservation(myResID, new AddOnService("WiFi", 10.0));

            // Final Bill Calculation
            manager.generateInvoice(myResID, single.getPrice());
        }

        System.out.println("\nSystem state consistent. Core logic preserved.");
    }
}
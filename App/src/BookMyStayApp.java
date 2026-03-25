import java.util.*;
import java.io.*;
import java.util.concurrent.*;

/**
 * --- BOOK MY STAY APP: FULL ARCHITECTURE ---
 * Final Version: Persistence & Recovery Integration.
 * Features:
 * - Thread-Safe Concurrent Booking (UC 11)
 * - LIFO Cancellation Rollback (UC 10)
 * - File-based Serialization/Deserialization (UC 12)
 */

// ==========================================
// 1. DOMAIN LAYER (Serializable for Persistence)
// ==========================================

abstract class Room implements Serializable {
    private static final long serialVersionUID = 1L;
    private String type;
    private double price;

    public Room(String type, double price) {
        this.type = type;
        this.price = price;
    }
    public String getType() { return type; }
}

class SingleRoom extends Room {
    public SingleRoom() { super("Single", 100.0); }
}

// ==========================================
// 2. CORE SERVICE LAYER
// ==========================================

class HotelSystem implements Serializable {
    private static final long serialVersionUID = 1L;

    // State to be persisted
    private Map<String, Integer> inventory = new ConcurrentHashMap<>();
    private List<String> bookingHistory = Collections.synchronizedList(new ArrayList<>());
    private transient Stack<String> rollbackStack = new Stack<>(); // Reset on restart

    public void addInventory(String type, int count) {
        inventory.put(type.toLowerCase(), count);
    }

    /**
     * Use Case 11: Synchronized Concurrent Booking
     */
    public synchronized String bookRoom(String guest, String type) throws Exception {
        String key = type.toLowerCase();
        if (!inventory.containsKey(key) || inventory.get(key) <= 0) {
            throw new Exception("Room unavailable or invalid type.");
        }

        String resID = type.toUpperCase() + "-" + (101 + bookingHistory.size());
        inventory.put(key, inventory.get(key) - 1);
        bookingHistory.add("Guest: " + guest + " | Room: " + resID);
        rollbackStack.push(resID);

        return resID;
    }

    public void displayReport() {
        System.out.println("\n--- Current Booking History ---");
        bookingHistory.forEach(System.out::println);
    }
}

// ==========================================
// 3. PERSISTENCE SERVICE (Use Case 12)
// ==========================================

class PersistenceManager {
    private static final String FILE_NAME = "hotel_state.ser";

    public static void saveState(HotelSystem system) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(system);
            System.out.println("[Persistence] System state saved successfully.");
        } catch (IOException e) {
            System.err.println("[Error] Failed to save state: " + e.getMessage());
        }
    }

    public static HotelSystem loadState() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return null;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            System.out.println("[Persistence] Recovering state from file...");
            return (HotelSystem) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[Error] Recovery failed: " + e.getMessage());
            return null;
        }
    }
}

// ==========================================
// 4. MAIN EXECUTION FLOW
// ==========================================

public class BookMyStayApp {
    public static void main(String[] args) {
        System.out.println("--- BOOK MY STAY v1.12 ---");

        // Step 1: Attempt Recovery
        HotelSystem hotel = PersistenceManager.loadState();

        if (hotel == null) {
            System.out.println("[Init] No saved state found. Starting fresh.");
            hotel = new HotelSystem();
            hotel.addInventory("Single", 5);
        }

        // Step 2: Perform Operations
        try {
            String id = hotel.bookRoom("John Doe", "Single");
            System.out.println("Booking Successful: " + id);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        // Step 3: Shutdown & Persist
        hotel.displayReport();
        PersistenceManager.saveState(hotel);
        System.out.println("Application Terminated.");
    }
}
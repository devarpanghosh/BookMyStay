import java.util.*;
import java.util.concurrent.*;

/**
 * --- BOOK MY STAY APP: FINAL ROBUST & CONCURRENT VERSION ---
 * This version integrates Use Case 11: Multi-threaded Synchronization.
 * Key Concept: Thread Safety and Critical Sections to prevent Double Allocation.
 */

// ==========================================
// 1. CUSTOM EXCEPTIONS
// ==========================================

class BookingException extends Exception {
    public BookingException(String message) { super(message); }
}

// ==========================================
// 2. DOMAIN LAYER
// ==========================================

abstract class Room {
    private String type;
    private double price;

    public Room(String type, double price) {
        this.type = type;
        this.price = price;
    }

    public String getType() { return type; }
    public double getPrice() { return price; }
}

class SingleRoom extends Room { public SingleRoom() { super("Single", 100.0); } }
class DoubleRoom extends Room { public DoubleRoom() { super("Double", 180.0); } }

// ==========================================
// 3. CORE CONCURRENT SYSTEM
// ==========================================

class HotelSystem {
    // Shared Mutable State
    private final Map<String, Integer> inventory = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> allocatedIDs = new ConcurrentHashMap<>();
    private final List<String> auditTrail = Collections.synchronizedList(new ArrayList<>());

    // Stack for LIFO Rollback (UC 10)
    private final Stack<String> rollbackStack = new Stack<>();

    public void initializeInventory(Room room, int count) {
        inventory.put(room.getType().toLowerCase(), count);
        allocatedIDs.put(room.getType().toLowerCase(), ConcurrentHashMap.newKeySet());
    }

    /**
     * Use Case 11: Synchronized Room Allocation
     * Uses a synchronized block to create a 'Critical Section'.
     */
    public String processConcurrentBooking(String guest, String type) throws BookingException {
        String key = type.toLowerCase();

        // Critical Section: Only one thread can execute this block at a time
        synchronized (this) {
            if (!inventory.containsKey(key)) {
                throw new BookingException("Invalid Room Type: " + type); // UC 9
            }

            int currentCount = inventory.get(key);
            if (currentCount <= 0) {
                throw new BookingException("Sold Out: " + type); // UC 9
            }

            // Atomic Updates to prevent Race Conditions
            String roomID = type.toUpperCase() + "-" + (101 + allocatedIDs.get(key).size());
            inventory.put(key, currentCount - 1);
            allocatedIDs.get(key).add(roomID);
            rollbackStack.push(roomID);

            String entry = "[SUCCESS] Guest: " + guest + " | Room: " + roomID;
            auditTrail.add(entry);
            return roomID;
        }
    }

    /**
     * Use Case 10: Safe State Reversal
     */
    public synchronized void rollbackLastBooking() {
        if (!rollbackStack.isEmpty()) {
            String resID = rollbackStack.pop();
            String type = resID.split("-")[0].toLowerCase();
            inventory.put(type, inventory.get(type) + 1);
            auditTrail.add("[ROLLBACK] Cancelled: " + resID);
            System.out.println("System Restored: " + resID + " returned to inventory.");
        }
    }

    public void printFinalReport() {
        System.out.println("\n--- FINAL OPERATIONAL REPORT ---");
        auditTrail.forEach(System.out::println);
        System.out.println("--------------------------------");
    }
}

// ==========================================
// 4. MULTI-THREADED SIMULATION
// ==========================================

public class BookMyStayApp {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("BOOK MY STAY v1.11 - Concurrent System Running...\n");

        HotelSystem hotel = new HotelSystem();
        hotel.initializeInventory(new SingleRoom(), 2); // Only 2 rooms for 3 guests

        // Use Case 11: Simulate Concurrent Guests
        ExecutorService executor = Executors.newFixedThreadPool(3);
        String[] guests = {"Alice", "Bob", "Charlie"};

        for (String guest : guests) {
            executor.execute(() -> {
                try {
                    String id = hotel.processConcurrentBooking(guest, "Single");
                    System.out.println("Guest " + guest + " confirmed: " + id);
                } catch (BookingException e) {
                    System.err.println("Guest " + guest + " failed: " + e.getMessage());
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        hotel.printFinalReport();
    }
}
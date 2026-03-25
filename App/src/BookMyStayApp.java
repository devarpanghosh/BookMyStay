/**
 * Book My Stay App - Hotel Booking Management System
 * * This class represents the entry point and demonstrates:
 * 1. Application Metadata (Use Case 1)
 * 2. Inheritance & Abstraction (Use Case 2)
 * 3. Encapsulation & Polymorphism (Use Case 2)
 * * @author [Your Name]
 * @version 1.1
 */

// --- DOMAIN MODEL ---

/**
 * Abstract class representing a generalized Room.
 */
abstract class Room {
    private String roomType;
    private int numberOfBeds;
    private double pricePerNight;

    public Room(String roomType, int numberOfBeds, double pricePerNight) {
        this.roomType = roomType;
        this.numberOfBeds = numberOfBeds;
        this.pricePerNight = pricePerNight;
    }

    /**
     * Prints room characteristics to the console.
     */
    public void displayDetails() {
        System.out.println("Room Type       : " + roomType);
        System.out.println("Number of Beds  : " + numberOfBeds);
        System.out.println("Price per Night : $" + pricePerNight);
    }
}

/**
 * Concrete implementations of the Room class.
 */
class SingleRoom extends Room {
    public SingleRoom() { super("Single Room", 1, 100.0); }
}

class DoubleRoom extends Room {
    public DoubleRoom() { super("Double Room", 2, 180.0); }
}

class SuiteRoom extends Room {
    public SuiteRoom() { super("Suite Room", 4, 350.0); }
}

// --- MAIN APPLICATION ENTRY POINT ---

public class BookMyStayApp {

    /**
     * JVM invokes this method to start the application.
     */
    public static void main(String[] args) {

        // --- USE CASE 1: Welcome & Metadata ---
        System.out.println("========================================");
        System.out.println("   Welcome to BOOK MY STAY App          ");
        System.out.println("   System: Hotel Booking Management     ");
        System.out.println("   Version: 1.1                         ");
        System.out.println("========================================\n");

        // --- USE CASE 2: Domain Logic & State ---

        // 1. Initialize Room Objects (Polymorphism)
        Room single = new SingleRoom();
        Room doubleRm = new DoubleRoom();
        Room suite = new SuiteRoom();

        // 2. Static Availability Representation (State Management)
        int singleRoomAvailability = 10;
        int doubleRoomAvailability = 5;
        int suiteRoomAvailability = 2;

        // 3. Display Room Details and Inventory State
        System.out.println("--- Current Room Inventory ---");

        single.displayDetails();
        System.out.println("Current Availability: " + singleRoomAvailability);
        System.out.println("----------------------------------------");

        doubleRm.displayDetails();
        System.out.println("Current Availability: " + doubleRoomAvailability);
        System.out.println("----------------------------------------");

        suite.displayDetails();
        System.out.println("Current Availability: " + suiteRoomAvailability);
        System.out.println("----------------------------------------");

        System.out.println("\nApplication execution completed successfully.");
    }
}
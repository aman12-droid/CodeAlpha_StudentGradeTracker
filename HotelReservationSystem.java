
import java.io.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

class Room {
    private int roomNumber;
    private String category;
    private double price;

    public Room(int roomNumber, String category, double price) {
        this.roomNumber = roomNumber;
        this.category = category;
        this.price = price;
    }

    public int getRoomNumber() { return roomNumber; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }

    public String toString() {
        return "Room: " + roomNumber + " | " + category
                + " | Rs. " + price + " per night";
    }
}

class Booking {
    private String id, name, phone, category;
    private int roomNumber;
    private LocalDate checkIn, checkOut;
    private double amount;
    private String paymentStatus, status;

    public Booking(String id, String name, String phone,
                   int roomNumber, String category,
                   LocalDate checkIn, LocalDate checkOut,
                   double amount, String paymentStatus, String status) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.roomNumber = roomNumber;
        this.category = category;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.status = status;
    }

    public String getId() { return id; }
    public int getRoomNumber() { return roomNumber; }
    public LocalDate getCheckIn() { return checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public String getStatus() { return status; }

    public void cancel() {
        status = "CANCELLED";
    }

    public void showDetails() {
        System.out.println("\n--- BOOKING DETAILS ---");
        System.out.println("Booking ID: " + id);
        System.out.println("Guest: " + name);
        System.out.println("Phone: " + phone);
        System.out.println("Room: " + roomNumber);
        System.out.println("Category: " + category);
        System.out.println("Check-in: " + checkIn);
        System.out.println("Check-out: " + checkOut);
        System.out.println("Amount: Rs. " + amount);
        System.out.println("Payment: " + paymentStatus);
        System.out.println("Status: " + status);
    }

    public String toFileLine() {
        return String.join("|", id, name.replace("|", "/"),
                phone.replace("|", "/"), String.valueOf(roomNumber),
                category, checkIn.toString(), checkOut.toString(),
                String.valueOf(amount), paymentStatus, status);
    }

    public static Booking fromFileLine(String line) {
        try {
            String[] p = line.split("\\|", -1);
            if (p.length != 10) return null;

            return new Booking(p[0], p[1], p[2],
                    Integer.parseInt(p[3]), p[4],
                    LocalDate.parse(p[5]), LocalDate.parse(p[6]),
                    Double.parseDouble(p[7]), p[8], p[9]);
        } catch (Exception e) {
            return null;
        }
    }
}

public class HotelReservationSystem {
    static Scanner sc = new Scanner(System.in);
    static ArrayList<Room> rooms = new ArrayList<>();
    static ArrayList<Booking> bookings = new ArrayList<>();

    static final String BOOKING_FILE = "bookings.txt";
    static final String ROOM_FILE = "rooms.txt";
    static int nextId = 1001;

    public static void main(String[] args) {
        initializeRooms();
        loadRooms();
        loadBookings();

        while (true) {
            System.out.println("\n===== HOTEL RESERVATION SYSTEM =====");
            System.out.println("1. Search Available Rooms");
            System.out.println("2. Book a Room");
            System.out.println("3. View Booking Details");
            System.out.println("4. Cancel Reservation");
            System.out.println("5. View All Active Bookings");
            System.out.println("6. Manage Hotel Rooms");
            System.out.println("7. Exit");

            int choice = readInt("Enter choice: ");

            switch (choice) {
                case 1: searchRooms(); break;
                case 2: bookRoom(); break;
                case 3: viewBooking(); break;
                case 4: cancelBooking(); break;
                case 5: viewAllBookings(); break;
                case 6: manageRooms(); break;
                case 7:
                    saveBookings();
                    saveRooms();
                    System.out.println("Thank you!");
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    static void initializeRooms() {
        for (int i = 101; i <= 105; i++)
            rooms.add(new Room(i, "Standard", 1500));

        for (int i = 201; i <= 205; i++)
            rooms.add(new Room(i, "Deluxe", 2500));

        for (int i = 301; i <= 305; i++)
            rooms.add(new Room(i, "Suite", 4000));
    }

    static String chooseCategory() {
        System.out.println("1. Standard - Rs.1500/night");
        System.out.println("2. Deluxe   - Rs.2500/night");
        System.out.println("3. Suite    - Rs.4000/night");

        while (true) {
            int c = readInt("Choose category: ");
            if (c == 1) return "Standard";
            if (c == 2) return "Deluxe";
            if (c == 3) return "Suite";
            System.out.println("Choose 1, 2 or 3.");
        }
    }

    static LocalDate readDate(String message) {
        while (true) {
            try {
                System.out.print(message);
                return LocalDate.parse(sc.nextLine().trim());
            } catch (Exception e) {
                System.out.println("Use date format YYYY-MM-DD.");
            }
        }
    }

    static boolean validDates(LocalDate in, LocalDate out) {
        if (in.isBefore(LocalDate.now())) {
            System.out.println("Check-in cannot be in the past.");
            return false;
        }
        if (!out.isAfter(in)) {
            System.out.println("Check-out must be after check-in.");
            return false;
        }
        return true;
    }

    static int readInt(String message) {
        while (true) {
            try {
                System.out.print(message);
                return Integer.parseInt(sc.nextLine().trim());
            } catch (Exception e) {
                System.out.println("Enter a valid number.");
            }
        }
    }

    static String readText(String message) {
        while (true) {
            System.out.print(message);
            String s = sc.nextLine().trim();
            if (!s.isEmpty() && !s.contains("|")) return s;
            System.out.println("Enter a valid value without |.");
        }
    }

    static ArrayList<Room> availableRooms(
            String category, LocalDate in, LocalDate out) {

        ArrayList<Room> result = new ArrayList<>();

        for (Room r : rooms) {
            if (!r.getCategory().equalsIgnoreCase(category))
                continue;

            boolean available = true;

            for (Booking b : bookings) {
                boolean sameRoom =
                        b.getRoomNumber() == r.getRoomNumber();
                boolean active = b.getStatus().equals("CONFIRMED");
                boolean datesOverlap =
                        in.isBefore(b.getCheckOut())
                        && out.isAfter(b.getCheckIn());

                if (sameRoom && active && datesOverlap) {
                    available = false;
                    break;
                }
            }

            if (available) result.add(r);
        }

        return result;
    }

    static void searchRooms() {
        String category = chooseCategory();
        LocalDate in = readDate("Check-in (YYYY-MM-DD): ");
        LocalDate out = readDate("Check-out (YYYY-MM-DD): ");

        if (!validDates(in, out)) return;

        ArrayList<Room> available = availableRooms(category, in, out);

        if (available.isEmpty()) {
            System.out.println("No rooms available.");
        } else {
            System.out.println("Available rooms:");
            for (Room r : available) System.out.println(r);
        }
    }

    static void bookRoom() {
        String category = chooseCategory();
        LocalDate in = readDate("Check-in (YYYY-MM-DD): ");
        LocalDate out = readDate("Check-out (YYYY-MM-DD): ");

        if (!validDates(in, out)) return;

        ArrayList<Room> available = availableRooms(category, in, out);

        if (available.isEmpty()) {
            System.out.println("No rooms available for these dates.");
            return;
        }

        for (Room r : available) System.out.println(r);

        int roomNo = readInt("Enter room number: ");
        Room selected = null;

        for (Room r : available) {
            if (r.getRoomNumber() == roomNo) {
                selected = r;
                break;
            }
        }

        if (selected == null) {
            System.out.println("Invalid or unavailable room.");
            return;
        }

        String name = readText("Guest name: ");
        String phone = readText("Phone number: ");

        long nights = ChronoUnit.DAYS.between(in, out);
        double amount = nights * selected.getPrice();

        System.out.println("\n----- PAYMENT SIMULATION -----");
        System.out.println("Number of nights: " + nights);
        System.out.println("Total amount: Rs. " + amount);
        System.out.println("1. Simulate successful payment");
        System.out.println("2. Simulate failed payment");

        int payment = readInt("Choose payment option: ");

        if (payment != 1) {
            System.out.println("Payment failed. Booking not created.");
            return;
        }

        String id = "B" + nextId++;

        Booking b = new Booking(id, name, phone, roomNo,
                category, in, out, amount,
                "PAID (SIMULATED)", "CONFIRMED");

        bookings.add(b);
        saveBookings();

        System.out.println("Booking successful! ID: " + id);
        b.showDetails();
    }

    static Booking findBooking(String id) {
        for (Booking b : bookings) {
            if (b.getId().equalsIgnoreCase(id)) return b;
        }
        return null;
    }

    static void viewBooking() {
        String id = readText("Enter booking ID: ");
        Booking b = findBooking(id);

        if (b == null) System.out.println("Booking not found.");
        else b.showDetails();
    }

    static void cancelBooking() {
        String id = readText("Enter booking ID to cancel: ");
        Booking b = findBooking(id);

        if (b == null) {
            System.out.println("Booking not found.");
        } else if (b.getStatus().equals("CANCELLED")) {
            System.out.println("Already cancelled.");
        } else {
            b.cancel();
            saveBookings();
            System.out.println("Reservation cancelled successfully.");
        }
    }

    static void viewAllBookings() {
        boolean found = false;

        for (Booking b : bookings) {
            if (b.getStatus().equals("CONFIRMED")) {
                b.showDetails();
                found = true;
            }
        }

        if (!found) System.out.println("No active bookings.");
    }

    static void manageRooms() {
        while (true) {
            System.out.println("\n----- MANAGE ROOMS -----");
            System.out.println("1. View all rooms");
            System.out.println("2. Add a room");
            System.out.println("3. Back to main menu");

            int choice = readInt("Enter choice: ");

            if (choice == 1) {
                for (Room r : rooms) System.out.println(r);

            } else if (choice == 2) {
                int number = readInt("New room number: ");
                boolean exists = false;

                for (Room r : rooms) {
                    if (r.getRoomNumber() == number) exists = true;
                }

                if (exists) {
                    System.out.println("Room number already exists.");
                    continue;
                }

                String category = chooseCategory();
                double price;

                while (true) {
                    try {
                        System.out.print("Price per night: Rs. ");
                        price = Double.parseDouble(sc.nextLine().trim());
                        if (price > 0) break;
                    } catch (Exception ignored) { }
                    System.out.println("Enter a positive price.");
                }

                rooms.add(new Room(number, category, price));
                saveRooms();
                System.out.println("Room added successfully.");

            } else if (choice == 3) {
                return;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    static void saveBookings() {
        try (PrintWriter pw = new PrintWriter(
                new FileWriter(BOOKING_FILE))) {
            for (Booking b : bookings) pw.println(b.toFileLine());
        } catch (IOException e) {
            System.out.println("Could not save bookings: " + e.getMessage());
        }
    }

    static void loadBookings() {
        File file = new File(BOOKING_FILE);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int max = 1000;

            while ((line = br.readLine()) != null) {
                Booking b = Booking.fromFileLine(line);

                if (b != null) {
                    bookings.add(b);
                    try {
                        max = Math.max(max,
                                Integer.parseInt(b.getId().substring(1)));
                    } catch (Exception ignored) { }
                }
            }

            nextId = max + 1;
        } catch (IOException e) {
            System.out.println("Could not load bookings.");
        }
    }

    static void saveRooms() {
        try (PrintWriter pw = new PrintWriter(
                new FileWriter(ROOM_FILE))) {
            for (Room r : rooms) {
                pw.println(r.getRoomNumber() + "|"
                        + r.getCategory() + "|" + r.getPrice());
            }
        } catch (IOException e) {
            System.out.println("Could not save rooms.");
        }
    }

    static void loadRooms() {
        File file = new File(ROOM_FILE);
        if (!file.exists()) {
            saveRooms();
            return;
        }

        ArrayList<Room> loaded = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|", -1);

                if (p.length == 3) {
                    try {
                        loaded.add(new Room(
                                Integer.parseInt(p[0]),
                                p[1],
                                Double.parseDouble(p[2])));
                    } catch (Exception ignored) { }
                }
            }

            if (!loaded.isEmpty()) {
                rooms.clear();
                rooms.addAll(loaded);
            }
        } catch (IOException e) {
            System.out.println("Could not load rooms.");
        }
    }
}

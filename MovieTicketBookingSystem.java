import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.Scanner;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class MovieTicketBookingSystem {

    static Scanner input = new Scanner(System.in);

    static String[] movies = {
            "Avatar",
            "Inception",
            "Interstellar",
            "Real Steel"
    };

    static String[] timeSlots = {
            "3:00 PM",
            "6:00 PM",
            "9:00 PM"
    };

    static String[][] seatNames = {
            {"A1", "A2", "A3", "A4", "A5", "A6"},
            {"B1", "B2", "B3", "B4", "B5", "B6"},
            {"C1", "C2", "C3", "C4", "C5", "C6"},
            {"D1", "D2", "D3", "D4", "D5", "D6"},
            {"E1", "E2", "E3", "E4", "E5", "E6"}
    };

    static boolean[][][] bookedSeats = new boolean[movies.length * timeSlots.length][5][6];

    static String[] bookingNames = new String[200];
    static String[] bookingMovies = new String[200];
    static String[] bookingTimes = new String[200];
    static String[] bookingSeatLists = new String[200];
    static int bookingCount = 0;

    static final String FILE_NAME = "booking.txt";

    static JFrame frame;
    static JTextField nameField;
    static JComboBox<String> movieBox;
    static JComboBox<String> timeBox;
    static JButton[][] seatButtons = new JButton[5][6];
    static boolean[][] selectedSeats = new boolean[5][6];
    static JTextArea bookingArea;
    static JLabel summaryLabel;

    public static void main(String[] args) {

        loadBookingsFromFile();

//        runConsoleProgram();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createGui();
            }
        });
    }

    // ---------------- GUI METHODS ----------------

    public static void createGui() {
        frame = new JFrame("Smart Movie Ticket Booking System");
        frame.setSize(850, 550);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Name:"));
        nameField = new JTextField(12);
        topPanel.add(nameField);

        topPanel.add(new JLabel("Movie:"));
        movieBox = new JComboBox<String>(movies);
        topPanel.add(movieBox);

        topPanel.add(new JLabel("Time:"));
        timeBox = new JComboBox<String>(timeSlots);
        topPanel.add(timeBox);

        frame.add(topPanel, BorderLayout.NORTH);

        JPanel seatPanel = new JPanel();
        seatPanel.setLayout(new GridLayout(5, 6, 8, 8));
        createSeatButtons(seatPanel);
        frame.add(seatPanel, BorderLayout.CENTER);

        bookingArea = new JTextArea();
        bookingArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(bookingArea);
        frame.add(scrollPane, BorderLayout.EAST);

        JPanel bottomPanel = new JPanel();
        JButton bookButton = new JButton("Book Selected Seats");
        JButton viewButton = new JButton("View Bookings");
        JButton summaryButton = new JButton("Show Summary");

        bottomPanel.add(bookButton);
        bottomPanel.add(viewButton);
        bottomPanel.add(summaryButton);

        summaryLabel = new JLabel("Select movie and time to view seats.");
        bottomPanel.add(summaryLabel);

        frame.add(bottomPanel, BorderLayout.SOUTH);

        movieBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                clearSelectedSeats();
                refreshSeatButtons();
            }
        });

        timeBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                clearSelectedSeats();
                refreshSeatButtons();
            }
        });

        bookButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                bookSelectedSeatsFromGui();
            }
        });

        viewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                showBookingsInGui();
            }
        });

        summaryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                showSummaryInGui();
            }
        });

        refreshSeatButtons();
        showBookingsInGui();
        frame.setVisible(true);
    }

    public static void createSeatButtons(JPanel seatPanel) {
        for (int row = 0; row < seatNames.length; row++) {
            for (int col = 0; col < seatNames[row].length; col++) {
                JButton button = new JButton(seatNames[row][col]);
                button.setOpaque(true);
                button.setContentAreaFilled(true);
                button.setFocusPainted(false);
                final int selectedRow = row;
                final int selectedCol = col;

                button.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        selectSeatButton(selectedRow, selectedCol);
                    }
                });

                seatButtons[row][col] = button;
                seatPanel.add(button);
            }
        }
    }

    public static void selectSeatButton(int row, int col) {
        int movieChoice = movieBox.getSelectedIndex();
        int timeChoice = timeBox.getSelectedIndex();
        int showIndex = getShowIndex(movieChoice, timeChoice);

        if (bookedSeats[showIndex][row][col]) {
            JOptionPane.showMessageDialog(frame, "Seat " + seatNames[row][col] + " is already booked.");
            return;
        }

        if (selectedSeats[row][col]) {
            selectedSeats[row][col] = false;
        } else {
            selectedSeats[row][col] = true;
        }

        refreshSeatButtons();
    }

    public static void refreshSeatButtons() {
        int movieChoice = movieBox.getSelectedIndex();
        int timeChoice = timeBox.getSelectedIndex();
        int showIndex = getShowIndex(movieChoice, timeChoice);

        for (int row = 0; row < seatNames.length; row++) {
            for (int col = 0; col < seatNames[row].length; col++) {
                if (bookedSeats[showIndex][row][col]) {
                    seatButtons[row][col].setText("X");
                    seatButtons[row][col].setEnabled(false);
                    seatButtons[row][col].setBackground(Color.RED);
                } else {
                    seatButtons[row][col].setText(seatNames[row][col]);
                    seatButtons[row][col].setEnabled(true);

                    if (selectedSeats[row][col]) {
                        seatButtons[row][col].setBackground(Color.GREEN);
                    } else {
                        seatButtons[row][col].setBackground(null);
                    }
                }
            }
        }

        int bookedForShow = countBookedSeatsForShow(movieChoice, timeChoice);
        summaryLabel.setText("Booked: " + bookedForShow + " | Remaining: " + (30 - bookedForShow));
    }

    public static void clearSelectedSeats() {
        for (int row = 0; row < selectedSeats.length; row++) {
            for (int col = 0; col < selectedSeats[row].length; col++) {
                selectedSeats[row][col] = false;
            }
        }
    }

    public static void bookSelectedSeatsFromGui() {
        String name = nameField.getText().trim();

        if (name.length() == 0) {
            JOptionPane.showMessageDialog(frame, "Please enter customer name.");
            return;
        }

        int movieChoice = movieBox.getSelectedIndex();
        int timeChoice = timeBox.getSelectedIndex();

        String[] seatsToBook = new String[30];
        int selectedCount = 0;

        for (int row = 0; row < selectedSeats.length; row++) {
            for (int col = 0; col < selectedSeats[row].length; col++) {
                if (selectedSeats[row][col]) {
                    seatsToBook[selectedCount] = seatNames[row][col];
                    selectedCount++;
                }
            }
        }

        if (selectedCount == 0) {
            JOptionPane.showMessageDialog(frame, "Please select at least one seat.");
            return;
        }

        for (int i = 0; i < selectedCount; i++) {
            markSeatBooked(movieChoice, timeChoice, seatsToBook[i]);
        }

        String seatList = makeSeatList(seatsToBook, selectedCount);
        String movie = movies[movieChoice];
        String time = timeSlots[timeChoice];

        saveBookingInArrays(name, movie, time, seatList);
        saveToFile(name, movie, time, seatList);

        clearSelectedSeats();
        refreshSeatButtons();
        showBookingsInGui();

        JOptionPane.showMessageDialog(frame, "Booking successful for " + movie + " at " + time + ". Seats: " + seatList);
    }

    public static void showBookingsInGui() {
        bookingArea.setText("===== BOOKINGS =====\n");

        if (bookingCount == 0) {
            bookingArea.append("No bookings found.\n");
            return;
        }

        for (int i = 0; i < bookingCount; i++) {
            bookingArea.append("Name: " + bookingNames[i]
                    + ", Movie: " + bookingMovies[i]
                    + ", Showtime: " + bookingTimes[i]
                    + ", Seats: " + bookingSeatLists[i] + "\n");
        }
    }

    public static void showSummaryInGui() {
        int totalBookedSeats = 0;
        int movieChoice = movieBox.getSelectedIndex();
        int timeChoice = timeBox.getSelectedIndex();
        int bookedForShow = countBookedSeatsForShow(movieChoice, timeChoice);

        for (int i = 0; i < bookingCount; i++) {
            totalBookedSeats = totalBookedSeats + countSeatsInList(bookingSeatLists[i]);
        }

        bookingArea.setText("===== BOOKING SUMMARY =====\n");
        bookingArea.append("Total Booking Records: " + bookingCount + "\n");
        bookingArea.append("Total Booked Seats: " + totalBookedSeats + "\n\n");
        bookingArea.append("Selected Show: " + movies[movieChoice] + " at " + timeSlots[timeChoice] + "\n");
        bookingArea.append("Booked Seats For This Show: " + bookedForShow + "\n");
        bookingArea.append("Remaining Seats For This Show: " + (30 - bookedForShow) + "\n");
    }

    // ---------------- CONSOLE METHODS ----------------

    public static void runConsoleProgram() {

        int choice;

        do {
            System.out.println("\n================================");
            System.out.println(" MOVIE TICKET BOOKING SYSTEM");
            System.out.println("================================");
            System.out.println("1. Book Ticket");
            System.out.println("2. View Seating Arrangement");
            System.out.println("3. View Bookings From File");
            System.out.println("4. Booking Summary");
            System.out.println("5. Exit");

            choice = readInt("Enter Choice: ");

            switch (choice) {
                case 1:
                    bookTicket();
                    break;

                case 2:
                    viewSeatsForSelectedShow();
                    break;

                case 3:
                    viewBookingsFromFile();
                    break;

                case 4:
                    bookingSummary();
                    break;

                case 5:
                    System.out.println("Thank You!");
                    break;

                default:
                    System.out.println("Invalid Choice!");
            }

        } while (choice != 5);
    }

    public static void bookTicket() {

        input.nextLine();

        System.out.print("Enter Customer Name: ");
        String name = input.nextLine();

        int movieChoice = selectMovie();
        if (movieChoice == -1) {
            return;
        }

        int timeChoice = selectTimeSlot();
        if (timeChoice == -1) {
            return;
        }

        viewSeats(movieChoice, timeChoice);

        int totalSeats = readInt("How many seats do you want to book? ");
        if (totalSeats < 1 || totalSeats > 30) {
            System.out.println("Seat count must be between 1 and 30!");
            return;
        }

        String[] selectedSeats = new String[totalSeats];
        int selectedCount = 0;

        for (int i = 0; i < totalSeats; i++) {
            System.out.print("Enter Seat " + (i + 1) + " (Example: D3): ");
            String seat = input.next().toUpperCase();

            if (!isValidSeat(seat)) {
                System.out.println("Invalid Seat! Please use seats from A1 to E6.");
                i--;
                continue;
            }

            if (isDuplicateSeat(selectedSeats, selectedCount, seat)) {
                System.out.println("You already selected this seat.");
                i--;
                continue;
            }

            if (isSeatBooked(movieChoice, timeChoice, seat)) {
                System.out.println("Error: Seat " + seat + " is already booked.");
                i--;
                continue;
            }

            selectedSeats[selectedCount] = seat;
            selectedCount++;
        }

        for (int i = 0; i < selectedCount; i++) {
            markSeatBooked(movieChoice, timeChoice, selectedSeats[i]);
        }

        String seatList = makeSeatList(selectedSeats, selectedCount);
        String movie = movies[movieChoice];
        String time = timeSlots[timeChoice];

        saveBookingInArrays(name, movie, time, seatList);
        saveToFile(name, movie, time, seatList);

        System.out.println("\nBooking Successful!");
        System.out.println("Customer: " + name);
        System.out.println("Movie: " + movie);
        System.out.println("Showtime: " + time);
        System.out.println("Seats: " + seatList);
    }

    public static int selectMovie() {
        System.out.println("\nSelect Movie:");

        for (int i = 0; i < movies.length; i++) {
            System.out.println((i + 1) + ". " + movies[i]);
        }

        int movieChoice = readInt("Choice: ");

        if (movieChoice < 1 || movieChoice > movies.length) {
            System.out.println("Invalid Movie!");
            return -1;
        }

        return movieChoice - 1;
    }

    public static int selectTimeSlot() {
        System.out.println("\nSelect Time Slot:");

        for (int i = 0; i < timeSlots.length; i++) {
            System.out.println((i + 1) + ". " + timeSlots[i]);
        }

        int timeChoice = readInt("Choice: ");

        if (timeChoice < 1 || timeChoice > timeSlots.length) {
            System.out.println("Invalid Time Slot!");
            return -1;
        }

        return timeChoice - 1;
    }

    public static void viewSeatsForSelectedShow() {
        int movieChoice = selectMovie();
        if (movieChoice == -1) {
            return;
        }

        int timeChoice = selectTimeSlot();
        if (timeChoice == -1) {
            return;
        }

        viewSeats(movieChoice, timeChoice);
    }

    public static void viewSeats(int movieChoice, int timeChoice) {
        int showIndex = getShowIndex(movieChoice, timeChoice);

        System.out.println("\nSeat Arrangement For " + movies[movieChoice] + " At " + timeSlots[timeChoice]);
        System.out.println("X = Booked\n");

        for (int row = 0; row < seatNames.length; row++) {
            for (int col = 0; col < seatNames[row].length; col++) {
                if (bookedSeats[showIndex][row][col]) {
                    System.out.printf("%-5s", "X");
                } else {
                    System.out.printf("%-5s", seatNames[row][col]);
                }
            }
            System.out.println();
        }
    }

    public static boolean isValidSeat(String seat) {
        for (int row = 0; row < seatNames.length; row++) {
            for (int col = 0; col < seatNames[row].length; col++) {
                if (seatNames[row][col].equals(seat)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isDuplicateSeat(String[] selectedSeats, int selectedCount, String seat) {
        for (int i = 0; i < selectedCount; i++) {
            if (selectedSeats[i].equals(seat)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isSeatBooked(int movieChoice, int timeChoice, String seat) {
        int showIndex = getShowIndex(movieChoice, timeChoice);

        for (int row = 0; row < seatNames.length; row++) {
            for (int col = 0; col < seatNames[row].length; col++) {
                if (seatNames[row][col].equals(seat)) {
                    if (bookedSeats[showIndex][row][col]) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static void markSeatBooked(int movieChoice, int timeChoice, String seat) {
        int showIndex = getShowIndex(movieChoice, timeChoice);

        for (int row = 0; row < seatNames.length; row++) {
            for (int col = 0; col < seatNames[row].length; col++) {
                if (seatNames[row][col].equals(seat)) {
                    bookedSeats[showIndex][row][col] = true;
                }
            }
        }
    }

    public static String makeSeatList(String[] selectedSeats, int selectedCount) {
        String seatList = "";

        for (int i = 0; i < selectedCount; i++) {
            seatList = seatList + selectedSeats[i];

            if (i < selectedCount - 1) {
                seatList = seatList + " ";
            }
        }

        return seatList;
    }

    public static void saveBookingInArrays(String name, String movie, String time, String seatList) {
        if (bookingCount >= bookingNames.length) {
            System.out.println("Booking memory is full!");
            return;
        }

        bookingNames[bookingCount] = name;
        bookingMovies[bookingCount] = movie;
        bookingTimes[bookingCount] = time;
        bookingSeatLists[bookingCount] = seatList;
        bookingCount++;
    }

    public static void saveToFile(String name, String movie, String time, String seatList) {
        try {
            FileWriter writer = new FileWriter(FILE_NAME, true);
            writer.write("Name: " + name + ", Movie: " + movie + ", Showtime: " + time + ", Seats: " + seatList);
            writer.write("\n");
            writer.close();
        } catch (IOException e) {
            System.out.println("File Error!");
        }
    }

    public static void viewBookingsFromFile() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME));

            String line;
            System.out.println("\n===== BOOKINGS =====");

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            reader.close();
        } catch (IOException e) {
            System.out.println("No Bookings Found!");
        }
    }

    public static void bookingSummary() {
        int totalBookedSeats = 0;

        for (int i = 0; i < bookingCount; i++) {
            totalBookedSeats = totalBookedSeats + countSeatsInList(bookingSeatLists[i]);
        }

        System.out.println("\n===== BOOKING SUMMARY =====");
        System.out.println("Total Booking Records: " + bookingCount);
        System.out.println("Total Booked Seats: " + totalBookedSeats);
        System.out.println("\nSelect a show to see available seats:");

        int movieChoice = selectMovie();
        if (movieChoice == -1) {
            return;
        }

        int timeChoice = selectTimeSlot();
        if (timeChoice == -1) {
            return;
        }

        int bookedForShow = countBookedSeatsForShow(movieChoice, timeChoice);
        int remainingForShow = 30 - bookedForShow;

        System.out.println("\nShow: " + movies[movieChoice] + " At " + timeSlots[timeChoice]);
        System.out.println("Booked Seats For This Show: " + bookedForShow);
        System.out.println("Remaining Seats For This Show: " + remainingForShow);
        System.out.println("1. Filter By Movie");
        System.out.println("2. Filter By Time Slot");
        System.out.println("3. Back");

        int choice = readInt("Enter Choice: ");
        input.nextLine();

        switch (choice) {
            case 1:
                System.out.print("Enter Movie Name: ");
                String movieFilter = input.nextLine();
                showFilteredBookings(movieFilter, "movie");
                break;

            case 2:
                System.out.print("Enter Time Slot: ");
                String timeFilter = input.nextLine();
                showFilteredBookings(timeFilter, "time");
                break;

            case 3:
                return;

            default:
                System.out.println("Invalid Choice!");
        }
    }

    public static void showFilteredBookings(String filter, String type) {
        boolean found = false;

        System.out.println("\nMatching Bookings:");

        for (int i = 0; i < bookingCount; i++) {
            if (type.equals("movie")) {
                if (bookingMovies[i].equalsIgnoreCase(filter)) {
                    printBooking(i);
                    found = true;
                }
            } else if (type.equals("time")) {
                if (bookingTimes[i].equalsIgnoreCase(filter)) {
                    printBooking(i);
                    found = true;
                }
            }
        }

        if (!found) {
            System.out.println("No matching bookings found.");
        }
    }

    public static void printBooking(int index) {
        System.out.println("Name: " + bookingNames[index]
                + ", Movie: " + bookingMovies[index]
                + ", Showtime: " + bookingTimes[index]
                + ", Seats: " + bookingSeatLists[index]);
    }

    public static int getShowIndex(int movieChoice, int timeChoice) {
        return (movieChoice * timeSlots.length) + timeChoice;
    }

    public static int countBookedSeatsForShow(int movieChoice, int timeChoice) {
        int total = 0;
        int showIndex = getShowIndex(movieChoice, timeChoice);

        for (int row = 0; row < seatNames.length; row++) {
            for (int col = 0; col < seatNames[row].length; col++) {
                if (bookedSeats[showIndex][row][col]) {
                    total++;
                }
            }
        }

        return total;
    }

    public static int countSeatsInList(String seatList) {
        int count = 0;

        for (int i = 0; i < seatList.length(); i++) {
            if (seatList.charAt(i) == ' ') {
                count++;
            }
        }

        if (seatList.length() > 0) {
            count++;
        }

        return count;
    }

    public static int readInt(String message) {
        int number;

        while (true) {
            try {
                System.out.print(message);
                number = input.nextInt();
                return number;
            } catch (Exception e) {
                System.out.println("Invalid input! Please enter a number.");
                input.nextLine();
            }
        }
    }

    public static void loadBookingsFromFile() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME));
            String line;

            while ((line = reader.readLine()) != null) {
                readBookingLine(line);
            }

            reader.close();
        } catch (IOException e) {
            // File will be created when the first booking is saved.
        }
    }

    public static void readBookingLine(String line) {
        String name = getValue(line, "Name: ", ", Movie:");
        String movie = getValue(line, "Movie: ", ", Showtime:");
        String time = getValue(line, "Showtime: ", ", Seats:");
        String seatList = "";

        int seatIndex = line.indexOf("Seats: ");
        if (seatIndex != -1) {
            seatList = line.substring(seatIndex + 7);
        }

        if (name.length() == 0 || movie.length() == 0 || time.length() == 0 || seatList.length() == 0) {
            return;
        }

        int movieIndex = findMovieIndex(movie);
        int timeIndex = findTimeIndex(time);

        if (movieIndex == -1 || timeIndex == -1) {
            return;
        }

        saveBookingInArrays(name, movie, time, seatList);
        markSeatsFromList(movieIndex, timeIndex, seatList);
    }

    public static String getValue(String line, String startText, String endText) {
        int start = line.indexOf(startText);
        int end = line.indexOf(endText);

        if (start == -1 || end == -1 || end <= start) {
            return "";
        }

        return line.substring(start + startText.length(), end);
    }

    public static int findMovieIndex(String movie) {
        for (int i = 0; i < movies.length; i++) {
            if (movies[i].equalsIgnoreCase(movie)) {
                return i;
            }
        }

        return -1;
    }

    public static int findTimeIndex(String time) {
        for (int i = 0; i < timeSlots.length; i++) {
            if (timeSlots[i].equalsIgnoreCase(time)) {
                return i;
            }
        }

        return -1;
    }

    public static void markSeatsFromList(int movieIndex, int timeIndex, String seatList) {
        String seat = "";

        for (int i = 0; i < seatList.length(); i++) {
            char ch = seatList.charAt(i);

            if (ch == ' ') {
                if (seat.length() > 0) {
                    markSeatBooked(movieIndex, timeIndex, seat);
                    seat = "";
                }
            } else {
                seat = seat + ch;
            }
        }

        if (seat.length() > 0) {
            markSeatBooked(movieIndex, timeIndex, seat);
        }
    }
}


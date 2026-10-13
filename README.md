# 🎬 CineShow - Movie Ticket Booking System

> **2nd Year College Micro-Project (Diploma / Degree Computer Engineering / IT)**  
> Built with **Core Java (JDK 17)** Backend, REST API, JSON File Persistence, and a modern **BookMyShow-inspired** responsive web interface with a **Dummy Payment Gateway**.

---

## 🌟 Key Features

1. **BookMyShow Inspired UI/UX**:
   - Dark cinematic aesthetic (`#0f1016` with BookMyShow crimson red accents `#f84464`).
   - Hero banner carousel showcasing trending movies.
   - Real-time instant search by title, genre, or cast.
   - Filter pills for Genres (*Action, Sci-Fi, Comedy, Thriller, Drama, Animation*) and Languages (*Hindi, English, Telugu*).
   - City selector (*Mumbai, Delhi-NCR, Bengaluru, Pune, Hyderabad, Chennai*).

2. **User Authentication (Sign In & Sign Up)**:
   - Full user registration with Full Name, Email, Mobile Number, and Password.
   - Secure login and user session persistence in `localStorage`.
   - Pre-configured demo account ready for immediate demonstration.

3. **Movie Showtimes & Theaters**:
   - Date picker tabs (*Today, Tomorrow, Day 3*).
   - Multi-theater listings (*PVR Cinemas, INOX Megaplex, Cinepolis, PVR ICON*).
   - Amenities badges (*M-Ticket, Food & Beverage, Recliner, IMAX 3D*).
   - Multiple time slots (*10:15 AM, 01:45 PM, 05:30 PM, 09:15 PM*).

4. **Interactive Seat Selection Layout**:
   - Curved cinema screen visualization: *"SCREEN THIS WAY (All eyes here)"*.
   - 3 Seat Tiers:
     - 🛋️ **Platinum Recliner (₹380)** - Rows A & B
     - 💺 **Gold Prime (₹240)** - Rows C, D, & E
     - 🎟️ **Silver Classic (₹160)** - Rows F, G, & H
   - Visual seat indicators: **Available**, **Selected (Green)**, and **Sold Out / Booked (Dark Grey)**.
   - Real-time fare calculation with 18% GST and convenience fee breakdown.
   - **Concurrency Safety**: Thread-safe synchronized seat reservation prevents double-booking race conditions.

5. **Simulated Dummy Payment Gateway**:
   - Resembles modern Indian payment checkouts (like Razorpay / PayTM / BMS Cash).
   - 3 Interactive Payment Methods:
     - 📱 **UPI / QR Code**: Interactive simulated QR code + UPI ID validation (*GPay, PhonePe, Paytm, BHIM*).
     - 💳 **Credit / Debit Card**: 16-digit card input with auto-formatting, MM/YY expiry, and CVV.
     - 🏦 **Net Banking**: Quick bank selection (*HDFC, SBI, ICICI, Axis*).
   - Realistic multi-stage simulated processing animation (*Securing Connection → Authorizing Transaction → Payment Approved*).

6. **Cinematic Boarding Pass / Ticket Confirmation**:
   - High-fidelity movie ticket pass with movie poster, booking ID, theater, screen, date, showtime, and seat badges.
   - Barcode and QR code entry graphics.
   - **One-Click Print / PDF Download**: Formatted with `@media print` CSS for physical or PDF ticket printing.

7. **My Bookings Dashboard**:
   - View all past and upcoming booked tickets.
   - Reopen any ticket pass anytime.
   - **Ticket Cancellation with Seat Release**: Cancelling a ticket immediately frees up the seats back to available in the database!

8. **Zero Heavy Dependencies**:
   - Runs on standard Java 17 standard library (`com.sun.net.httpserver.HttpServer`).
   - No complex Tomcat setup, no heavyweight external database setup, no Maven required to run!

---

## 🚀 How to Run the Project

### Option A: One-Click Launch (Windows Batch File - Recommended)
Simply **double-click** `run.bat` in this folder:
```cmd
run.bat
```
*(Or right-click `run.bat` and select "Open"). It compiles all Java files and starts the server on `http://localhost:8080` automatically.*

---

### Option B: PowerShell
In PowerShell, run:
```powershell
.\run.ps1
```

---

### Option C: Manual Command Line (Using JDK)
1. **Compile all Java source files:**
   ```cmd
   javac -encoding UTF-8 -d bin src\main\java\com\movieticket\util\*.java src\main\java\com\movieticket\model\*.java src\main\java\com\movieticket\storage\*.java src\main\java\com\movieticket\server\*.java src\main\java\com\movieticket\Main.java
   ```

2. **Run the server:**
   ```cmd
   java -cp bin com.movieticket.Main
   ```

3. **Open in your web browser:**
   ```
   http://localhost:8080
   ```

---

## 🔑 Demo Account Credentials

You can test immediately without creating a new account, or register a new one:

- **Email:** `demo@example.com`
- **Password:** `password123`

---

## 📁 Project Directory Structure

```text
movie ticket bkng/
├── run.bat                          # One-click Windows runner
├── run.ps1                          # PowerShell runner script
├── pom.xml                          # Maven build file (for Eclipse/IntelliJ/IDE)
├── README.md                        # Documentation & setup instructions
├── PROJECT_REPORT_GUIDE.md          # 2nd Year College Viva & Report Guide
├── data/
│   └── database.json                # Persistent JSON database (auto-created)
├── bin/                             # Compiled Java .class bytecode
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── movieticket/
        │           ├── Main.java              # Application entry point
        │           ├── model/
        │           │   ├── User.java          # User entity model
        │           │   ├── Movie.java         # Movie catalog model
        │           │   ├── Showtime.java      # Theater showtime & seat model
        │           │   └── Booking.java       # Ticket booking record model
        │           ├── storage/
        │           │   └── DataStore.java     # Thread-safe persistent file store
        │           ├── server/
        │           │   ├── MovieWebServer.java    # Built-in HTTP server & routing
        │           │   └── HttpHandlerHelper.java # HTTP helpers & CORS handling
        │           └── util/
        │               └── SimpleJson.java    # Zero-dependency JSON parser/serializer
        └── resources/
            └── static/
                ├── index.html       # Single Page Application HTML5
                ├── css/
                │   └── style.css    # BookMyShow dark theme styling
                └── js/
                    └── app.js       # Frontend UI logic & REST API consumer
```

---

## 🔌 REST API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/` | Serves the main Single Page Application (`index.html`) |
| `GET` | `/api/movies` | Get all movies (supports `?q=`, `?genre=`, `?language=`) |
| `GET` | `/api/movies/{id}` | Get details of a single movie |
| `GET` | `/api/movies/{id}/showtimes?date=YYYY-MM-DD` | Get showtimes and seat status for a movie |
| `GET` | `/api/showtimes/{id}` | Get real-time seat availability for a showtime |
| `POST` | `/api/auth/register` | Register a new user account |
| `POST` | `/api/auth/login` | Authenticate user credentials |
| `POST` | `/api/bookings` | Book seats with dummy payment verification |
| `GET` | `/api/bookings?userId={id}` | Retrieve all booked tickets for a user |
| `POST` | `/api/bookings/{id}/cancel` | Cancel a booking and release seats |

---

## 🎓 College Viva & Micro-Project Support

Check out [PROJECT_REPORT_GUIDE.md](file:///c:/Users/bodke/OneDrive/Desktop/movie%20ticket%20bkng/PROJECT_REPORT_GUIDE.md) for:
- Viva questions and answers for external examiners.
- System architecture explanations.
- Object-Oriented Programming (OOP) concepts demonstrated in Java.


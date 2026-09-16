# CineBook — Movie Ticket Management System

CineBook is a Java 21 desktop Movie Ticket Management System built with Java Swing, MySQL and JDBC.

Java Swing-based Movie Ticket Management System with MySQL, interactive seat booking, digital QR tickets, CinePoints, coupons, TMDB integration and admin management.

**IMPORTANT:** Payment is simulated. There is no real payment gateway. Email integration is not included.

## Major Functionality

- Movie browsing
- Movie search
- TMDB integration
- OMDb fallback
- Theatre management
- Showtimes
- Interactive seat selection
- Seat availability
- Booking management
- Simulated payment
- Digital tickets
- QR code tickets
- Booking history
- Watchlist
- Favorites
- CinePoints
- Coupons
- User authentication
- Admin management
- Responsive Swing UI
- Light/Dark/Default theme
- Windows desktop packaging

## Download for Windows

Windows users can download the latest packaged CineBook application directly from the **[Releases](../../releases)** page. You do not need to build from source to run the application.

## Screenshots

*(Screenshots will be added here: Login, User Dashboard, Movie Details, Seat Selection, Checkout, Digital Ticket, Admin Dashboard)*

## Installation & Setup

### Requirements
- Windows
- Java 21 (if running from source)
- Maven
- MySQL 8.x
- Git

### Database Setup

1. Create a MySQL database named `movie_ticket_management`.
2. Run the schema script: `database/schema.sql`
3. Optionally, run the sample data script: `database/sample_data.sql`
4. Copy `src/main/resources/db.properties.example` to `src/main/resources/db.properties`.
5. Enter your local MySQL credentials in `db.properties`. 

> **Note:** Users must provide their own database credentials. Never commit your real `db.properties` file!

### Movie API Configuration

CineBook uses real movie metadata, posters, and trailers via external APIs:
- **TMDB** is the primary movie metadata/poster/trailer source.
- **OMDb** is used as a fallback where applicable.

**Never place real API keys in the repository.**

To configure the APIs locally, set the following Environment Variables on your system before launching the application:

- `TMDB_API_KEY=YOUR_TMDB_API_KEY`
- `OMDB_API_KEY=YOUR_OMDB_API_KEY`

If the environment variables are not set, you may see exceptions or missing posters for movies.

## Run from Source

To compile, package, and run the application from source:

```bash
mvn clean package
```

Then run the application:
```bash
mvn exec:java -Dexec.mainClass="com.movieticket.Main"
```

## Security

- **Never commit database passwords.**
- **Never commit TMDB/OMDb API keys.**
- **Never commit private credentials.**
- Users should configure their own local secrets using environment variables and `db.properties`.
- Simulated payment does not process real money.

## Project Structure

```text
src/
├── main/
│   └── java/
│       └── com/movieticket/
│           ├── controller/
│           ├── dao/
│           ├── model/
│           ├── service/
│           ├── view/
│           └── util/

database/
├── schema.sql
└── sample_data.sql

pom.xml
README.md
.gitignore
```

## License

[PLACEHOLDER FOR LICENSE]

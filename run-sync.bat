set TMDB_API_BASE_URL=https://api.tmdb.org/3
mvn test-compile exec:java -Dexec.mainClass="com.movieticket.RunExistingSync" -Dexec.classpathScope="test"

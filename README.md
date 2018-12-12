# hours-logger
An open source logger for hourly work, written in Java.

# About this Project
Danny's Website Design releases this work under [an MIT License](LICENSE).
For contributing, please see [CONTRIBUTING](CONTRIBUTING).

This project is to be worked on in IntelliJ IDEA. To import it, simply open the project in IDEA.
You may have to add `hours-logger.iml` as a module, and, for JAR export, see `.idea/artifacts/HoursLogger.xml`.

# File Formatting
hours-logger saves output as `.csv` (Comma Seperated Values) files.
This format is best in our purpose, since we can easily open them in Excel.

# File Structure
File structure for output is saved as:
- a header row
- a client
- their work done

Where, a client has their info stored as:

    name
    Rate, ${hourly rate}
    {work... each line is one task}
    --

Their work done is as a row, for each "Work", stored as,

    {start time as ISO-8601 Instant}, {end time as ISO-8601 Instant}, {description}, ${amount owed}

# Multi-platform

This should work with any device that can run GUI Java code, including Windows, Mac OS X, and a Linux GUI.

In order to run it, you will need a copy of the Java Runtime Environment. We built this one on Java 8. You may download this specific version at [https://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html](https://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html).

package parser;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ActorSAXParser extends DefaultHandler {
    private List<Actor> actors;
    private Actor tempActor;
    private String tempValue;
    private Connection conn;
    private PreparedStatement insertActorStmt;
    private Map<String, String> existingStars;
    private int count = 1;

    private static final Logger logger = Logger.getLogger(CastSAXParser.class.getName());

    static {
        try {
            FileHandler fileHandler = new FileHandler("parse_errors.log", true); // Append to the log file
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false); // Prevent logging to console twice
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ActorSAXParser(Connection conn) throws SQLException {
        this.conn = conn;
        this.actors = new ArrayList<>();

        insertActorStmt = conn.prepareStatement(
                "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)"
        );

        loadExistingStars();
    }

    private void loadExistingStars() throws SQLException {
        existingStars = new HashMap<>();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT id, name FROM stars");
        while (rs.next()) {
            existingStars.put(rs.getString("name").toLowerCase(), rs.getString("id"));
        }
        rs.close();
        stmt.close();
    }

    public void parseXML(String filename) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            // Explicitly set ISO-8859-1 encoding
            FileInputStream fis = new FileInputStream(filename);
            InputStreamReader isr = new InputStreamReader(fis, "ISO-8859-1");
            InputSource inputSource = new InputSource(isr);

            saxParser.parse(inputSource, this);
            batchInsertActors();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void batchInsertActors() {
        try {
            for (Actor actor : actors) {
                if (!existingStars.containsKey(actor.getName().toLowerCase())) {
                    String starId = "mm" + (1000000 + count);
                    count++;
                    System.out.println("New star: " + starId);


                    insertActorStmt.setString(1, starId);
                    insertActorStmt.setString(2, actor.getName());
                    if (actor.getBirthYear() != null) {
                        insertActorStmt.setInt(3, actor.getBirthYear());
                    } else {
                        insertActorStmt.setNull(3, Types.INTEGER);
                    }

                    insertActorStmt.addBatch();

                    existingStars.put(actor.getName().toLowerCase(), starId);
                }
            }
            insertActorStmt.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();

                System.err.println(e);
                System.err.println("Error in actor -> Rolling Back");
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        tempValue = "";
        if (qName.equalsIgnoreCase("actor")) {
            tempActor = new Actor();
        }
    }

    public void characters(char[] ch, int start, int length) {
        tempValue = new String(ch, start, length).trim();
    }

    public void endElement(String uri, String localName, String qName) {
        if (qName.equalsIgnoreCase("stagename")) {
            tempActor.setName(tempValue.trim());
        }else if (qName.equalsIgnoreCase("dob")) {
            tempValue = tempValue.trim();
            if (!tempValue.isEmpty() && tempValue.matches("\\d+")) {
                tempActor.setBirthYear(Integer.parseInt(tempValue));
            } else {
                tempActor.setBirthYear(null);
            }


    } else if (qName.equalsIgnoreCase("actor")) {
            if (!existingStars.containsKey(tempActor.getName().toLowerCase())) {
                actors.add(tempActor);
            }
        }
    }
}





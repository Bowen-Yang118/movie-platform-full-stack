package parser;

import java.sql.Connection;
import java.sql.DriverManager;

public class XMLDataImporter {
    public static void main(String[] args) {
        try {
            // connect to database
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb", "mytestuser", "My6$Password");
            conn.setAutoCommit(false);

            // mains243.xml（movies）
            MovieSAXParser movieParser = new MovieSAXParser(conn);
            movieParser.parseXML("XML_related/mains243.xml");
            

            // actors63.xml（stars）
            ActorSAXParser actorParser = new ActorSAXParser(conn);
            actorParser.parseXML("XML_related/actors63.xml");


            // casts124.xml（stars_in_movies）
            CastSAXParser castParser = new CastSAXParser(conn);
            castParser.parseXML("XML_related/casts124.xml");

            conn.commit();
            conn.close();

            System.out.println("FINISHED");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


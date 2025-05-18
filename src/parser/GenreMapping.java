package parser;

import java.util.HashMap;
import java.util.Map;

public class GenreMapping {
    public static final Map<String, String> genreMapping = new HashMap<>();

    static {
        //Drama
        genreMapping.put("Dram", "Drama");
        genreMapping.put("DRam", "Drama");
        genreMapping.put("DraM", "Drama");
        genreMapping.put("Dram>", "Drama");
        genreMapping.put("Dramd", "Drama");
        genreMapping.put("Draam", "Drama");
        genreMapping.put("Dram Docu", "Drama");
        genreMapping.put("Dram.Actn", "Drama");
        genreMapping.put("anti-Dram", "anti-Drama");
        genreMapping.put("ram", "Drama");
        genreMapping.put("Dramn", "Drama");
        genreMapping.put("dram", "Drama");
        genreMapping.put("DRAM", "Drama");


        //Comedy
        genreMapping.put("Comd", "Comedy");
        genreMapping.put("Comdx", "Comedy");
        genreMapping.put("Cond", "Comedy");
        genreMapping.put("CmR", "Comedy");
        genreMapping.put("Comd Noir", "Comedy");
        genreMapping.put("Comd West", "Comedy");
        genreMapping.put("comd", "Comedy");

        //Crime
        genreMapping.put("CnRb", "Crime Noir");
        genreMapping.put("CnR", "Crime Noir");
        genreMapping.put("CnRbb", "Crime Noir");
        genreMapping.put("Crime Noir", "Crime Noir");
        genreMapping.put("Noir", "Crime Noir");
        genreMapping.put("Noir Comd", "Crime Noir");
        genreMapping.put("Noir Comd Romt", "Crime Noir");
        genreMapping.put("Crim", "Crime");

        //Action
        genreMapping.put("actn", "Action");
        genreMapping.put("Act", "Action");
        genreMapping.put("Actn", "Action");
        genreMapping.put("Axtn", "Action");
        genreMapping.put("Adctx", "Action");
        genreMapping.put("Adct", "Action");
        genreMapping.put("Sctn", "Action");

        //Romance
        genreMapping.put("Romt", "Romance");
        genreMapping.put("Romtx", "Romance");
        genreMapping.put("Ront", "Romance");
        genreMapping.put("Romt Actn", "Romance");
        genreMapping.put("Romt Dram", "Romance");
        genreMapping.put("Romt. Comd", "Romance");
        genreMapping.put("Romt Fant", "Romance");
        genreMapping.put("RomtAdvt", "Romance");
        genreMapping.put("romt", "Romance");

        //Sci-Fi
        genreMapping.put("SciF", "Sci-Fi");
        genreMapping.put("ScFi", "Sci-Fi");
        genreMapping.put("Scfi", "Sci-Fi");
        genreMapping.put("SxFi", "Sci-Fi");
        genreMapping.put("S.F.", "Sci-Fi");

        //Musical
        genreMapping.put("Music", "Musical");
        genreMapping.put("Musc", "Musical");
        genreMapping.put("Muusc", "Musical");
        genreMapping.put("Muscl", "Musical");
        genreMapping.put("Stage Musical", "Musical");
        genreMapping.put("stage Musical", "Musical");

        //Horror
        genreMapping.put("Horr", "Horror");
        genreMapping.put("Hor", "Horror");
        genreMapping.put("H0", "Horror");
        genreMapping.put("Viol", "Thriller");
        genreMapping.put("Susp", "Thriller");
        genreMapping.put("susp", "Thriller");

        //Fantasy
        genreMapping.put("Fant", "Fantasy");
        genreMapping.put("FantH*", "Fantasy");

        //Mystery
        genreMapping.put("Myst", "Mystery");
        genreMapping.put("Mystp", "Mystery");

        //Biography
        genreMapping.put("Bio", "Biography");
        genreMapping.put("BiopP", "Biography");
        genreMapping.put("Biop", "Biography");
        genreMapping.put("BioP", "Biography");
        genreMapping.put("BioPx", "Biography");
        genreMapping.put("BioG", "Biography");
        genreMapping.put("BioB", "Biography");
        genreMapping.put("BioPP", "Biography");

        genreMapping.put("Faml", "Family");

        //Documentary
        genreMapping.put("Docu", "Documentary");
        genreMapping.put("Ducu", "Documentary");
        genreMapping.put("Docu Dram", "Documentary");
        genreMapping.put("Duco", "Documentary");
        genreMapping.put("verite", "Documentary");
        genreMapping.put("Natu", "Documentary");

        //Adventure
        genreMapping.put("Advt", "Adventure");

        //Disaster
        genreMapping.put("Disa", "Disaster");
        genreMapping.put("Dist", "Disaster");

        //Experimental
        genreMapping.put("Avant Garde", "Experimental");
        genreMapping.put("AvGa", "Experimental");
        genreMapping.put("Expm", "Experimental");
        genreMapping.put("Weird", "Cult");
        genreMapping.put("Art Video", "Experimental");
        genreMapping.put("Art", "Experimental");

        //Surrealism
        genreMapping.put("Surr", "Surreal");
        genreMapping.put("Surl", "Surreal");
        genreMapping.put("surreal", "Surreal");

        //Psychological Drama
        genreMapping.put("Psych Dram", "Psychological Drama");
        genreMapping.put("Psyc", "Psychological Drama");

        //Sports
        genreMapping.put("Sports", "Sport");
        genreMapping.put("sports", "Sport");

        genreMapping.put("West", "Western");

        genreMapping.put("Hist", "History");


        genreMapping.put("TVmini", "TV");

        //Adult
        genreMapping.put("Porn", "Adult");
        genreMapping.put("porn", "Adult");
        genreMapping.put("Porb", "Adult");
        genreMapping.put("txx", "Adult");
        genreMapping.put("Ctxxx", "Adult");
        genreMapping.put("Ctcxx", "Adult");
        genreMapping.put("Ctxx", "Adult");
        genreMapping.put("Kinky", "Adult");
        genreMapping.put("Scat", "Adult");

        //Cult Films
        genreMapping.put("Camp", "Cult");
        genreMapping.put("Cult", "Cult");



    }

    public static Map<String, String> getGenreMapping() {
        return genreMapping;
    }
}

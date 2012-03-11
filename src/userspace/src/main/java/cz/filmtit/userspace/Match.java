package cz.filmtit.userspace;

import org.apache.commons.collections.list.SynchronizedList;

import java.util.*;
            
/**
 * Represents a match found in the TM.
 *
 * @author Jindřich Libovický
*/

public class Match extends DatabaseObject {
    /**
     * Default constructor for Hibernate. It does nothing.
     */
    public Match() {
        translations = new ArrayList<Translation>();
    }

    // Here another constructor will follow for creating from TM

    private String match;
    private List<Translation> translations;
    private Long chunkDatabaseId = -1l;

    public String getMatch() {
        return match;
    }

    public void setMatch(String match) {
        if (match == null) { this.match = match; }
        else { throw new UnsupportedOperationException("The match text can be set just once."); }
    }

    public Match(String match, List<Translation> translations) {
        this.match = match;
        this.translations = translations;
    }

    public List<Translation> getTranslations() {
        return translations;
    }

    public Long getChunkDatabaseId() {
        return chunkDatabaseId;
    }

    public void setChunkDatabaseId(Long chunkDatabaseID) {
        this.chunkDatabaseId = chunkDatabaseID;
    }

    public void loadTranslationsFromDatabase() {
        org.hibernate.Session session = UserSpace.getSessionFactory().getCurrentSession();
        session.beginTransaction();

        // query the matches from the database
        List foundTranslations = session.createQuery("select t from Translations where m.chunkId = :tid")
                .setParameter("tid", getDatabaseId()).list();

        List <Translation> newTranslations = new ArrayList<Translation>();
        for (Object t : foundTranslations) {
            newTranslations.add((Translation)t);
        }
        translations = Collections.synchronizedList(newTranslations);

        session.getTransaction().commit();
    }

    /**
     * Saves the object to the database.
     */
    public void saveToDatabase() {
        saveJustObject();
        
        // TODO: if it was a new object, is the databaseId set right now?
        
        for (Translation t : translations) {
            t.setMatchDatabaseId(getDatabaseId());
            t.saveToDatabase();
        }
    }

    public void deleteFromDatabase() {
        deleteJustObject();
        for (Translation translation : translations) {
            translation.deleteFromDatabase();
        }
    }
}

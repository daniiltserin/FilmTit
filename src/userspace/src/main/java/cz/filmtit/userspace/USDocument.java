package cz.filmtit.userspace;

import cz.filmtit.share.*;
import cz.filmtit.core.model.data.*;
import org.hibernate.Session;

import java.util.*;

/* Functionality ... what may happen
    - changing the title / year or even language of the document => regenerate no finished translations
    - changing the original language chunk => regenerate this chunk translations
    - changing the timing of the chunk
    - adding new chunk, deleting a chunk
    - claim it's finished => delete matches, ?? where the result file will be generated
                               update the TM
 */

/**
 * Represents a subtitle file the user work with.
 * @author Jindřich Libovický
 */
public class USDocument extends DatabaseObject {
    private static final int MINIMUM_MOVIE_YEAR = 1850;
    private static final int ALLOWED_FUTURE_FOR_YEARS = 5;
    private static final long RELOAD_TRANSLATIONS_TIME = 86400000;

    private long ownerDatabaseId;
    private Document document;
    private List<USTranslationResult> translationResults;
    private long workStartTime;
    private long translationGenerationTime;
    private boolean finished;

    private String cachedMovieTitle;
    private String cachedMovieYear;
    
    public USDocument(Document document) {
        this.document = document;
        workStartTime = new Date().getTime();
        translationResults = new ArrayList<USTranslationResult>();

        Session dbSession = HibernateUtil.getSessionWithActiveTransaction();
        saveToDatabase(dbSession);
        HibernateUtil.closeAndCommitSession(dbSession);
    }

    /**
     * Default constructor (for Hibernate).
     */
    public USDocument() {
        document = new Document();
        translationResults = new ArrayList<USTranslationResult>();
    }

    public long getOwnerDatabaseId() {
        return ownerDatabaseId;
    }

    public void setOwnerDatabaseId(long ownerDatabaseId) {
        this.ownerDatabaseId = ownerDatabaseId;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public Document getDocument() {
		return document;
	}

     public String getMovieTitle() {
        return document.getMovie().getTitle();
    }

    /**
     * Sets the title of the movie. If the year of publishing the movie has been defined,
     * it also updates the IMDB information.
     * @param movieTitle New movie title.
     */
    public void setMovieTitle(String movieTitle) {
        cachedMovieTitle = movieTitle;
        if (cachedMovieYear != null) { generateMediaSource(); }
    }

    public String getYear() {
        return document.getMovie().getYear();
    }

    private void generateMediaSource() {
        document.setMovie(MediaSourceFactory.fromIMDB(cachedMovieTitle, cachedMovieYear));
    }

    /**
     * Sets the year when the movie was published. If the title has been defined, it also updates the IMDB information.
     * @param year New value of year.
     * @throws IllegalArgumentException
     */
    public void setYear(String year) {
        int yearInt = Integer.parseInt(year);
        // the movie should be from a reasonable time period
        if (yearInt < MINIMUM_MOVIE_YEAR  ) {
            throw new IllegalArgumentException("Value of year should from 1850 to the current year + "  +
                    Calendar.YEAR + "" + ALLOWED_FUTURE_FOR_YEARS + ".");
        }
        cachedMovieYear = year;
        if (cachedMovieTitle != null) { generateMediaSource(); }
    }

    /**
     * Gets the time spent on translating this subtitles valid right now.
     * @return The time spent on this subtitles in milliseconds.
     */
    public long getSpentOnThisTime() {
        return document.spentOnThisTime + (new Date()).getTime() - workStartTime;
    }

    /**
     * Sets the new time which was spent on translating this subtitles.
     * If this is set, the work start time reset to current time.
     * @param spentOnThisTime New value of time spent on this document.
     */
    public void setSpentOnThisTime(long spentOnThisTime) {
        document.spentOnThisTime = spentOnThisTime;
        workStartTime = new Date().getTime();
    }

    public Language getLanguage() {
        return document.getLanguage();
    }

    public String getLanguageCode() {
        return document.getLanguage().getCode();
    }
    
    public void setLanguageCode(String languageCode) {
        document.setLanguageCode(languageCode);
    }

    public MediaSource getMediaSource() {
        return document.getMovie();
    }

    public long getTranslationGenerationTime() {
        return translationGenerationTime;
    }

    public void setTranslationGenerationTime(long translationGenerationTime) {
        this.translationGenerationTime = translationGenerationTime;
    }

    protected long getSharedDatabaseId() {
        return document.getId();
    }

    protected void setSharedDatabaseId(long databaseId) {
        document.setId(databaseId);
    }

    protected void setMovie(MediaSource movie) {
        document.setMovie(movie);
    }

    protected MediaSource getMovie() {
        return document.getMovie();
    }

    public List<USTranslationResult> getTranslationsResults() {
        Collections.sort(translationResults);
        return translationResults;
    }


    /**
     * Loads the translationResults from User Space database if there are some
     */
    public void loadChunksFromDb() {
        org.hibernate.Session dbSession = HibernateUtil.getSessionWithActiveTransaction();
    
        // query the database for the translationResults
        List foundChunks = dbSession.createQuery("select c from USTranslationResult c where c.documentDatabaseId = :did")
                .setParameter("did", getDatabaseId()).list();

        translationResults = new ArrayList<USTranslationResult>();
        for (Object o : foundChunks) {
            USTranslationResult result = (USTranslationResult)o;
            result.setParent(this);
            translationResults.add(result);
        }
    
        HibernateUtil.closeAndCommitSession(dbSession);

        Collections.sort(translationResults);
        // add the translation results to the inner document
        for (USTranslationResult usResult : translationResults) {
            document.getTranslationResults().add(usResult.getTranslationResult());
        }

        // if the translationResults have old translations, regenerate them
        /*if (new Date().getTime() > this.translationGenerationTime + RELOAD_TRANSLATIONS_TIME)  {
            for (USTranslationResult translationResult : translationResults) {
                translationResult.generateMTSuggestions(FilmTitServer.getInstance().getTM());
            }
        } */
        // otherwise they're automatically loaded from the database
    }

    /**
     * Saves the document to the database including the Translation Results in the case they were loaded
     * or created.
     * @param dbSession  Opened database session.
     */
    public void saveToDatabase(Session dbSession) {
        saveJustObject(dbSession);

        if (translationResults != null) {
            for (USTranslationResult translationResult : translationResults) {
                translationResult.saveToDatabase(dbSession);
            }
        }
    }

    public void deleteFromDatabase(Session dbSession) {
        deleteJustObject(dbSession);
    }

    public void addTranslationResult(USTranslationResult translationResult) {
        translationResults.add(translationResult);
        document.getTranslationResults().add(translationResult.getTranslationResult());
    }


    public void replaceTranslationResult(USTranslationResult usTranslationResult) {
         for (int i = 0; i < translationResults.size(); ++i) {
             if (translationResults.get(i).getSharedId() == usTranslationResult.getSharedId()) {
                 translationResults.set(i, usTranslationResult);
             }
         }

        for (int i = 0; i < document.getTranslationResults().size(); ++i) {
            if (document.getTranslationResults().get(i).getChunkId() == usTranslationResult.getSharedId()) {
                document.getTranslationResults().set(i, usTranslationResult.getTranslationResult());
            }
        }
    }

}

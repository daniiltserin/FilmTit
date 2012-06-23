package cz.filmtit.share;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import cz.filmtit.share.exceptions.InvalidChunkIdException;
import cz.filmtit.share.exceptions.InvalidDocumentIdException;
import cz.filmtit.share.exceptions.InvalidSessionIdException;

import java.util.List;
//import cz.filmtit.share.Feedback;

@RemoteServiceRelativePath("filmtit")
public interface FilmTitService extends RemoteService {
	List<TranslationResult> getTranslationResults(List<TimedChunk> chunk);
	Void setUserTranslation(int chunkId, long documentId, String userTranslation, long chosenTranslationPair);
	Document createDocument(String movieTitle, String year, String language);
    DocumentResponse createNewDocument(String movieTitle, String year, String language);
    Void selectSource(long documentID, MediaSource selectedMediaSource);

    // Logging in methods prepared for using JOpenID
    String getAuthenticationURL(long authID, AuthenticationServiceType serviceType);
    Boolean validateAuthentication(long authID, String responseURL);
    String getSessionID(long authID);

    // Simple login (returns session id on success, null in case of error (should throw an exception eventually))
    String simple_login(String username, String password);
    
    // Method signatures prepared for the moment we'll have users and session:

    Void logout(String sessionId) throws InvalidSessionIdException;
    DocumentResponse createNewDocument(String sessionId, String movieTitle, String year, String language)
            throws InvalidSessionIdException;
    TranslationResult getTranslationResults(String sessionId, TimedChunk chunk) throws InvalidSessionIdException;
    Void setUserTranslation(String sessionId, int chunkId, long documentId, String userTranslation, long chosenTranslationPairID)
            throws InvalidSessionIdException, InvalidChunkIdException, InvalidDocumentIdException;
    Void selectSource(String sessionId, long documentID, MediaSource selectedMediaSource) throws InvalidSessionIdException;
    List<Document> getListOfDocuments(String sessionId) throws InvalidSessionIdException;
    Document loadDocument(String sessionId, long documentID) throws InvalidDocumentIdException, InvalidSessionIdException;
    Void closeDocument(String sessionId, long documentId) throws InvalidSessionIdException, InvalidDocumentIdException;
    Void setChunkStartTime(String sessionId, int chunkId, long documentId, String newStartTime)
            throws InvalidSessionIdException, InvalidChunkIdException, InvalidDocumentIdException;
    Void setChunkEndTime(String sessionId, int chunkId, long documentId, String newEndTime)
            throws InvalidSessionIdException, InvalidChunkIdException, InvalidDocumentIdException;
    TranslationResult regenerateTranslationResult(String sessionId, int chunkId, long documentId, TimedChunk chunk)
            throws InvalidSessionIdException, InvalidChunkIdException, InvalidDocumentIdException;
}

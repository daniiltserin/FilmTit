package cz.filmtit.client.callables;
import com.google.gwt.user.client.*;

import cz.filmtit.client.*;

import cz.filmtit.client.pages.TranslationWorkspace;
import cz.filmtit.client.pages.TranslationWorkspace.SendChunksCommand;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.rpc.*;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.core.client.*;
import cz.filmtit.share.*;

import java.util.*;

	public class GetTranslationResults extends Callable<List<TranslationResult>> {
		
		// parameters
		List<TimedChunk> chunks;
		SendChunksCommand command;
		TranslationWorkspace workspace;
		
		int id;
		static int nextId = 0;
	
        @Override
        public String getName() {
            return "GetTranslationResults (chunks size: "+chunks.size()+")";
        }

        @Override
        protected void onEachReturn(Object returned) {
            workspace.removeGetTranslationsResultsCall(id);
        }
        
		@Override	
        public void onSuccessAfterLog(List<TranslationResult> newresults) {
			
            if (workspace.getStopLoading()) {
            	return;
            }
            else {
            	if (newresults == null || newresults.isEmpty() ||
            			!newresults.get(0).getSourceChunk().isActive ||
            			newresults.size() != chunks.size()) {
            		// expected suggestions for all chunks but did not get them
            		// retry
            		hasReturned = false;
            		if (!retry()) {
            			// cannot retry
            			
            			// tell workspace that these chunks won't arrive
            			for (TimedChunk chunk : chunks) {
                			workspace.noResult(chunk.getChunkIndex());
						}
            			// request next translations
            			command.execute();
            			// say error
            			displayWindow("Some of the translation suggestions did not arrive. " +
            					"You can ignore this or you can try refreshing the page.");
            		}
            	}
            	else {
            		// got suggestions alright
                    for (TranslationResult newresult:newresults) {
                        workspace.showResult(newresult);                	
                    }
        			// request next translations
                    command.execute();
            	}
            }
            
        }
		
		@Override
		protected void onProbablyOffline(Throwable returned) {
			// tell workspace not to expect any more results
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				@Override
				public void execute() {
        			// tell workspace that these chunks won't arrive
        			for (TimedChunk chunk : chunks) {
            			workspace.noResult(chunk.getChunkIndex());
					}
        			// tell workspace that no other chunk translations will arrive either
        			command.noMoreResults();
				}
			});
			if (LocalStorageHandler.isOnline()) {
				displayWindow("Some of the translation suggestions cannot arrive " +
						"because there is no connection to the server, " +
						"probably because you are offline. " +
						"You can try refreshing the page once back online.");
			}
			else {
				displayWindow("You went offline before some of the translation suggestions arrived. " +
						"You can try refreshing the page once back online to get them, " +
						"or you can just do without them.");
			}
		}
		
		@Override
		protected void onFinalError(String message) {
			// tell workspace that these chunks won't arrive
			for (TimedChunk chunk : chunks) {
    			workspace.noResult(chunk.getChunkIndex());
			}
			// TODO: request next translations or not based on the type of error
			// request next translations
			command.execute();
			// say error
			displayWindow("Some of the translation suggestions did not arrive. " +
				"You can ignore this or you can try refreshing the page. " +
				"Error message: " + message);
		}
		
		// constructor
		public GetTranslationResults(List<TimedChunk> chunks,
				SendChunksCommand command, TranslationWorkspace workspace) {
			super();
			
			this.chunks = chunks;
			this.command = command;
			this.workspace = workspace;
			
			// + 5s for each chunk
			callTimeOut += 5000 * chunks.size();
			
			enqueue();
		}

		@Override protected void call() {
			id = nextId++;
			workspace.addGetTranslationsResultsCall(id, this);
            filmTitService.getTranslationResults(Gui.getSessionID(), chunks, this);
		}
		
		public void stop() {
			new StopTranslationResults(chunks);
		}
	}


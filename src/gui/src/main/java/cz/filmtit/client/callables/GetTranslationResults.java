package cz.filmtit.client.callables;
import com.google.gwt.user.client.*;

import cz.filmtit.client.*;

import cz.filmtit.client.TranslationWorkspace.SendChunksCommand;
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
	
        @Override
        public String getName() {
            return "GetTranslationResults (chunks size: "+chunks.size()+")";
        }

		@Override	
        public void onSuccessAfterLog(List<TranslationResult> newresults) {
            for (TranslationResult newresult:newresults) {

                ChunkIndex poi = newresult.getSourceChunk().getChunkIndex();
                workspace.showResult(newresult);
            
            }
            command.execute();
        }
        		
		// constructor
		public GetTranslationResults(List<TimedChunk> chunks,
				SendChunksCommand command, TranslationWorkspace workspace) {
			super();
			
			this.chunks = chunks;
			this.command = command;
			this.workspace = workspace;
			
			enqueue();
		}

		@Override
		public void call() {
            filmTitService.getTranslationResults(gui.getSessionID(), chunks, this);
		}
	}

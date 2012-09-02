package cz.filmtit.client.callables;

import cz.filmtit.client.Callable;
import cz.filmtit.client.ReceivesSettings;
import cz.filmtit.share.exceptions.InvalidValueException;

/**
 * An ancestor to methods setting some settings.
 * The settings page is informed about the success or failure on return.
 */
public abstract class SetSetting<T> extends Callable<Void> {
	
	// parameters
	protected T setting;
	private ReceivesSettings settingsPage;

	@Override
	public String getName() {
		return getNameWithParameters(setting);
	}
	
    @Override
    public void onSuccessAfterLog(Void o) {
    	settingsPage.settingSuccess();
    }
    
    @Override
    public void onFailureAfterLog(Throwable returned) {
    	if (returned instanceof InvalidValueException) {
    		// the value is invalid, there is no point in trying again
    		onFinalError(returned.getLocalizedMessage());
    	}
    	else {
        	super.onFailureAfterLog(returned);
    	}
    }

    @Override
    protected void onFinalError(String message) {
        settingsPage.settingError(message);
    }
    
    /**
     * Does <b>not</b> enqueue the call immediately,
     * call enqueue() explicitly!
     */
    public SetSetting(T setting, ReceivesSettings settingsPage) {
		super();
		
		this.setting = setting;
		this.settingsPage = settingsPage;

		// do not enqueue on construction
        // enqueue();
	}

}


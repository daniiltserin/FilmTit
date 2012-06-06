package cz.filmtit.client;

import org.vectomatic.file.FileUploadExt;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

import com.github.gwtbootstrap.client.ui.*;


public class GuiStructure extends Composite {
	
	private static GuiStructureUiBinder uiBinder = GWT.create(GuiStructureUiBinder.class);

	interface GuiStructureUiBinder extends UiBinder<Widget, GuiStructure> {
	}

	public GuiStructure() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	

	@UiField
	ScrollPanel scrollPanel;
	
	@UiField
	RadioButton rdbEncodingUtf8;
	@UiField
	RadioButton rdbEncodingWin;
	@UiField
	RadioButton rdbEncodingIso;

	@UiField
	RadioButton rdbFormatSrt;
	@UiField
	RadioButton rdbFormatSub;
	
	
	@UiField
	FileUploadExt fileUpload;
	
	
	@UiField
	TextArea txtFileContentArea;
	
	@UiField
	SubmitButton btnSendToTm;
	
	@UiField
	TextArea txtDebug;
	
}
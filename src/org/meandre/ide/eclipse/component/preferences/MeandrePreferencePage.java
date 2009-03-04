package org.meandre.ide.eclipse.component.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.meandre.ide.eclipse.component.Activator;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.

 *   @author Amit Kumar
 */
public class MeandrePreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public MeandrePreferencePage() {
		super(GRID);
		
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Meandre Server "+  Activator.getPluginVersion() + " Preferences");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		addField(
				new StringFieldEditor(PreferenceConstants.P_SERVER, "&Server:", getFieldEditorParent()));

		addField(
				new StringFieldEditor(PreferenceConstants.P_PORT, "Server &Port:", getFieldEditorParent()));

		addField(
				new StringFieldEditor(PreferenceConstants.P_LOGIN, "Server &Login:", getFieldEditorParent()));

		addField(
				new StringFieldEditor(PreferenceConstants.P_PASSWORD, "Server P&assword:", getFieldEditorParent()));
		
		addField(
				new StringFieldEditor(PreferenceConstants.P_FILTERJAR, "Remove Following Jars:", getFieldEditorParent()));


		
		DirectoryFieldEditor dfe=new DirectoryFieldEditor(PreferenceConstants.P_DESC_DIR, 
				"&Descriptor Directory:", getFieldEditorParent());
		
		addField(dfe);
		
		addField(new BooleanFieldEditor(PreferenceConstants.P_EMBED,"&Embed jars",getFieldEditorParent()));
		addField(
			new BooleanFieldEditor(
				PreferenceConstants.P_OVERWRITE,
				"&Overwrite component",
				getFieldEditorParent()));
		
		addField(new BooleanFieldEditor(PreferenceConstants.P_PACKAGE_COMPONENT_AS_JAR,"&Package Individual Component as Jar",getFieldEditorParent()));
		addField(new BooleanFieldEditor(PreferenceConstants.P_HAS_ASPECT_J,"&Components with Aspect J support",getFieldEditorParent()));
		
		//addField(new BooleanFieldEditor(PreferenceConstants.P_USE_COMPONENT_VERSIONING,"&Use Component Versioning",getFieldEditorParent()));
		//addField(new BooleanFieldEditor(PreferenceConstants.P_PING_SERVER,"&Periodically Ping Server ",getFieldEditorParent()));
		addField(new BooleanFieldEditor(PreferenceConstants.P_INCLUDE_SOURCE,"&Package Source with the Component ",getFieldEditorParent()));
		
		addField(new BooleanFieldEditor(PreferenceConstants.P_CREATE_PACKAGE_PATH,"&Descriptor -Create in package directory",getFieldEditorParent()));
			
		
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
}
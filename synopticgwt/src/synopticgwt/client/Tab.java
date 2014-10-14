package synopticgwt.client;

import com.google.gwt.user.client.ui.Panel;

import synopticgwt.client.util.ProgressWheel;

/**
 * Encapsulates functionality associated with a single tab in the application. <br/>
 * <br/>
 * To enable/disable the Tab use the corresponding TabBar:
 * tabPanel.getTabBar().setTabEnabled(1, boolean); <br/>
 * <br/>
 * To check if a Tab is enabled, use: <br/>
 * tabPanel.getTabBar().isTabEnabled(tabIndex);
 */
public abstract class Tab<T extends Panel> {
    /** The service that the tab can generate calls to. */
    protected ISynopticServiceAsync synopticService;

    /** Progress indicator for this tab. */
    protected ProgressWheel pWheel;

    /** Panel with the contents of this tab. */
    protected T panel;

    /** Whether or not this tab is enabled */
    protected boolean isEnabled = false;

    /** Tracker event category name. */
    public String trackerCategoryName;

    /** Returns the service this tab communicates with. */
    public ISynopticServiceAsync getService() {
        return synopticService;
    }

    /** Returns the progress wheel associated with this tab. */
    public ProgressWheel getProgressWheel() {
        return pWheel;
    }

    /** This call returns the tab's Panel widget that holds the tab's contents. */
    public T getPanel() {
        return panel;
    }

    public Tab(ISynopticServiceAsync synopticService, ProgressWheel pWheel,
            String trackerCategoryName) {
        this.synopticService = synopticService;
        this.pWheel = pWheel;
        this.trackerCategoryName = trackerCategoryName;
    }

    public Tab(ISynopticServiceAsync synopticService) {
        this.synopticService = synopticService;
        this.pWheel = null;
    }

}

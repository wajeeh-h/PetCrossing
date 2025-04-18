/**
 * The abstract base class of controllers in our game.
 * <br><br>
 * This class extends the Observer class and is responsible for handling events.
 * It also manages an associated Panel, to display changes in the UI.
 */
public abstract class Controller extends Observer {
    /** The panel associated with this class */
    private Panel panel;

    /**
     * Constructor for the Controller class.
     * 
     * @param eventDispatcher The event dispatcher to handle events.
     * @param panel The panel associated with this controller.
     */
    public Controller(EventDispatcher eventDispatcher, Panel panel) {
        super(eventDispatcher);
        this.panel = panel;
    }

    public Panel getPanel() {
        return panel;
    }

    public void setPanel(Panel panel) {
        this.panel = panel;
    }
}

/**
 * This abstract class represents an Observer in the Observer design pattern.
 * <br><br>
 * Observers listen for events from an EventDispatcher and handle them when triggered.
 */
public abstract class Observer {
    /** The event dispatcher that this observer is associated with */
    protected EventDispatcher eventDispatcher;

    /* 
     * Initializes the observer with an event dispatcher. 
     */
    public Observer(EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    /* 
     * Initializes the observer with a new event dispatcher.
     */
    public Observer() {
        this.eventDispatcher = new EventDispatcher();
    }

    /**  
     * This class is triggered when an event associated with this object occurs.
     * 
     * @param event The event that occurred.
    */
    abstract protected void handleEvent(GameEvent event);

    /* Registers the object to listen for and respond to some game event */
    abstract protected void registerEvents();
}

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * EventDispatcher is responsible for managing the observers and notifying them
 * of events. 
 */
public class EventDispatcher {
    /** The map of observers, where the key is a game event and the value is a list of observers */
    HashMap<GameEvent, List<Observer>> observers;

    /**
     * Constructor for the EventDispatcher class, initializes the observers map.
     */
    public EventDispatcher() {
        observers = new HashMap<GameEvent, List<Observer>>();
    }

    /**
     * Adds an observer for a specific game event. 
     * <br><br>
     * If the event already has observers, the new observer is added to the list.
     * Otherwise, a new list is created for the event and the observer is added to it.
     * 
     * @param event The game event to observe.
     * @param observer The observer to add.
     */
    public <T extends Observer> void addObserver(GameEvent event, T observer) {
        if (observers.containsKey(event)) {
            observers.get(event).add(observer);
        } else {
            List<Observer> newObserver = new ArrayList<Observer>();
            newObserver.add(observer);
            observers.put(event, newObserver);
        }
    }

    /**
     * Removes an observer from a specific game event. If the event has observers,
     * the specified observer is removed from the list.
     * 
     * @param event The game event to stop observing.
     * @param observer The observer to remove.
     */
    public void removeObserver(GameEvent event, Observer observer) {
        if (observers.containsKey(event)) {
            observers.get(event).remove(observer);
        }
    }

    /**
     * Notifies all observers of a specific game event. 
     * <br><br>
     * If the event has observers, each observer is notified by calling its handleEvent method with the event.
     * For example, if the MENU event is triggered, all observers registered for the
     * MENU event will be notified, and respond by doing something (calling a method).
     * 
     * @param event The game event to notify observers about.
     */
    public void notifyObservers(GameEvent event) {
        if (observers.containsKey(event)) {
            for (Observer observer : observers.get(event)) {
                observer.handleEvent(event);
            }
        }
    }
}

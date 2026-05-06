package me.retucio.sputnik.event.sputnik;

import com.github.retucio.neutrino.Event;
import me.retucio.sputnik.friend.Friend;

// evento lanzado cuando se agrega o se elimina un amigo
public class FriendEvent extends Event {

    private final Friend friend;

    public FriendEvent(Friend friend) {
        this.friend = friend;
    }

    public Friend getFriend() {
        return friend;
    }

    public static class Add extends FriendEvent {
        public Add(Friend friend) { super(friend); }
    }

    public static class Remove extends FriendEvent {
        public Remove(Friend friend) { super(friend); }
    }
}

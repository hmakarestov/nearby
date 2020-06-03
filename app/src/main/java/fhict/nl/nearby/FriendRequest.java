package fhict.nl.nearby;

import java.util.ArrayList;

class FriendRequests {

    private ArrayList<String>requests;

    public FriendRequests() {
        requests = new ArrayList<>();
    }

    public ArrayList<String> getRequests() {
        return requests;
    }

    public void setRequests(ArrayList<String> requests) {
        this.requests = requests;
    }
}

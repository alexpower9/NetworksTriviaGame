package Client;

public interface ClientStateObserver
{
    public void onClientStateChanged(ClientState state, String message, String[] questionFile);
}
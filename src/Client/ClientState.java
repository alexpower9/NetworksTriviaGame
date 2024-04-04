package Client;

public enum ClientState
{
    AWAITING_GAME_START,
    AWAITING_QUESTION,
    QUESTION_RECIEVED,
    AWAITING_TURN,
    PULLING_TURN,
    ANSWERING_QUESTION,
    GAME_OVER,
    POLL_WON,
    POLL_LOST    
}
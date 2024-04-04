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
    WINNER_QUESTION,
    LOSER_QUESTION,
    NO_POLL,
    ANSWER_CORRECT,
    ANSWER_INCORRECT,
    NO_ANSWER    
}

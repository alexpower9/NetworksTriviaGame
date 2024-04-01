package Server;

public class Poll {
    private Integer clientID;
    private Integer questionNumber;

    public Poll(Integer clientID, Integer questionNumber){
        this.clientID = clientID;
        this.questionNumber = questionNumber;
    }

    public Integer getID(){
        return this.clientID;
    }

    public Integer getQuestionNumber(){
        return this.questionNumber;
    }

}
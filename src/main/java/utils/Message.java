package utils;

public class Message<T> {
  private MessageTypeEnum type;
  private String message;
  private T result;

  public Message() {
    this.type = MessageTypeEnum.ERROR;
    this.message = "";
    this.result = null;
  }

  public Message(MessageTypeEnum type, String message, T Result) {
    this.type = type;
    this.message = message;
    this.result = Result;
  }

  public Message(MessageTypeEnum type, String message) {
    this.type = type;
    this.message = message;
    this.result = null;
  }

  public MessageTypeEnum getType() {
    return type;
  }

  public void setType(MessageTypeEnum type) {
    this.type = type;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public T getResult() {
    return result;
  }

  public void setResult(T Result) {
    this.result = Result;
  }
}

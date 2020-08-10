package server.src.Data;

public class ApiResponse {
    public int code;
    public String type;
    public String message;

    public ApiResponse(int apiCode, String apiType, String apiMessage){
        code = apiCode;
        type = apiType;
        message = apiMessage;
    }
}

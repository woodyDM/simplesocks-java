package org.simplesocks.netty.app.http;



import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter
@Getter
@ToString
public class AjaxResponse<E> {


    private int code;
    private String msg;
    private E data;
    private String detail;

    public static final int NOT_LOGIN = 400;
    public static final int NOT_AUTHORIZED = 401;
    public static final int CODE_OK = 0;
    public static final int CODE_FAIL = 1;
    public static final int CODE_VALIDATE_FAIL = 100;

    public AjaxResponse() {

    }

    public AjaxResponse(int code) {
        this.code = code;
    }

    public AjaxResponse<E> detail(String detail) {
        this.detail = detail;
        return this;
    }

    public boolean isSuccess() {
        return code == CODE_OK;
    }

    public static <E>AjaxResponse<E> ok() {
        return new AjaxResponse<>(CODE_OK);
    }

    public static <E> AjaxResponse<E> ok(E data) {
        AjaxResponse<E> response = new AjaxResponse<>(CODE_OK);
        response.setData(data);
        return response;
    }


    public static <E> AjaxResponse<E> fail(String msg, E data) {
        return failInternal(CODE_FAIL, msg, data);
    }

    public static <E> AjaxResponse<E> fail(String msg) {
        return failInternal(CODE_FAIL, msg, null);
    }

    public static AjaxResponse validateFail(String msg, Object data) {
        return failInternal(CODE_VALIDATE_FAIL, msg, data);
    }

    public static <E> AjaxResponse<E> validateFail(String msg) {
        return failInternal(CODE_VALIDATE_FAIL, msg, null);
    }


    private static <E> AjaxResponse<E> failInternal(int code, String msg, E data) {
        AjaxResponse<E> response = new AjaxResponse<>(code);
        response.setMsg(msg);
        response.setData(data);
        return response;
    }


}


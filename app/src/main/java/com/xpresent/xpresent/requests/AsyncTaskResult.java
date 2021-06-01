/**
 * Company: Xpresent
 * Creator: Alex Fedotov
 * date: 26.05.20 17:31
 */
package com.xpresent.xpresent.requests;

class AsyncTaskResult<T> {
    private T result;
    private Exception error;

    T getResult() {
        return result;
    }

    Exception getError() {
        return error;
    }

    AsyncTaskResult(T result) {
        super();
        this.result = result;
    }

    AsyncTaskResult(Exception error) {
        super();
        this.error = error;
    }
}
